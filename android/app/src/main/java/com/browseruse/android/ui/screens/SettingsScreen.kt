package com.browseruse.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseruse.android.model.AppUiState
import com.browseruse.android.ui.components.TopBar
import com.browseruse.android.ui.theme.Border
import com.browseruse.android.ui.theme.Surface
import com.browseruse.android.ui.theme.TextMuted
import com.browseruse.android.ui.theme.TextPrimary
import com.browseruse.android.ui.theme.TextSecondary
import com.browseruse.android.vm.MainViewModel

@Composable
fun SettingsScreen(state: AppUiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Settings")
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { SectionTitle("Model") }
            item { ModelRow("BU Mini", "Fast and inexpensive", "bu-mini", state.settings.model == "bu-mini", vm) }
            item { ModelRow("BU Max", "Best for complex tasks", "bu-max", state.settings.model == "bu-max", vm) }

            item { Spacer(Modifier.height(8.dp)); SectionTitle("Agent") }
            item { SwitchRow("Vision", "Send annotated screenshots to the model", state.settings.vision) { vm.setVision(it) } }
            item { SwitchRow("Flash mode", "Skip evaluation and next-goal fields", state.settings.flash) { vm.setFlash(it) } }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Max steps", fontSize = 14.sp, color = TextPrimary)
                        Text("Upper bound for a run", fontSize = 11.sp, color = TextMuted)
                    }
                    TextButton(onClick = { vm.setMaxSteps(state.settings.maxSteps - 5) }) { Text("-") }
                    Text("${state.settings.maxSteps}", fontSize = 14.sp, color = TextPrimary)
                    TextButton(onClick = { vm.setMaxSteps(state.settings.maxSteps + 5) }) { Text("+") }
                }
            }

            item { Spacer(Modifier.height(8.dp)); SectionTitle("Connection") }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Backend", fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                        Text(
                            if (state.backendOnline) "online" else "demo fallback",
                            fontSize = 11.sp,
                            color = if (state.backendOnline) com.browseruse.android.ui.theme.Success else TextMuted
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.settings.backendUrl,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Start backend/ with ./gradlew run, or engine/ with uvicorn. If unreachable, the app runs a deterministic demo agent.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextMuted
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)); SectionTitle("About") }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Text("browser-use android · 0.1.0", fontSize = 13.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "An Android client and cloud-runtime scaffold for browser-use. UI and behaviour are based on the MIT-licensed browser-use project and its chat-ui-example.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("MIT licensed · not affiliated with Browser Use", fontSize = 10.sp, color = TextMuted)
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text.uppercase(), fontSize = 10.sp, color = TextMuted, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
}

@Composable
private fun ModelRow(title: String, subtitle: String, value: String, selected: Boolean, vm: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(10.dp))
            .clickable { vm.setModel(value) }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = { vm.setModel(value) })
        Spacer(Modifier.width(4.dp))
        Column {
            Text(title, fontSize = 14.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
