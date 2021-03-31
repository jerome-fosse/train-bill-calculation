package fr.jfo.examples.bnp

import fr.jfo.examples.bnp.error.TripIncompleteException
import fr.jfo.examples.bnp.model.Trip
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class TripParserTest {

    @Test
    fun `when there is two tags for the same client it should return a trip`() {
        // Given a source file with two tags for the same customer
        val source = """
            {
                "tags" : [
                    {
                      "unixTimestamp": 10,
                      "customerId": 1,
                      "station": "A"
                    },
                    {
                      "unixTimestamp": 12,
                      "customerId": 1,
                      "station": "B"
                    }
                ]
            }
        """

        // When I parse the file
        val parser = TripParser()
        val trips = parser.parse(ByteArrayInputStream(source.toByteArray()))

        // Then I have a list of 1 trip for the customer 1
        assertThat(trips).isNotEmpty
        assertThat(trips).size().isEqualTo(1)
        assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))
    }

    @Test
    fun `when there is four tags for the same client it should return two trips`() {
        // Given a source file with four tags for the same customer
        val source = """
            {
                "tags" : [
                    {
                      "unixTimestamp": 10,
                      "customerId": 1,
                      "station": "A"
                    },
                    {
                      "unixTimestamp": 12,
                      "customerId": 1,
                      "station": "B"
                    },
                    {
                      "unixTimestamp": 15,
                      "customerId": 1,
                      "station": "G"
                    },
                    {
                      "unixTimestamp": 17,
                      "customerId": 1,
                      "station": "C"
                    }
                ]
            }
        """

        // When I parse the file
        val parser = TripParser()
        val trips = parser.parse(ByteArrayInputStream(source.toByteArray()))

        // Then I have a list of two trips for the customer 1
        assertThat(trips).isNotEmpty
        assertThat(trips).size().isEqualTo(2)

        // With a first trip for customer 1
        assertThat(trips[0].isRight).isTrue
        assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))

        // And a second trip for customer 1
        assertThat(trips[1].isRight).isTrue
        assertThat(trips[1]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "G", stationEnd = "C", startedJourneyAt = 15)))
    }

    @Test
    fun `when there is an odd number of tags for a customer then the last trip should be on error`() {
        // Given a source file with three tags for the same customer
        val source = """
            {
                "tags" : [
                    {
                      "unixTimestamp": 10,
                      "customerId": 1,
                      "station": "A"
                    },
                    {
                      "unixTimestamp": 12,
                      "customerId": 1,
                      "station": "B"
                    },
                    {
                      "unixTimestamp": 15,
                      "customerId": 1,
                      "station": "G"
                    }
                ]
            }
        """

        // When I parse the file
        val parser = TripParser()
        val trips = parser.parse(ByteArrayInputStream(source.toByteArray()))

        // Then I have a list of two trips for the customer 1
        assertThat(trips).isNotEmpty
        assertThat(trips).size().isEqualTo(2)

        // With a first trip for customer 1
        assertThat(trips[0].isRight).isTrue
        assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))

        // And the second trip is in fact an error
        assertThat(trips[1].isLeft).isTrue
        assertThat(trips[1].left()).isInstanceOf(TripIncompleteException::class.java)
        assertThat(trips[1].left()?.message).isEqualTo("Missing second tag for trip of customer 1 started at 1970-01-01T00:00:00.015Z at station G")
    }

    @Test
    fun `when there is tags for more than one customer it should retrieve the travels for each customers`() {
        // Given a source file with tags for two customers
        val source = """
            {
                "tags" : [
                    {
                      "unixTimestamp": 10,
                      "customerId": 1,
                      "station": "A"
                    },
                    {
                      "unixTimestamp": 12,
                      "customerId": 1,
                      "station": "B"
                    },
                    {
                      "unixTimestamp": 11,
                      "customerId": 2,
                      "station": "B"
                    },
                    {
                      "unixTimestamp": 16,
                      "customerId": 2,
                      "station": "E"
                    },
                    {
                      "unixTimestamp": 15,
                      "customerId": 1,
                      "station": "G"
                    },
                    {
                      "unixTimestamp": 17,
                      "customerId": 1,
                      "station": "C"
                    }
                ]
            }
        """

        // When I parse the file
        val parser = TripParser()
        val trips = parser.parse(ByteArrayInputStream(source.toByteArray()))

        // Then I have a list of three trips for the customer 1 and customer 2
        assertThat(trips).isNotEmpty
        assertThat(trips).size().isEqualTo(3)

        // With a first trip for customer 1
        assertThat(trips[0].isRight).isTrue
        assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))

        // And a second trip for customer 1
        assertThat(trips[1].isRight).isTrue
        assertThat(trips[1]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "G", stationEnd = "C", startedJourneyAt = 15)))

        // And a third trip for customer 2
        assertThat(trips[1].isRight).isTrue
        assertThat(trips[2]).isEqualTo(Either.Right(Trip(customerId = 2, stationStart = "B", stationEnd = "E", startedJourneyAt = 11)))
    }

    @Test
    fun `when customers's tags are mixed it should retrieve the travels for each customers anyway `() {
        // Given a source file with tags for two customers
        val source = """
            {
                "tags" : [
                    {
                      "unixTimestamp": 10,
                      "customerId": 1,
                      "station": "A"
                    },
                    {
                      "unixTimestamp": 11,
                      "customerId": 2,
                      "station": "B"
                    },
                    {
                      "unixTimestamp": 12,
                      "customerId": 1,
                      "station": "B"
                    },
                    {
                      "unixTimestamp": 16,
                      "customerId": 2,
                      "station": "E"
                    },
                    {
                      "unixTimestamp": 15,
                      "customerId": 1,
                      "station": "G"
                    },
                    {
                      "unixTimestamp": 17,
                      "customerId": 1,
                      "station": "C"
                    }
                ]
            }
        """

        // When I parse the file
        val parser = TripParser()
        val trips = parser.parse(ByteArrayInputStream(source.toByteArray()))

        // Then I have a list of three trips for the customer 1 and customer 2
        assertThat(trips).isNotEmpty
        assertThat(trips).size().isEqualTo(3)

        // With a first trip for customer 1
        assertThat(trips[0].isRight).isTrue
        assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))

        // And a second trip for customer 1
        assertThat(trips[1].isRight).isTrue
        assertThat(trips[1]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "G", stationEnd = "C", startedJourneyAt = 15)))

        // And a third trip for customer 2
        assertThat(trips[1].isRight).isTrue
        assertThat(trips[2]).isEqualTo(Either.Right(Trip(customerId = 2, stationStart = "B", stationEnd = "E", startedJourneyAt = 11)))
    }
}
