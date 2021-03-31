package fr.jfo.examples.bnp

import fr.jfo.examples.bnp.model.Trip
import org.apache.commons.cli.*
import java.io.File

class Billing {

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
            options.addOption(
                    Option.builder("h")
                            .longOpt("help")
                            .required(false)
                            .desc("show usage")
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
            var trips = loadCustomersTrips(cmd.getOptionValue("s"))

        } catch (e: ParseException) {
            println("Syntax error. " + e.message)
            showUsage(options)
        }
    }

    private fun loadCustomersTrips(fileName: String): List<Trip> {
        val tripParser = TripParser();
        return tripParser.parse(File(fileName))
            .foldRight(Pair(ArrayList<Throwable>(), ArrayList<Trip>())) {either, pair ->
                when (either) {
                    is Either.Left -> pair.first.add(either.value)
                    is Either.Right -> pair.second.add(either.value)
                }
                pair
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
