package fr.jfo.examples.bnp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import fr.jfo.examples.bnp.Either.Left
import fr.jfo.examples.bnp.Either.Right
import fr.jfo.examples.bnp.error.TripIncompleteException
import fr.jfo.examples.bnp.model.TravelTaps
import fr.jfo.examples.bnp.model.Trip
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class TripParser {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    fun parse(file: File): List<Either<Throwable, Trip>> = parse(FileInputStream(file))

    fun parse(inputStream: InputStream): List<Either<Throwable, Trip>> {
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

}
