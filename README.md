# Test technique BNP
## Scenario
Our company has recently bought a train company. The trains run between train stations. A train station is geographically situated in a zone, some stations are situated on the boundary of a zone which means they are classed as being within 2 zones. Zones are a set of concentric circles which start at the centre of the city. Zone 1 is at the centre of the city and Zone 4 is furthest away from the city.

Pricing is dependent upon:
1. Which zone(s) you travel between.
2. What train stations are situated on a zone boundary, say zones 2 and zone 3, these have a specific pricing rule. The customer is charged the lowest fare, for instance if you are travelling from zone 1 to a station on the boundary of zone 2 and 3 then you will be charged the fare of zone 1 to zone 2.
3. You are charged per trip.

## Stations and zones
Zone 1 stations:<br/>
A<br/>
B<br/>

Zone 2 stations:<br/>
C<br/>
D<br/>
E<br/>

Zone 3 stations:<br/>
C<br/>
E<br/>
F<br/>

Zone 4 stations:<br/>
F<br/>
G<br/>
H<br/>
I<br/>

## Pricing
We charge in euros (€), a euro has 100 cents.
1. Travel within zones 1 and 2: €2.40 per trip.
2. Travel within zones 3 and 4: €2.00 per trip.
3. Travel from zone 3 to zone 1 or 2: €2.80 per trip.
4. Travel from zone 4 to Zone 1 or 2: €3.00 per trip.
5. Travel from zone 1 or 2, to zone 3: €2.80 per trip.
6. Travel from zone 1 or 2, zone 4 to: €3.00 per trip.
7. If there is the possibility of two prices then we must charge the customer the lowest amount and reflect this in the pricing information.

## Test objective
We bill customers at the end of the day, based on an input file which contains all customer journeys.  You need to write a process that from the input file passed in at the command line will generate a summary of the customers trips and total costings.  The input file and output files are defined and an example of each are included, see "CandidateInputExample.txt" and "CandidateOutputExample.txt". To help with understanding the relationship between the input and output we have given time values as small integers, we know you can not travel between stations in one millisecond!  The input file given, when passed as a path to your program as the first command line parameter and the output file path passed as the second command line parameter, must generate the valid JSON output of the given output file (differences in whitespace and ordering are of course permitted - as per the JSON standard).

A customer always "taps" their customer travel card at a barrier/gate which allows us to record their entry and then their exit with a second "tap" as the leave at the destination.  You can assume that:
1. For each customers' journey we always get an entry and exit tap pair in a file. An entry will always be before an exit.
2. The customers first tap represents an entry at the beginning of the journey and the second tap their exit at the end of the journey.
3/ A customer may have multiple journeys in a day.

## Input File explanation
A snippet of input file has the following format:
```json
{
    "taps": [
        {
            "unixTimestamp": 1572242400,
            "customerId": 1,
            "station": "A"
        },
        {
            "unixTimestamp": 1572244200,
            "customerId": 1,
            "station": "D"
        },
        {
            "unixTimestamp": 1572282000,
            "customerId": 1,
            "station": "D"
        },
        {
            "unixTimestamp": 1572283800,
            "customerId": 1,
            "station": "A"
        }
    ]
}
```

This represents customer who had id 1 taking a journey from station A at the unix time of 1572242400 which represents 2019-10-28T06:00+00:00 and complete their journey by exiting Station D at the unix time of 1572244200 which represents 2019-10-28T06:30+00:00.  The customer then returns in the opposite direction at 2019-10-28T17:30+00:00 (1572282000).

## Output File explanation
The output file has the following format:
```json
{
    "customerSummaries": [
        {
            "customerId": 1,
            "totalCostInCents": 480,
            "trips": [
                {
                    "stationStart": "A",
                    "stationEnd": "D",
                    "startedJourneyAt": 1572242400,
                    "costInCents": 240,
                    "zoneFrom": 1,
                    "zoneTo": 2
                },
                {
                    "stationStart": "D",
                    "stationEnd": "A",
                    "startedJourneyAt": 1572282000,
                    "costInCents": 240,
                    "zoneFrom": 2,
                    "zoneTo": 1
                }
            ]
        }
    ]
}
```

For each customer we output:
1. "customerId" - the customers identifier.
2. "totalCostInCents" - the total cost of the days journeys in cents.

For each of the customers' trips we output the following:
1. "stationStart" - the station where the journey started at.
2. "stationEnd" - the station where the journey ended at.
3. "startedJourneyAt" - the time when journey started at.
4. "costInCents" - the cost of the journey in cents.
5. "zoneFrom" the zone which the user was charged from.
6. "zoneTo" - the zone which the user was charged to.

## Expectations
1. Be aware that your sample file has the main requirements only and we expect you to test all the requirements based on the requirements given.  We have a test file, which has all the scenarios covered by these requirements, if your submission when passed our test file does not pass tests which we will run on your output file (differences in whitespace and ordering are of course permitted - as per the JSON standard), then we will not review the code and will reply to the recruiter stating that it does not meet the functional requirements.  For obvious reasons we will not share our test file, please do not ask.  The aim of this test is not to catch you out, it is to validate that you can take requirements and have the skills to create a professional program.
2. We do not require elaborate console output, and want to see how YOU write code.  Please do not use dependency injection frameworks, aspect oriented frameworks, such as spring or any other code generation frameworks such as Lombok.  Significant consideration is given to how well you can meet requirements.
3. We are looking for a professional object oriented submission in Java or Kotlin, with unit tests which demonstrates your skills, of how you would deliver a project at work. An important point is it must build in any IDE by using a either gradle or maven to define the build process and any dependencies. If we cannot build it, it will fail.
4. We expect your main method to take 2 input parameters, the first is the input file path and the second is the output file path.  No other inputs should be required to run your program.
