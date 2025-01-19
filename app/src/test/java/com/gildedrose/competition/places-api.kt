package com.gildedrose.competition

data class Root(
    val places: List<Place>
)

data class Place(
    val name: String,
    val id: String,
    val types: List<String>,
    val nationalPhoneNumber: String,
    val internationalPhoneNumber: String,
    val formattedAddress: String,
    val addressComponents: List<AddressComponent>,
    val plusCode: PlusCode,
    val location: Location,
    val viewport: Viewport,
    val rating: Double,
    val googleMapsUri: String,
    val websiteUri: String,
    val regularOpeningHours: RegularOpeningHours?,
    val utcOffsetMinutes: Int,
    val adrFormatAddress: String,
    val businessStatus: String,
    val userRatingCount: Int,
    val iconMaskBaseUri: String,
    val iconBackgroundColor: String,
    val displayName: DisplayName,
    val primaryTypeDisplayName: PrimaryTypeDisplayName,
    val delivery: Boolean,
    val curbsidePickup: Boolean,
    val currentOpeningHours: CurrentOpeningHours?,
    val primaryType: String,
    val shortFormattedAddress: String,
    val editorialSummary: EditorialSummary?,
    val reviews: List<Review>,
    val photos: List<Photo>,
    val paymentOptions: PaymentOptions,
    val accessibilityOptions: AccessibilityOptions?
)

data class AddressComponent(
    val longText: String,
    val shortText: String,
    val types: List<String>,
    val languageCode: String
)

data class PlusCode(
    val globalCode: String,
    val compoundCode: String
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class Viewport(
    val low: Low,
    val high: High
)

data class Low(
    val latitude: Double,
    val longitude: Double
)

data class High(
    val latitude: Double,
    val longitude: Double
)

data class RegularOpeningHours(
    val openNow: Boolean,
    val periods: List<Period>,
    val weekdayDescriptions: List<String>
)

data class Period(
    val open: Open,
    val close: Close
)

data class Open(
    val day: Int,
    val hour: Int,
    val minute: Int
)

data class Close(
    val day: Int,
    val hour: Int,
    val minute: Int
)

data class DisplayName(
    val text: String,
    val languageCode: String
)

data class PrimaryTypeDisplayName(
    val text: String,
    val languageCode: String
)

data class CurrentOpeningHours(
    val openNow: Boolean,
    val periods: List<Period>,
    val weekdayDescriptions: List<String>
)

data class EditorialSummary(
    val text: String,
    val languageCode: String
)

data class Review(
    val name: String,
    val relativePublishTimeDescription: String,
    val rating: Int,
    val text: Text,
    val originalText: OriginalText,
    val authorAttribution: AuthorAttribution,
    val publishTime: String
)

data class Text(
    val text: String,
    val languageCode: String
)

data class OriginalText(
    val text: String,
    val languageCode: String
)

data class AuthorAttribution(
    val displayName: String,
    val uri: String,
    val photoUri: String
)

data class Photo(
    val name: String,
    val widthPx: Int,
    val heightPx: Int,
    val authorAttributions: List<AuthorAttribution>
)

data class PaymentOptions(
    val acceptsCreditCards: Boolean,
    val acceptsDebitCards: Boolean,
    val acceptsCashOnly: Boolean,
    val acceptsNfc: Boolean
)

data class AccessibilityOptions(
    val wheelchairAccessibleParking: Boolean,
    val wheelchairAccessibleEntrance: Boolean
)
