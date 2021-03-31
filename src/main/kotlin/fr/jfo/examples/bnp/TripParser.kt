package fr.jfo.examples.bnp

import fr.jfo.examples.bnp.model.Trip
import java.io.File
import java.io.InputStream

class TripParser {

    fun parse(file: File): List<Trip> = throw NotImplementedError("TODO")

    fun parse(inputStream: InputStream): List<Trip> = throw NotImplementedError("TODO")
}
