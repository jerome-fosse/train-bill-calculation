package fr.jfo.examples.bnp

import org.apache.commons.cli.*

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
            val parser = DefaultParser()
            val cmd = parser.parse(options, args)
            if (cmd.hasOption("h")) {
                showUsage(options)
            }
        } catch (e: ParseException) {
            println("Syntax error. " + e.message)
            showUsage(options)
        }
    }

    private fun showUsage(options: Options) {
        val helpFormatter = HelpFormatter()
        helpFormatter.printHelp("billing", options)
    }
}
