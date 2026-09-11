package today.takaki

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import today.takaki.data.advisor.OnDeviceSmartAdvisorService
import today.takaki.data.model.DistanceUnit
import today.takaki.data.model.EconomyUnit
import today.takaki.data.model.FuelLog
import today.takaki.data.model.FuelType
import today.takaki.data.model.PeriodSummary
import today.takaki.data.model.TimeFilter
import today.takaki.data.model.VehicleProfile
import today.takaki.data.model.VolumeUnit
import today.takaki.data.util.ImportExportHelper

class ExampleUnitTest {

  @Test
  fun testWeightedAveragePricePaidCalculation() {
    val logs = listOf(
      FuelLog(
        odometer = 1000.0,
        amount = 40.0,
        unitPrice = 1.50,
        totalCost = 60.00
      ),
      FuelLog(
        odometer = 1500.0,
        amount = 50.0,
        unitPrice = 1.40,
        totalCost = 70.00
      ),
      FuelLog(
        odometer = 2000.0,
        amount = 30.0,
        unitPrice = 1.60,
        totalCost = 48.00
      )
    )

    val totalSpent = logs.sumOf { it.totalCost } // 60 + 70 + 48 = 178.0
    val totalVolume = logs.sumOf { it.amount } // 40 + 50 + 30 = 120.0
    val weightedAvgPrice = totalSpent / totalVolume // 178 / 120 = 1.48333...

    assertEquals(178.0, totalSpent, 0.001)
    assertEquals(120.0, totalVolume, 0.001)
    assertEquals(1.4833, weightedAvgPrice, 0.001)
  }

  @Test
  fun testCsvExportAndParseRoundTrip() {
    val vehicle = VehicleProfile(
      id = 1,
      name = "Civic",
      makeModel = "Honda Civic",
      year = 2022,
      fuelType = FuelType.GASOLINE,
      initialOdometer = 10000.0,
      currencySymbol = "$",
      distanceUnit = DistanceUnit.KILOMETERS,
      volumeUnit = VolumeUnit.LITERS
    )

    val logs = listOf(
      FuelLog(
        id = 10,
        vehicleId = 1,
        odometer = 10500.0,
        amount = 45.0,
        unitPrice = 1.55,
        totalCost = 69.75,
        timestamp = 1700000000000L,
        isFullTank = true,
        stationName = "Shell",
        fuelGrade = "Regular 87",
        notes = "Commute fillup"
      )
    )

    val csv = ImportExportHelper.exportToCsv(vehicle, logs)
    val parsed = ImportExportHelper.parseCsv(csv, vehicle.id)

    assertEquals(1, parsed.size)
    assertEquals(10500.0, parsed[0].odometer, 0.001)
    assertEquals(45.0, parsed[0].amount, 0.001)
    assertEquals(1.55, parsed[0].unitPrice, 0.001)
    assertEquals(69.75, parsed[0].totalCost, 0.001)
    assertEquals("Shell", parsed[0].stationName)
  }

  @Test
  fun testEconomyUnitCalculation() {
    // 500 km on 35 Liters
    val lPer100 = EconomyUnit.calculate(
      500.0,
      35.0,
      EconomyUnit.L_PER_100KM
    ) // (35 / 500) * 100 = 7.0 L/100km
    assertEquals(7.0, lPer100, 0.001)

    // 300 miles on 10 gallons
    val mpg = EconomyUnit.calculate(
      300.0,
      10.0,
      EconomyUnit.MPG_US
    ) // 300 / 10 = 30.0 MPG
    assertEquals(30.0, mpg, 0.001)
  }

  @Test
  fun testAiHeuristicPeriodAnalysis() {
    val vehicle = VehicleProfile(
      id = 1,
      name = "Daily Sedan",
      makeModel = "Toyota Corolla",
      year = 2021,
      currencySymbol = "$",
      fuelType = FuelType.GASOLINE,
      volumeUnit = VolumeUnit.LITERS,
      distanceUnit = DistanceUnit.KILOMETERS
    )

    val summary = PeriodSummary(
      timeFilter = TimeFilter.THIS_WEEK,
      startDateMillis = 1000L,
      endDateMillis = 5000L,
      totalSpent = 75.0,
      totalVolume = 50.0,
      totalDistance = 600.0,
      avgPricePaid = 1.50,
      minPricePaid = 1.48,
      maxPricePaid = 1.52,
      averageEconomy = 8.33,
      spendingPerDay = 10.71,
      spendingPerWeek = 75.0,
      spendingPerMonth = 325.0
    )

    val service = OnDeviceSmartAdvisorService()
    val analysis = runBlocking {
      service.analyzePeriod(
        vehicle = vehicle,
        summary = summary,
        periodLogs = emptyList(),
        allTimeAvgPrice = 1.45,
        allTimeSummary = summary
      )
    }

    assertNotNull(analysis)
    assertEquals(TimeFilter.THIS_WEEK, analysis.timeFilter)
    assertTrue("Score should be positive", analysis.efficiencyScore > 0)
    assertTrue("Recommendations should not be empty", analysis.recommendations.isNotEmpty())
    assertTrue("Forecast should not be blank", analysis.nextPeriodForecast.isNotBlank())
  }
}
