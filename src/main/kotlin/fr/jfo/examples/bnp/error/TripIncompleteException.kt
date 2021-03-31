package fr.jfo.examples.bnp.error

import fr.jfo.examples.bnp.model.Tap
import java.time.Instant

class TripIncompleteException(tap: Tap) :
    RuntimeException("Missing second tap for trip of customer ${tap.customerId} started at ${Instant.ofEpochMilli(tap.unixTimestamp)} at station ${tap.station}")
