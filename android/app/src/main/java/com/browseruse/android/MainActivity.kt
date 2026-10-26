package com.browseruse.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.browseruse.android.ui.AppRoot
import com.browseruse.android.ui.theme.BrowserUseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrowserUseTheme {
                AppRoot()
            }
        }
    }
}
