package fr.jfo.examples.bnp.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class Trip(
    @JsonIgnore
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
