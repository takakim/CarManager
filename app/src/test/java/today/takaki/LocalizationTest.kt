package today.takaki

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import today.takaki.data.model.DistanceUnit
import today.takaki.data.model.EconomyUnit
import today.takaki.data.model.FuelType
import today.takaki.data.model.TimeFilter
import today.takaki.data.model.VolumeUnit
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocalizationTest {

  private fun getLocalizedContext(locale: Locale): Context {
    val base = ApplicationProvider.getApplicationContext<Context>()
    val config = Configuration(base.resources.configuration)
    config.setLocale(locale)
    return base.createConfigurationContext(config)
  }

  @Test
  fun `verify English US translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("en-US"))
    assertEquals("Overview", context.getString(R.string.tab_overview))
    assertEquals("Logs", context.getString(R.string.tab_logs))
    assertEquals("Analytics", context.getString(R.string.tab_analytics))
    assertEquals("Vehicle", context.getString(R.string.tab_vehicle))
    assertEquals("Save", context.getString(R.string.action_save))
    assertEquals("Delete", context.getString(R.string.action_delete))
    assertEquals("Cancel", context.getString(R.string.action_cancel))
    assertEquals("Gasoline / Petrol", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify Portuguese Brazil translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("pt-BR"))
    assertEquals("Visão Geral", context.getString(R.string.tab_overview))
    assertEquals("Registros", context.getString(R.string.tab_logs))
    assertEquals("Estatísticas", context.getString(R.string.tab_analytics))
    assertEquals("Veículo", context.getString(R.string.tab_vehicle))
    assertEquals("Salvar", context.getString(R.string.action_save))
    assertEquals("Excluir", context.getString(R.string.action_delete))
    assertEquals("Cancelar", context.getString(R.string.action_cancel))
    assertEquals("Gasolina", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify Portuguese Portugal translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("pt-PT"))
    assertEquals("Visão Geral", context.getString(R.string.tab_overview))
    assertEquals("Registos", context.getString(R.string.tab_logs))
    assertEquals("Estatísticas", context.getString(R.string.tab_analytics))
    assertEquals("Veículo", context.getString(R.string.tab_vehicle))
    assertEquals("Guardar", context.getString(R.string.action_save))
    assertEquals("Eliminar", context.getString(R.string.action_delete))
    assertEquals("Cancelar", context.getString(R.string.action_cancel))
    assertEquals("Gasolina", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify French France translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("fr-FR"))
    assertEquals("Aperçu", context.getString(R.string.tab_overview))
    assertEquals("Journaux", context.getString(R.string.tab_logs))
    assertEquals("Analytique", context.getString(R.string.tab_analytics))
    assertEquals("Véhicule", context.getString(R.string.tab_vehicle))
    assertEquals("Enregistrer", context.getString(R.string.action_save))
    assertEquals("Supprimer", context.getString(R.string.action_delete))
    assertEquals("Annuler", context.getString(R.string.action_cancel))
    assertEquals("Essence", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify French Canada translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("fr-CA"))
    assertEquals("Aperçu", context.getString(R.string.tab_overview))
    assertEquals("Journaux", context.getString(R.string.tab_logs))
    assertEquals("Analytique", context.getString(R.string.tab_analytics))
    assertEquals("Véhicule", context.getString(R.string.tab_vehicle))
    assertEquals("Enregistrer", context.getString(R.string.action_save))
    assertEquals("Supprimer", context.getString(R.string.action_delete))
    assertEquals("Annuler", context.getString(R.string.action_cancel))
    assertEquals("Essence", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify German Germany translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("de-DE"))
    assertEquals("Übersicht", context.getString(R.string.tab_overview))
    assertEquals("Einträge", context.getString(R.string.tab_logs))
    assertEquals("Statistiken", context.getString(R.string.tab_analytics))
    assertEquals("Fahrzeug", context.getString(R.string.tab_vehicle))
    assertEquals("Speichern", context.getString(R.string.action_save))
    assertEquals("Löschen", context.getString(R.string.action_delete))
    assertEquals("Abbrechen", context.getString(R.string.action_cancel))
    assertEquals("Benzin", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify Spanish Spain translations`() {
    val context = getLocalizedContext(Locale.forLanguageTag("es-ES"))
    assertEquals("Resumen", context.getString(R.string.tab_overview))
    assertEquals("Registros", context.getString(R.string.tab_logs))
    assertEquals("Estadísticas", context.getString(R.string.tab_analytics))
    assertEquals("Vehículo", context.getString(R.string.tab_vehicle))
    assertEquals("Guardar", context.getString(R.string.action_save))
    assertEquals("Eliminar", context.getString(R.string.action_delete))
    assertEquals("Cancelar", context.getString(R.string.action_cancel))
    assertEquals("Gasolina", context.getString(R.string.fuel_gasoline))
  }

  @Test
  fun `verify all enum nameRes resolve non-empty strings across all supported locales`() {
    val locales = listOf(
      Locale.forLanguageTag("en-US"),
      Locale.forLanguageTag("en-GB"),
      Locale.forLanguageTag("pt-PT"),
      Locale.forLanguageTag("pt-BR"),
      Locale.forLanguageTag("fr-FR"),
      Locale.forLanguageTag("fr-CA"),
      Locale.forLanguageTag("de-DE"),
      Locale.forLanguageTag("es-ES")
    )

    for (locale in locales) {
      val context = getLocalizedContext(locale)

      for (item in FuelType.values()) {
        val str = context.getString(item.nameRes)
        assertNotNull("Missing string for FuelType.$item in $locale", str)
        assertFalse("Empty string for FuelType.$item in $locale", str.isBlank())
      }

      for (item in DistanceUnit.values()) {
        val str = context.getString(item.nameRes)
        assertNotNull("Missing string for DistanceUnit.$item in $locale", str)
        assertFalse("Empty string for DistanceUnit.$item in $locale", str.isBlank())
      }

      for (item in VolumeUnit.values()) {
        val str = context.getString(item.nameRes)
        assertNotNull("Missing string for VolumeUnit.$item in $locale", str)
        assertFalse("Empty string for VolumeUnit.$item in $locale", str.isBlank())
      }

      for (item in EconomyUnit.values()) {
        val str = context.getString(item.nameRes)
        assertNotNull("Missing string for EconomyUnit.$item in $locale", str)
        assertFalse("Empty string for EconomyUnit.$item in $locale", str.isBlank())
      }

      for (item in TimeFilter.values()) {
        val str = context.getString(item.nameRes)
        assertNotNull("Missing string for TimeFilter.$item in $locale", str)
        assertFalse("Empty string for TimeFilter.$item in $locale", str.isBlank())
      }

      val testKeys = listOf(
        R.string.ownership_history_title,
        R.string.smart_advisor_card_title,
        R.string.delete_vehicle_title,
        R.string.total_running_costs,
        R.string.lowest_paid,
        R.string.highest_paid,
        R.string.ai_finding_favorable_price_title,
        R.string.export_btn,
        R.string.price_paid,
        R.string.date_present,
        R.string.odo_range_fmt,
        R.string.archive_exchange_new_tab,
        R.string.archive_only_tab,
        R.string.ownership_end_details,
        R.string.exchange_archive_date,
        R.string.final_odometer,
        R.string.reason_for_archive,
        R.string.reason_traded_in,
        R.string.reason_sold_private,
        R.string.reason_lease_ended,
        R.string.reason_fleet_change,
        R.string.reason_scrapped,
        R.string.reason_other,
        R.string.new_replacement_car_details,
        R.string.car_nickname,
        R.string.battery_capacity_kwh,
        R.string.archive_starting_odo_note
      )
      for (key in testKeys) {
        val str = context.getString(key)
        assertNotNull("Missing string for key $key in $locale", str)
        assertFalse("Empty string for key $key in $locale", str.isBlank())
      }
    }
  }
}
