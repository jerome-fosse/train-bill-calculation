package fr.jfo.examples.bnp

import fr.jfo.examples.bnp.error.TripIncompleteException
import fr.jfo.examples.bnp.error.ZoneNotFoundException
import fr.jfo.examples.bnp.model.Trip
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class TripServiceTest {

    @Nested
    inner class TapsParsing {
        @Test
        fun `when there is two taps for the same client it should return a trip`() {
            // Given a source file with two taps for the same customer
            val source = """
                {
                    "taps" : [
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
            val tripService = TripService()
            val trips = tripService.parseTaps(ByteArrayInputStream(source.toByteArray()))

            // Then I have a list of 1 trip for the customer 1
            assertThat(trips).isNotEmpty
            assertThat(trips).size().isEqualTo(1)
            assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))
        }

        @Test
        fun `when there is four taps for the same client it should return two trips`() {
            // Given a source file with four taps for the same customer
            val source = """
                {
                    "taps" : [
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
            val tripService = TripService()
            val trips = tripService.parseTaps(ByteArrayInputStream(source.toByteArray()))

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
        fun `when there is an odd number of taps for a customer then the last trip should be on error`() {
            // Given a source file with three taps for the same customer
            val source = """
                {
                    "taps" : [
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
            val tripService = TripService()
            val trips = tripService.parseTaps(ByteArrayInputStream(source.toByteArray()))

            // Then I have a list of two trips for the customer 1
            assertThat(trips).isNotEmpty
            assertThat(trips).size().isEqualTo(2)

            // With a first trip for customer 1
            assertThat(trips[0].isRight).isTrue
            assertThat(trips[0]).isEqualTo(Either.Right(Trip(customerId = 1, stationStart = "A", stationEnd = "B", startedJourneyAt = 10)))

            // And the second trip is in fact an error
            assertThat(trips[1].isLeft).isTrue
            assertThat(trips[1].left()).isInstanceOf(TripIncompleteException::class.java)
            assertThat(trips[1].left()?.message).isEqualTo("Missing second tap for trip of customer 1 started at 1970-01-01T00:00:00.015Z at station G")
        }

        @Test
        fun `when there is taps for more than one customer it should retrieve the travels for each customers`() {
            // Given a source file with taps for two customers
            val source = """
                {
                    "taps" : [
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
            val tripService = TripService()
            val trips = tripService.parseTaps(ByteArrayInputStream(source.toByteArray()))

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
        fun `when customers's taps are mixed it should retrieve the travels for each customers anyway `() {
            // Given a source file with taps for two customers
            val source = """
                {
                    "taps" : [
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
            val tripService = TripService()
            val trips = tripService.parseTaps(ByteArrayInputStream(source.toByteArray()))

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

    @Nested
    inner class PriceCalculation {
        @Test
        fun `it should find the price of a trip`() {
            // Given a trip from station A (zone 1) to station C (zone 2)
            val trip = Trip(customerId = 1, stationStart = "A", stationEnd = "C", startedJourneyAt = 1)

            // When I calculate the price
            val tripService = TripService()
            val price = tripService.calculatePrice(trip)

            // it should give me the price
            assertThat(price.isRight).isTrue
            assertThat((price.right()!!.costInCents)).isEqualTo(240)
        }

        @Test
        fun `it should return an error when the price a station does not exists`() {
            // Given a trip from station A (zone 1) to station Z (zone 2). Z does not exists
            val trip = Trip(customerId = 1, stationStart = "A", stationEnd = "Z", startedJourneyAt = 1)

            // When I calculate the price
            val tripService = TripService()
            val price = tripService.calculatePrice(trip)

            // it should return an error
            assertThat(price.isLeft).isTrue
            assertThat((price.left())).isInstanceOf(ZoneNotFoundException::class.java)
        }

        @Test
        fun `when there is more than one price for a trip it should return the lowest`() {
            // Given a trip from station A (zone 1) to station F (zone 3 and Zone 4)
            val trip = Trip(customerId = 1, stationStart = "A", stationEnd = "F", startedJourneyAt = 1)

            // When I calculate the price
            val tripService = TripService()
            val price = tripService.calculatePrice(trip)

            // it should return the lowest price. 280 and not 300
            assertThat(price.isRight).isTrue
            assertThat((price.right()!!.costInCents)).isEqualTo(280)
        }
    }
}
