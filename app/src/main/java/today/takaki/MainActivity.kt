package today.takaki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import today.takaki.data.util.LocaleManager
import today.takaki.data.util.ProvideLocalizedApp
import today.takaki.ui.screens.MainScreen
import today.takaki.ui.theme.MyApplicationTheme
import today.takaki.ui.viewmodel.FuelTrackerViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: FuelTrackerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    LocaleManager.init(this)
    enableEdgeToEdge()
    setContent {
      ProvideLocalizedApp {
        MyApplicationTheme {
          Surface(modifier = Modifier.fillMaxSize()) {
            MainScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}
