package com.igent.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.igent.mobile.ui.screens.IgentAppShell
import com.igent.mobile.ui.theme.IgentTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val targetScreen = intent?.getStringExtra(com.igent.mobile.fcm.IgentFirebaseMessagingService.EXTRA_TARGET_SCREEN)

        setContent {
            IgentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IgentAppShell(initialTargetScreen = targetScreen)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val targetScreen = intent?.getStringExtra(com.igent.mobile.fcm.IgentFirebaseMessagingService.EXTRA_TARGET_SCREEN)
        // If needed, we can also pass this down or let a shared event flow handle deep linking when already running
    }
}
