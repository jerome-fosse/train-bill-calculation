package fr.jfo.examples.bnp.error

import fr.jfo.examples.bnp.model.Tag
import java.time.Instant

class TripIncompleteException(tag: Tag) :
    RuntimeException("Missing second tag for trip of customer ${tag.customerId} started at ${Instant.ofEpochMilli(tag.unixTimestamp)} at station ${tag.station}")
