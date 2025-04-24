package cz.fit.cvut.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import cz.fit.cvut.demo.navigation.AppNavigation
import cz.fit.cvut.feature.language.presentation.TolgeeLanguageDropdown
import cz.fit.cvut.feature.translation.presentation.common.component.Translate
import cz.fit.cvut.sdk.components.TolgeeProvider
import cz.fit.cvut.sdk.extensions.registerAsRouteProviderForTolgee
import cz.fit.cvut.demo.ui.theme.TolgeeSdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TolgeeSdkTheme {
                TolgeeProvider {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column() {
        Translate(
            keyName = "app_subscriptions",
            modifier = modifier
        )
        Translate(
            keyName = "app_subscriptions_free",
            modifier = modifier
        )
        Translate(
            keyName = "app_close_button",
            modifier = modifier
        )

        Translate(
            keyName = "app_formatted_text",
            modifier = modifier,
            params = mapOf(
                "param" to "random param"
            )
        )

        TolgeeLanguageDropdown()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TolgeeSdkTheme {
        Greeting("Android")
    }
}