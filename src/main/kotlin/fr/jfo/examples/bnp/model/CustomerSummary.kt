package fr.jfo.examples.bnp.model

data class CustomerSummary(
    val customerId: Int,
    val totalCostInCents: Int,
    val trips: List<Trip>
)
