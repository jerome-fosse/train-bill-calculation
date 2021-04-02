package fr.jfo.examples.bnp

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class BillingTest {

    @Test
    fun `calculating prices with file CandidateInputExample should produce the same result than CandidateOutputExample`() {
        // When calculating prices for the file CandidateInputExample.txt
        Billing.main(arrayOf("-s", "src/test/resources/CandidateInputExample.txt", "-d", "target/BillingOutput.txt"))

        // Then the result should be the same that the file CandidateOutputExample
        val result = File("target/BillingOutput.txt").reader(Charsets.UTF_8)
            .use { it.readText() }
        val expected = File("src/test/resources/CandidateOutputExample.txt").reader(Charsets.UTF_8)
            .use { it.readText() }
        val mapper = ObjectMapper().registerModule(KotlinModule())
        Assertions.assertThat(mapper.readTree(result)).isEqualTo(mapper.readTree(expected))
    }
}
