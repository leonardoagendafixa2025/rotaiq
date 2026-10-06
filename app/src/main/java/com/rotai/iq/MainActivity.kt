package com.rotai.iq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rotai.iq.core.ui.theme.CockpitBackground
import com.rotai.iq.core.ui.theme.RotaIQTheme
import com.rotai.iq.navigation.RotaIqApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as RotaIqApplication
        val viewModelFactory = RotaIqViewModelFactory(app.repository)

        setContent {
            RotaIQTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CockpitBackground
                ) {
                    RotaIqApp(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}
