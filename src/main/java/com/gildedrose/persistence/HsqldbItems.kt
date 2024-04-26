package com.gildedrose.persistence

import com.gildedrose.domain.ID
import com.gildedrose.domain.Item
import com.gildedrose.domain.Quality
import com.gildedrose.domain.StockList
import com.gildedrose.persistence.StockListLoadingError.IOError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate
import javax.sql.DataSource


class ConnectionContext(val connection: Connection) : TXContext(), Connection by connection

class HsqldbItems(private val dataSource: DataSource) : Items<ConnectionContext> {
    override fun <R> inTransaction(block: context(ConnectionContext) () -> R): R {
        return dataSource.connection.use { connection ->
            try {
                block(ConnectionContext(connection))
                    .also { connection.commit() }
            } catch (t: Throwable) {
                connection.rollback()
                throw t
            }
        }
    }

    context(ConnectionContext)
    override fun save(stockList: StockList): Result<StockList, IOError> {
        val toSave = when {
            stockList.items.isEmpty() -> listOf(sentinelItem)
            else -> stockList.items
        }

        return connection.prepareStatement(
            //language=HSQLDB
            """
            INSERT INTO ITEMS (id,modified,name,"sellByDate",quality)
            VALUES (?,?,?,?,?)
            """
        ).use { s ->
            toSave.forEach { item ->
                s.setString(1, item.id.value.value)
                s.setObject(2, stockList.lastModified)
                s.setString(3, item.name)
                s.setObject(4, item.sellByDate)
                s.setInt(5, item.quality.valueInt)
                s.execute()
            }
            Success(stockList)
        }
    }

    context(ConnectionContext)
    override fun load(): Result<StockList, StockListLoadingError> =
        connection.prepareStatement(
            //language=HSQLDB
            """
            SELECT id,modified,name,"sellByDate",quality
            FROM ITEMS
            WHERE modified = (SELECT MAX(modified) FROM ITEMS)
            """
        ).use { s ->
            val items = mutableListOf<Item>()
            var modified: Instant? = null

            s.executeQuery().use { rs ->
                while (rs.next()) {
                    if (modified == null) {
                        modified = rs.getObject("modified", Instant::class.java)
                    }

                    items.add(
                        Item(
                            id = ID(rs.getString("id")) ?: return Failure(IOError("invalid id")),
                            name = rs.getString("name"),
                            sellByDate = rs.getObject("sellByDate", LocalDate::class.java),
                            quality = Quality(rs.getInt("quality")) ?: return Failure(IOError("invalid quality"))
                        )
                    )
                }
            }

            val isEmpty = items.isEmpty() || (items.singleOrNull() == sentinelItem)

            Success(
                if (isEmpty) {
                    StockList(modified ?: Instant.EPOCH, emptyList())
                } else {
                    StockList(
                        lastModified = modified ?: Instant.EPOCH,
                        items = items.toList()
                    )
                }
            )
        }
}
