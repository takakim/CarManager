package today.takaki.data.util

import today.takaki.data.model.DistanceUnit
import today.takaki.data.model.EconomyUnit
import today.takaki.data.model.FuelLog
import today.takaki.data.model.FuelType
import today.takaki.data.model.VehicleProfile
import today.takaki.data.model.VolumeUnit
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedBackup(
  val vehicles: List<VehicleProfile>,
  val logs: List<FuelLog>
)

object ImportExportHelper {

  private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

  /**
   * Generates a clean, standard CSV from a list of fuel logs.
   */
  fun exportToCsv(vehicle: VehicleProfile, logs: List<FuelLog>): String {
    val sb = StringBuilder()
    // CSV Header
    sb.append("Date,Odometer (${vehicle.distanceUnit.symbol}),Amount (${vehicle.volumeUnit.symbol}),Unit Price (${vehicle.currencySymbol}),Total Cost (${vehicle.currencySymbol}),Full Tank,Missed Previous,Station,Grade,Notes,Timestamp\n")

    logs.sortedBy { it.odometer }.forEach { log ->
      val dateStr = isoDateFormat.format(Date(log.timestamp))
      sb.append(escapeCsv(dateStr)).append(",")
      sb.append(String.format(Locale.US, "%.1f", log.odometer)).append(",")
      sb.append(String.format(Locale.US, "%.2f", log.amount)).append(",")
      sb.append(String.format(Locale.US, "%.3f", log.unitPrice)).append(",")
      sb.append(String.format(Locale.US, "%.2f", log.totalCost)).append(",")
      sb.append(if (log.isFullTank) "TRUE" else "FALSE").append(",")
      sb.append(if (log.isMissedPrevious) "TRUE" else "FALSE").append(",")
      sb.append(escapeCsv(log.stationName)).append(",")
      sb.append(escapeCsv(log.fuelGrade)).append(",")
      sb.append(escapeCsv(log.notes)).append(",")
      sb.append(log.timestamp).append("\n")
    }

    return sb.toString()
  }

  /**
   * Generates a full JSON backup including vehicles and all logs.
   */
  fun exportToJson(vehicles: List<VehicleProfile>, logs: List<FuelLog>): String {
    val root = JSONObject()
    root.put("app", "FuelTracker")
    root.put("version", 2)
    root.put("exportedAt", System.currentTimeMillis())

    val vehiclesArray = JSONArray()
    vehicles.forEach { v ->
      val vObj = JSONObject().apply {
        put("id", v.id)
        put("name", v.name)
        put("makeModel", v.makeModel)
        put("year", v.year)
        put("fuelType", v.fuelType.name)
        put("purchaseDateMillis", v.purchaseDateMillis)
        put("purchasePrice", v.purchasePrice)
        put("initialOdometer", v.initialOdometer)
        put("currencySymbol", v.currencySymbol)
        put("distanceUnit", v.distanceUnit.name)
        put("volumeUnit", v.volumeUnit.name)
        put("economyUnit", v.economyUnit.name)
        put("tankCapacity", v.tankCapacity)
        put("isArchived", v.isArchived)
        if (v.archiveDateMillis != null) put("archiveDateMillis", v.archiveDateMillis)
        if (v.archiveOdometer != null) put("archiveOdometer", v.archiveOdometer)
        put("archiveReason", v.archiveReason)
      }
      vehiclesArray.put(vObj)
    }
    root.put("vehicles", vehiclesArray)

    val logsArray = JSONArray()
    logs.forEach { l ->
      val lObj = JSONObject().apply {
        put("id", l.id)
        put("vehicleId", l.vehicleId)
        put("timestamp", l.timestamp)
        put("odometer", l.odometer)
        put("amount", l.amount)
        put("unitPrice", l.unitPrice)
        put("totalCost", l.totalCost)
        put("isFullTank", l.isFullTank)
        put("isMissedPrevious", l.isMissedPrevious)
        put("stationName", l.stationName)
        put("fuelGrade", l.fuelGrade)
        put("notes", l.notes)
      }
      logsArray.put(lObj)
    }
    root.put("logs", logsArray)

    return root.toString(2)
  }

  /**
   * Parses CSV string into a list of FuelLog objects for the given vehicleId.
   */
  fun parseCsv(csvText: String, targetVehicleId: Int): List<FuelLog> {
    val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()

    val parsedLogs = mutableListOf<FuelLog>()
    var startIndex = 0

    // Check if first line is a header
    val firstLine = lines[0].lowercase()
    if (firstLine.contains("date") || firstLine.contains("odometer") || firstLine.contains("amount") || firstLine.contains("cost")) {
      startIndex = 1
    }

    for (i in startIndex until lines.size) {
      val row = parseCsvRow(lines[i])
      if (row.size < 4) continue

      try {
        // Expected order: Date, Odometer, Amount, UnitPrice, TotalCost, FullTank, MissedPrevious, Station, Grade, Notes, [Timestamp]
        val odo = row.getOrNull(1)?.toDoubleOrNull() ?: continue
        val amt = row.getOrNull(2)?.toDoubleOrNull() ?: continue
        var unitPrice = row.getOrNull(3)?.toDoubleOrNull() ?: 0.0
        var totalCost = row.getOrNull(4)?.toDoubleOrNull() ?: 0.0

        if (totalCost <= 0.0 && unitPrice > 0.0 && amt > 0.0) {
          totalCost = unitPrice * amt
        } else if (unitPrice <= 0.0 && totalCost > 0.0 && amt > 0.0) {
          unitPrice = totalCost / amt
        }

        val fullTank = row.getOrNull(5)?.trim()?.lowercase()?.let { it == "true" || it == "1" || it == "yes" } ?: true
        val missed = row.getOrNull(6)?.trim()?.lowercase()?.let { it == "true" || it == "1" || it == "yes" } ?: false
        val station = row.getOrNull(7) ?: ""
        val grade = row.getOrNull(8) ?: "Regular"
        val notes = row.getOrNull(9) ?: ""

        var timestamp: Long = System.currentTimeMillis()
        val tsFromRow = row.getOrNull(10)?.toLongOrNull()
        if (tsFromRow != null && tsFromRow > 0) {
          timestamp = tsFromRow
        } else {
          val dateStr = row.getOrNull(0)
          if (!dateStr.isNullOrBlank()) {
            val parsedDate = runCatching { isoDateFormat.parse(dateStr) }.getOrNull()
              ?: runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) }.getOrNull()
              ?: runCatching { SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(dateStr) }.getOrNull()
              ?: runCatching { SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateStr) }.getOrNull()
            if (parsedDate != null) timestamp = parsedDate.time
          }
        }

        parsedLogs.add(
          FuelLog(
            id = 0L,
            vehicleId = targetVehicleId,
            timestamp = timestamp,
            odometer = odo,
            amount = amt,
            unitPrice = unitPrice,
            totalCost = totalCost,
            isFullTank = fullTank,
            isMissedPrevious = missed,
            stationName = station,
            fuelGrade = grade,
            notes = notes
          )
        )
      } catch (_: Exception) {
        // Skip malformed row
      }
    }

    return parsedLogs.sortedBy { it.odometer }
  }

  /**
   * Parses JSON backup containing vehicles and/or logs.
   */
  fun parseJson(jsonText: String, defaultVehicleId: Int): ParsedBackup {
    val root = JSONObject(jsonText)
    val vehicles = mutableListOf<VehicleProfile>()
    val logs = mutableListOf<FuelLog>()

    if (root.has("vehicles")) {
      val vArray = root.getJSONArray("vehicles")
      for (i in 0 until vArray.length()) {
        val vObj = vArray.getJSONObject(i)
        val v = VehicleProfile(
          id = vObj.optInt("id", 0),
          name = vObj.optString("name", "Car"),
          makeModel = vObj.optString("makeModel", "Daily Driver"),
          year = vObj.optInt("year", 2023),
          fuelType = runCatching { FuelType.valueOf(vObj.optString("fuelType", "GASOLINE")) }.getOrDefault(FuelType.GASOLINE),
          purchaseDateMillis = vObj.optLong("purchaseDateMillis", System.currentTimeMillis()),
          purchasePrice = vObj.optDouble("purchasePrice", 0.0),
          initialOdometer = vObj.optDouble("initialOdometer", 0.0),
          currencySymbol = vObj.optString("currencySymbol", "$"),
          distanceUnit = runCatching { DistanceUnit.valueOf(vObj.optString("distanceUnit", "KILOMETERS")) }.getOrDefault(DistanceUnit.KILOMETERS),
          volumeUnit = runCatching { VolumeUnit.valueOf(vObj.optString("volumeUnit", "LITERS")) }.getOrDefault(VolumeUnit.LITERS),
          economyUnit = runCatching { EconomyUnit.valueOf(vObj.optString("economyUnit", "L_PER_100KM")) }.getOrDefault(EconomyUnit.L_PER_100KM),
          tankCapacity = vObj.optDouble("tankCapacity", 50.0),
          isArchived = vObj.optBoolean("isArchived", false),
          archiveDateMillis = if (vObj.has("archiveDateMillis")) vObj.optLong("archiveDateMillis") else null,
          archiveOdometer = if (vObj.has("archiveOdometer")) vObj.optDouble("archiveOdometer") else null,
          archiveReason = vObj.optString("archiveReason", "")
        )
        vehicles.add(v)
      }
    } else if (root.has("vehicle")) {
      // Single vehicle backup
      val vObj = root.getJSONObject("vehicle")
      val v = VehicleProfile(
        id = vObj.optInt("id", defaultVehicleId),
        name = vObj.optString("name", "Car"),
        makeModel = vObj.optString("makeModel", "Daily Driver"),
        year = vObj.optInt("year", 2023),
        fuelType = runCatching { FuelType.valueOf(vObj.optString("fuelType", "GASOLINE")) }.getOrDefault(FuelType.GASOLINE),
        purchaseDateMillis = vObj.optLong("purchaseDateMillis", System.currentTimeMillis()),
        purchasePrice = vObj.optDouble("purchasePrice", 0.0),
        initialOdometer = vObj.optDouble("initialOdometer", 0.0),
        currencySymbol = vObj.optString("currencySymbol", "$"),
        distanceUnit = runCatching { DistanceUnit.valueOf(vObj.optString("distanceUnit", "KILOMETERS")) }.getOrDefault(DistanceUnit.KILOMETERS),
        volumeUnit = runCatching { VolumeUnit.valueOf(vObj.optString("volumeUnit", "LITERS")) }.getOrDefault(VolumeUnit.LITERS),
        economyUnit = runCatching { EconomyUnit.valueOf(vObj.optString("economyUnit", "L_PER_100KM")) }.getOrDefault(EconomyUnit.L_PER_100KM),
        tankCapacity = vObj.optDouble("tankCapacity", 50.0),
        isArchived = vObj.optBoolean("isArchived", false),
        archiveDateMillis = if (vObj.has("archiveDateMillis")) vObj.optLong("archiveDateMillis") else null,
        archiveOdometer = if (vObj.has("archiveOdometer")) vObj.optDouble("archiveOdometer") else null,
        archiveReason = vObj.optString("archiveReason", "")
      )
      vehicles.add(v)
    }

    if (root.has("logs")) {
      val lArray = root.getJSONArray("logs")
      for (i in 0 until lArray.length()) {
        val lObj = lArray.getJSONObject(i)
        val l = FuelLog(
          id = 0L,
          vehicleId = lObj.optInt("vehicleId", defaultVehicleId),
          timestamp = lObj.optLong("timestamp", System.currentTimeMillis()),
          odometer = lObj.optDouble("odometer", 0.0),
          amount = lObj.optDouble("amount", 0.0),
          unitPrice = lObj.optDouble("unitPrice", 0.0),
          totalCost = lObj.optDouble("totalCost", 0.0),
          isFullTank = lObj.optBoolean("isFullTank", true),
          isMissedPrevious = lObj.optBoolean("isMissedPrevious", false),
          stationName = lObj.optString("stationName", ""),
          fuelGrade = lObj.optString("fuelGrade", "Regular"),
          notes = lObj.optString("notes", "")
        )
        logs.add(l)
      }
    }

    return ParsedBackup(vehicles, logs)
  }

  private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      "\"" + value.replace("\"", "\"\"") + "\""
    } else {
      value
    }
  }

  private fun parseCsvRow(line: String): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false

    var i = 0
    while (i < line.length) {
      val c = line[i]
      if (c == '\"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
          sb.append('\"')
          i++
        } else {
          inQuotes = !inQuotes
        }
      } else if (c == ',' && !inQuotes) {
        tokens.add(sb.toString().trim())
        sb.setLength(0)
      } else {
        sb.append(c)
      }
      i++
    }
    tokens.add(sb.toString().trim())
    return tokens
  }
}
