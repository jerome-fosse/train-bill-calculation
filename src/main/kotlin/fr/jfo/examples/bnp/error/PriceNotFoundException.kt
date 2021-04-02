package fr.jfo.examples.bnp.error

import fr.jfo.examples.bnp.model.Trip
import java.lang.RuntimeException

class PriceNotFoundException(private val trip: Trip):
    RuntimeException("Price not found for trip for customer ${trip.customerId} from station ${trip.stationStart} ro station ${trip.stationEnd}.")
