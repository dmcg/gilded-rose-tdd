# Parallel Map Behaviour

| Variant              | Concurrency | Throws             | Cancellation | Execution |
|----------------------|------------:|--------------------|--------------|-----------|
| Serial (kotlin map)  |         0.9 | original exception | immediate    | blocks    |
| Streams              |         6.4 | original exception | yes          | blocks    |
| Streams forkJoinPool |          40 | ExecutionException | yes          | blocks    |
| Threads              |          68 | original exception | no unbounded | blocks    |
| ThreadPool           |          40 | ExecutionException | no           | blocks    |
| Coroutines (sleep)   |          39 | original exception | yes          | suspends  |
| Coroutines (delay)   |          83 | original exception | yes          | suspends  |


