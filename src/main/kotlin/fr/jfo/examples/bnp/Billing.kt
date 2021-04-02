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
                .foldRight(Pair(mutableListOf<Left<Throwable>>(), mutableListOf<Right<Trip>>())) { either, acc ->
                    when (either) {
                        is Left -> acc.first.add(either)
                        is Right -> acc.second.add(either)
                    }
                    acc
                }
                .also {
                    println("${it.first.size} error(s) while calculating prices.")
                    it.first.forEach { err -> println(err.value.message) }
                }
                .second
                .map { it.value }
                .groupBy { it.customerId }
                .map {
                    val totalPrice = it.value.sumOf { price -> price.costInCents ?: 0}
                    CustomerSummary(customerId = it.key, totalCostInCents = totalPrice, trips = it.value.sortedBy { trip -> trip.startedJourneyAt })
                }
                .sortedBy { it.customerId }

            writeResultFile(cmd.getOptionValue("d"), summaries)
        } catch (e: ParseException) {
            println("Syntax error. " + e.message)
            showUsage(options)
        }
    }

    private fun writeResultFile(destFileName: String, summaries: List<CustomerSummary>) {
        File(destFileName).printWriter()
            .use { out ->
                println("Writing result to $destFileName")
                out.write(mapper.writeValueAsString(BillingReport(summaries)))
            }
    }

    private fun loadCustomersTrips(fileName: String): List<Trip> {
        return tripService.parseTaps(File(fileName))
            .foldRight(Pair(mutableListOf<Throwable>(), mutableListOf<Trip>())) { either, acc ->
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
