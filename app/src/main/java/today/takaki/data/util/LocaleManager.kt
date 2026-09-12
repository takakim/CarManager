package today.takaki.data.util

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SupportedLanguage(
  val code: String,
  val displayName: String,
  val flagEmoji: String
)

object LocaleManager {
  private const val PREFS_NAME = "fuel_tracker_locale_prefs"
  private const val KEY_LANG = "selected_language"

  val supportedLanguages = listOf(
    SupportedLanguage("system", "System Default", "🌐"),
    SupportedLanguage("en-US", "English (US)", "🇺🇸"),
    SupportedLanguage("en-GB", "English (UK)", "🇬🇧"),
    SupportedLanguage("es-ES", "Español (España)", "🇪🇸"),
    SupportedLanguage("pt-PT", "Português (Portugal)", "🇵🇹"),
    SupportedLanguage("pt-BR", "Português (Brasil)", "🇧🇷"),
    SupportedLanguage("fr-FR", "Français (France)", "🇫🇷"),
    SupportedLanguage("fr-CA", "Français (Canada)", "🇨🇦"),
    SupportedLanguage("de-DE", "Deutsch (Deutschland)", "🇩🇪")
  )

  private val _currentLanguageCode = MutableStateFlow("system")
  val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

  fun init(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    _currentLanguageCode.value = prefs.getString(KEY_LANG, "system") ?: "system"
  }

  fun setLanguage(context: Context, code: String) {
    _currentLanguageCode.value = code
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LANG, code).apply()
  }

  fun getLocaleForCode(code: String): Locale {
    if (code == "system") return Locale.getDefault()
    val parts = code.split("-", "_")
    return if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
  }
}

@Composable
fun ProvideLocalizedApp(
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  val selectedCode by LocaleManager.currentLanguageCode.collectAsState()

  val locale = remember(selectedCode) {
    LocaleManager.getLocaleForCode(selectedCode)
  }

  val localizedContext = remember(context, locale, selectedCode) {
    if (selectedCode == "system") {
      context
    } else {
      val config = Configuration(context.resources.configuration)
      config.setLocale(locale)
      config.setLayoutDirection(locale)
      context.createConfigurationContext(config)
    }
  }

  val localizedConfig = remember(locale, selectedCode) {
    val config = Configuration(context.resources.configuration)
    if (selectedCode != "system") {
      config.setLocale(locale)
      config.setLayoutDirection(locale)
    }
    config
  }

  CompositionLocalProvider(
    LocalContext provides localizedContext,
    LocalConfiguration provides localizedConfig
  ) {
    content()
  }
}
