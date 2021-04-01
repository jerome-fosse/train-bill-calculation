package fr.jfo.examples.bnp.model

data class Trip(
    val customerId: Int,
    val stationStart: String,
    val stationEnd: String,
    val startedJourneyAt: Long,
    val costInCents: Int?,
    val zoneFrom: Int?,
    val zoneTo: Int?) {

    constructor(customerId: Int, stationStart: String, stationEnd: String, startedJourneyAt: Long) :
        this(customerId, stationStart, stationEnd, startedJourneyAt, null, null, null)
}
