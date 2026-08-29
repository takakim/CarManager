package com.example

import com.example.data.model.FuelLog
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
