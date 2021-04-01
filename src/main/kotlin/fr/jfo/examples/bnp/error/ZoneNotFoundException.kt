package fr.jfo.examples.bnp.error

class ZoneNotFoundException(private val station: String): RuntimeException("Zone not found for station $station.")
