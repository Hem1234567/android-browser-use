package com.browseruse.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.browseruse.android.model.Screen
import com.browseruse.android.ui.components.BottomBar
import com.browseruse.android.ui.screens.HomeScreen
import com.browseruse.android.ui.screens.LibraryScreen
import com.browseruse.android.ui.screens.SessionScreen
import com.browseruse.android.ui.screens.SettingsScreen
import com.browseruse.android.ui.theme.Bg
import com.browseruse.android.ui.theme.ErrorRed
import com.browseruse.android.ui.theme.SurfaceAlt
import com.browseruse.android.ui.theme.TextSecondary
import com.browseruse.android.vm.MainViewModel

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val state = vm.state

    BackHandler(enabled = state.showBrowser || state.screen != Screen.Home) {
        if (state.showBrowser) vm.toggleBrowser() else vm.back()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        state.banner?.let { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceAlt)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(
                    "Dismiss",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { vm.clearBanner() }
                )
            }
        }
        Box(Modifier.weight(1f)) {
            when (state.screen) {
                Screen.Home -> HomeScreen(state, vm)
                Screen.Session -> SessionScreen(state, vm)
                Screen.Library -> LibraryScreen(state, vm)
                Screen.Settings -> SettingsScreen(state, vm)
            }
        }
        if (state.screen != Screen.Session) {
            Box(Modifier.navigationBarsPadding()) {
                BottomBar(state.screen, vm::navigate)
            }
        }
    }
}
