package fr.jfo.examples.bnp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import fr.jfo.examples.bnp.Either.Left
import fr.jfo.examples.bnp.Either.Right
import fr.jfo.examples.bnp.error.PriceNotFoundException
import fr.jfo.examples.bnp.error.TripIncompleteException
import fr.jfo.examples.bnp.error.ZoneNotFoundException
import fr.jfo.examples.bnp.model.Price
import fr.jfo.examples.bnp.model.TravelTaps
import fr.jfo.examples.bnp.model.Trip
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TripService {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun parseTaps(file: File): List<Either<Throwable, Trip>> = parseTaps(FileInputStream(file))

    fun parseTaps(inputStream: InputStream): List<Either<Throwable, Trip>> {
        val travelTaps: TravelTaps = this.mapper.readValue(inputStream.readBytes())
        return travelTaps.taps
            .also { println("${it.size} taps imported.") }
            .groupBy { it.customerId }
            .flatMap { it.value.chunked(2) }
            .map { when (it.size) {
                1 -> Left(TripIncompleteException(it[0]))
                else -> Right(Trip(customerId = it[0].customerId, stationStart = it[0].station, stationEnd = it[1].station, startedJourneyAt = it[0].unixTimestamp))
            } }
    }

    fun calculatePrice(trip: Trip): Either<Throwable, Price> {
        val zonesFrom = Parameters.zonesOfStation(trip.stationStart)
        val zonesTo = Parameters.zonesOfStation(trip.stationEnd)

        if (zonesFrom.isNullOrEmpty()) {
            return Left(ZoneNotFoundException(trip.stationStart))
        }
        if (zonesTo.isNullOrEmpty()) {
            return Left(ZoneNotFoundException(trip.stationEnd))
        }

        val prices = zonesFrom
            .foldRight(listOf<Pair<Int, Int>>()) { i, acc -> listOf(acc, zonesTo.map { (Pair(i, it)) }).flatten() }
            .flatMap { Parameters.priceFor(it.first, it.second) }

        return when (prices.size) {
            0 -> Left(PriceNotFoundException(trip))
            else -> Right(prices.sortedBy { it.costInCents }.first() )
        }
    }
}
