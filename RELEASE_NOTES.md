# Release Notes - Fuel & Energy Tracker (CarManager)

## Version 1.0.0 (Build 5) — Current Release
**Release Date**: September 2026  
**Package**: `today.takaki.fueltracker.vxqmkz`  
**Version Code**: `5`  
**Version Name**: `1.0`  
**Platform**: Android (Android 7.0 / API 24+ to Android 16 / API 36)

---

### Google Play Store Release Notes (What's New)

#### 🇺🇸 English (en-US)
```text
What's new in v1.1 (Build 4):
• On-Device Smart Advisor Localized: Fuel insights, cost attributions, and smart tips now fully support all languages and respect your per-app language setting.
• Layout & Typography Polishing: Fixed button wrapping and title squishing in German and Portuguese headers.
• Unit Display Fix: Removed duplicated unit suffixes in vehicle settings.
• Comprehensive I18n: All analytics charts, vehicle profiles, and dialogs fully localized across 7 languages.
```

#### 🇧🇷 Portuguese (pt-BR)
```text
Novidades na v1.1 (Build 4):
• Consultor Inteligente Localizado: Análises de custos, dicas econômicas e projeções agora 100% em português, respeitando o idioma do app.
• Ajustes Visuais e Layout: Correção de quebra de texto em botões e títulos no cabeçalho do veículo.
• Exibição de Unidades Corrigida: Removidas repetições de unidades nas configurações do veículo.
• Tradução Integral: Gráficos estatísticos, perfis e diálogos totalmente localizados.
```

---

## Version 1.0.0 (Build 3) — Previous Release
**Release Date**: September 2026  
**Package**: `today.takaki.fueltracker.vxqmkz`  
**Version Code**: `3`  
**Version Name**: `1.0`  
**Platform**: Android (Android 7.0 / API 24+ to Android 16 / API 36)

---

### Google Play Store Release Notes (What's New)

#### 🇺🇸 English (en-US) (422 chars / 500 max)
```text
What's new in v1.0 (Build 2):
• Multilingual Support: Complete translations for Portuguese (PT & BR), French (FR & CA), German, and Spanish!
• Android 13+ Per-App Language: Choose your preferred app language directly in system settings.
• Improved Localization: Dates, numbers, and fuel economy units adapt to your regional settings.
• Performance improvements and minor UI fixes across vehicle setup and log cards.
```

#### 🇧🇷 Portuguese (pt-BR) (420 chars / 500 max)
```text
Novidades na v1.0 (Build 2):
• Suporte Multilíngue Completo: Agora em Português (Brasil e Portugal), Francês, Alemão e Espanhol!
• Idioma por aplicativo (Android 13+): Escolha o idioma do app diretamente nas configurações do sistema.
• Localização aprimorada: Datas, valores e unidades de consumo adaptados à sua região.
• Melhorias de desempenho e correções visuais na gestão de veículos e registros.
```

#### 🇵🇹 Portuguese (pt-PT) (424 chars / 500 max)
```text
Novidades na v1.0 (Build 2):
• Suporte Multilíngue Completo: Agora disponível em Português europeu, Francês, Alemão e Espanhol!
• Idioma por Aplicação (Android 13+): Escolha o idioma da app diretamente nas definições do sistema.
• Localização Melhorada: Datas, valores e unidades de consumo adaptados à sua região.
• Melhorias de estabilidade e correções visuais nos registos e gestão de veículos.
```

#### 🇫🇷 French (fr-FR) (438 chars / 500 max)
```text
Nouveautés de la v1.0 (Version 2) :
• Support multilingue complet : Maintenant disponible en français, portugais, allemand et espagnol !
• Langue par application (Android 13+) : Choisissez votre langue préférée dans les paramètres système.
• Localisation soignée : Dates, devises et unités de consommation adaptées à votre région.
• Améliorations des performances et ajustements visuels dans les journaux et profils.
```

#### 🇨🇦 French (fr-CA) (440 chars / 500 max)
```text
Nouveautés de la v1.0 (Version 2) :
• Support multilingue complet : Disponible en français canadien, portugais, allemand et espagnol !
• Langue par application (Android 13+) : Réglez la langue de l'application dans les paramètres système.
• Localisation adaptée : Dates, devises et unités de consommation calibrées pour votre région.
• Optimisations de l'interface et corrections mineures dans les journaux de carburant.
```

#### 🇩🇪 German (de-DE) (444 chars / 500 max)
```text
Neu in v1.0 (Build 2):
• Vollständige Mehrsprachigkeit: Jetzt auf Deutsch, Portugiesisch, Französisch und Spanisch!
• Sprachauswahl pro App (Android 13+): Wählen Sie Ihre bevorzugte Sprache in den Systemeinstellungen.
• Regionale Anpassung: Datumsangaben, Währungen und Verbrauchseinheiten passen sich automatisch an.
• Leistungsverbesserungen und UI-Korrekturen bei Fahrzeuginformationen und Protokollen.
```

#### 🇪🇸 Spanish (es-ES) (448 chars / 500 max)
```text
Novedades de la v1.0 (Compilación 2):
• Soporte multilingüe completo: ¡Ahora disponible en español, portugués, francés y alemán!
• Idioma por aplicación (Android 13+): Elige el idioma de la app directamente en los ajustes del sistema.
• Localización mejorada: Fechas, monedas y unidades de consumo adaptadas a tu región.
• Mejoras de rendimiento y correcciones de interfaz en el registro de combustible y garaje.
```

---

### Detailed Changelog in Build 2

#### 🌐 Internationalization (I18n) & Regional Adaptation
- **7 Target Locales Supported**:
  - `en` / `en-US` (English - US)
  - `pt-BR` (Portuguese - Brazil)
  - `pt-PT` (Portuguese - Portugal)
  - `fr-FR` (French - France)
  - `fr-CA` (French - Canada)
  - `de-DE` (German - Germany)
  - `es-ES` (Spanish - Spain)
- **Modern Android 13+ Per-App Languages**:
  - Automated `localeConfig` XML generation powered by AGP and `resources.properties`.
  - Native system settings language selector for user flexibility.
- **Dynamic Localization in Jetpack Compose UI**:
  - All screens (`DashboardScreen`, `LogsHistoryScreen`, `AnalyticsScreen`, `VehicleProfileScreen`, `MainScreen`) and components (`AddEditFuelLogDialog`, `VehicleSettingsDialog`, `ArchiveExchangeDialog`, `ImportExportDialog`, `FuelLogCard`) dynamically load string resources.
  - Enum representations (`FuelType`, `DistanceUnit`, `VolumeUnit`, `EconomyUnit`, `TimeFilter`) mapped to localized string resources.

#### 🧪 Quality Assurance & Test Verification
- Added `LocalizationTest.kt` verifying non-empty, accurate string resolution across all supported locales.
- All 13 unit/robolectric test suites pass cleanly (`./gradlew testDebugUnitTest`).
- Kover test coverage report generated and validated.

---

<details>
<summary><strong>Version 1.0.0 (Build 1) — Initial Release</strong></summary>

### Highlights & Key Features

#### ⛽ Fuel & EV Energy Tracking
- **Multi-Fuel Support**: Out-of-the-box support for Gasoline/Petrol, Diesel, Electric (EV), Hybrid, LPG/Autogas, and CNG.
- **Detailed Entries**: Log odometer readings, volume/energy consumed (L, US gal, UK gal, kWh, kg), unit price, total cost, fuel stations, fuel grades, and full-tank flags.
- **Accurate Consumption Calculations**: Automatically detects consecutive full-tank fill-ups to calculate real-world consumption and distance traveled per tank/charge.

#### 📊 Analytics & Visual Dashboards
- **Spending Breakdowns**: Interactive period summaries across weekly, monthly, and yearly intervals with historical period comparisons.
- **Price History Trends**: Visual price trend charts showing min, max, and average prices paid over time.
- **Versatile Economy Units**: Full support for global metric and imperial units (`L/100km`, `km/L`, `MPG US`, `MPG UK`, `kWh/100km`, `mi/kWh`, etc.).

#### 🤖 On-Device Smart Advisor (AI Insights)
- **Automated Period Analysis**: Calculates driving efficiency scores and economic ratings based on consumption patterns.
- **Actionable Recommendations**: Offers personalized driving tips, identifies anomalous consumption spikes, and projects future fuel budgets.
- **Local-First & Private**: Analysis heuristics run seamlessly on-device.

#### 🚘 Multi-Vehicle Profile Management
- **Fleet & Multi-Car Support**: Manage multiple cars, motorcycles, or commercial vehicles with distinct fuel types and units.
- **Vehicle Archiving**: Archive inactive or sold vehicles while preserving complete historical records.
- **Data Portability**: Full CSV export and import capabilities to backup and restore vehicle logs at any time.

</details>
