package fr.jfo.examples.bnp

import fr.jfo.examples.bnp.model.Price

object Parameters {
    private val stations = mapOf(
        Pair("A", listOf(1)), Pair("B", listOf(1)), Pair("C", listOf(2, 3)),
        Pair("D", listOf(2)), Pair("E", listOf(2, 3)), Pair("F", listOf(3, 4)),
        Pair("G", listOf(4)), Pair("H", listOf(4)), Pair("I", listOf(4))
    )

    private val prices = listOf(
        Price(costInCents = 240, zoneFrom = 1, zoneTo = 2),
        Price(costInCents = 200, zoneFrom = 3, zoneTo = 4),
        Price(costInCents = 200, zoneFrom = 3, zoneTo = 1),
        Price(costInCents = 200, zoneFrom = 3, zoneTo = 2),
        Price(costInCents = 300, zoneFrom = 4, zoneTo = 1),
        Price(costInCents = 300, zoneFrom = 4, zoneTo = 2),
        Price(costInCents = 280, zoneFrom = 1, zoneTo = 3),
        Price(costInCents = 280, zoneFrom = 2, zoneTo = 3),
        Price(costInCents = 300, zoneFrom = 1, zoneTo = 4),
        Price(costInCents = 300, zoneFrom = 2, zoneTo = 4)
    )

    fun zonesOfStation(station: String) = stations[station]

    fun priceFor(zoneFrom: Int, zoneTo: Int) = prices.filter { it.zoneFrom == zoneFrom && it.zoneTo == zoneTo }
}
