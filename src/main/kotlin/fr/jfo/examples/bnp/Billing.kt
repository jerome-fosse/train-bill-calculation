package fr.jfo.examples.bnp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import fr.jfo.examples.bnp.Either.Left
import fr.jfo.examples.bnp.Either.Right
import fr.jfo.examples.bnp.model.BillingReport
import fr.jfo.examples.bnp.model.CustomerSummary
import fr.jfo.examples.bnp.model.Trip
import org.apache.commons.cli.*
import java.io.File
import java.io.FileOutputStream

class Billing {
    private val tripService = TripService();
    private val mapper = ObjectMapper().registerModule(KotlinModule()).enable(SerializationFeature.INDENT_OUTPUT)

    companion object Billing {
        @JvmStatic
        fun main(args: Array<String>) {
            val options = Options()
            options.addOption(
                    Option.builder("s")
                            .longOpt("src")
                            .required(true)
                            .hasArg(true)
                            .argName("file")
                            .desc("source file")
                            .build()
            )
            options.addOption(
                    Option.builder("d")
                            .longOpt("dest")
                            .required(true)
                            .hasArg(true)
                            .argName("file")
                            .desc("destination file")
                            .build()
            )

            val billing = Billing();
            billing.run(options, args)
        }
    }

    fun run(options: Options, args: Array<String>) {
        try {
            val cmd = parseCommandLine(options, args)
            if (cmd.hasOption("h")) {
                showUsage(options)
            }

            println("Importing taps...")
            val summaries = loadCustomersTrips(cmd.getOptionValue("s"))
                .map {  trip ->
                    when(val either = tripService.calculatePrice(trip)) {
                        is Left -> either
                        is Right -> Right(trip.copy(zoneFrom = either.value.zoneFrom, zoneTo = either.value.zoneTo, costInCents = either.value.costInCents))
                    }
                }
                .groupBy { it.isLeft }
                .also {
                    println("${it[true]?.size ?: 0} error(s) while calculating prices.")
                    it[true]?.forEach { err -> println(err.left()?.message) }
                }
                .filter { it.key == false }
                .flatMap { it.value }
                .map { it.right()!! }
                .groupBy { it.customerId }
                .map {
                    val totalPrice = it.value.sumOf { price -> price.costInCents!! }
                    CustomerSummary(customerId = it.key, totalCostInCents = totalPrice, trips = it.value.sortedBy { trip -> trip.startedJourneyAt })
                }
                .sortedBy { it.customerId }


            File(cmd.getOptionValue("d")).printWriter()
                .use { out ->
                    println("Writing result to ${cmd.getOptionValue("d")}")
                    out.write(mapper.writeValueAsString(BillingReport(summaries)))
                }


        } catch (e: ParseException) {
            println("Syntax error. " + e.message)
            showUsage(options)
        }
    }

    private fun loadCustomersTrips(fileName: String): List<Trip> {
        return tripService.parseTaps(File(fileName))
            .foldRight(Pair(ArrayList<Throwable>(), ArrayList<Trip>())) {either, acc ->
                when (either) {
                    is Left -> acc.first.add(either.value)
                    is Right -> acc.second.add(either.value)
                }
                acc
            }
            .also { pair ->
                println("${pair.first.size} error(s) during taps import.")
                pair.first.forEach { println (it.message) }
                println("${pair.second.size} trip(s) imported.")
            }
            .second
    }

    private fun parseCommandLine(options: Options, args: Array<String>): CommandLine {
        val parser = DefaultParser()
        return parser.parse(options, args)
    }

    private fun showUsage(options: Options) {
        val helpFormatter = HelpFormatter()
        helpFormatter.printHelp("billing", options)
    }
}
