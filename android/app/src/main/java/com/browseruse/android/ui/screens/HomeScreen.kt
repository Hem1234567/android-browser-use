package com.browseruse.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseruse.android.model.AppSettings
import com.browseruse.android.model.AppUiState
import com.browseruse.android.model.Screen
import com.browseruse.android.model.SessionStatus
import com.browseruse.android.model.SessionUi
import com.browseruse.android.ui.components.Composer
import com.browseruse.android.ui.components.SuggestionChip
import com.browseruse.android.ui.theme.AccentAmber
import com.browseruse.android.ui.theme.AccentBlue
import com.browseruse.android.ui.theme.AccentGreen
import com.browseruse.android.ui.theme.AccentPurple
import com.browseruse.android.ui.theme.Border
import com.browseruse.android.ui.theme.Success
import com.browseruse.android.ui.theme.Surface
import com.browseruse.android.ui.theme.SurfaceAlt
import com.browseruse.android.ui.theme.TextMuted
import com.browseruse.android.ui.theme.TextPrimary
import com.browseruse.android.ui.theme.TextSecondary
import com.browseruse.android.vm.MainViewModel

private data class Option(val label: String, val value: String?)

private val SUGGESTIONS = listOf(
    "Find the top 3 articles on Hacker News right now",
    "Search for flights from SF to NYC next weekend",
    "Go to GitHub trending and summarize the top repos",
    "Find a recipe for chocolate chip cookies"
)

@Composable
fun HomeScreen(state: AppUiState, vm: MainViewModel) {
    var input by rememberSaveable { mutableStateOf("") }
    var dialog by remember { mutableStateOf<String?>(null) }
    val settings = state.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("browser use", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.backendOnline) {
                    Text("online", color = Success, fontSize = 11.sp)
                    Spacer(Modifier.width(8.dp))
                }
                IconButton(onClick = { vm.navigate(Screen.Settings) }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(52.dp))
        Text(
            "What should the agent do?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "It can browse the web, interact with pages, and report back.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Composer(
            value = input,
            onValueChange = { input = it },
            onSend = {
                if (input.isNotBlank()) {
                    vm.startTask(input)
                    input = ""
                }
            },
            busy = false,
            onStop = {},
            placeholder = "Describe a task..."
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingIcon(Icons.Filled.Star, "Model", AccentAmber, true) { dialog = "model" }
                SettingIcon(Icons.Filled.Person, "Profile", AccentGreen, settings.profileId != null) { dialog = "profile" }
                SettingIcon(Icons.Filled.Home, "Workspace", AccentPurple, settings.workspaceId != null) { dialog = "workspace" }
                SettingIcon(Icons.Filled.Search, "Proxy", AccentBlue, settings.proxyCountryCode != null) { dialog = "proxy" }
            }
        }

        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SUGGESTIONS.forEach { suggestion ->
                SuggestionChip(suggestion) { vm.startTask(suggestion) }
            }
        }

        if (state.history.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Text("Recent", fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            state.history.take(3).forEach { session ->
                RecentRow(session) { vm.open(session) }
            }
        }
        Spacer(Modifier.height(28.dp))
    }

    if (dialog != null) {
        val (title, options) = dialogContent(dialog!!)
        AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(title) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    applyOption(dialog!!, option.value, settings, vm)
                                    dialog = null
                                }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected(dialog!!, option.value, settings),
                                onClick = {
                                    applyOption(dialog!!, option.value, settings, vm)
                                    dialog = null
                                }
                            )
                            Text(option.label, fontSize = 14.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { dialog = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SettingIcon(
    icon: ImageVector,
    label: String,
    tint: Color,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(if (active) tint.copy(alpha = 0.12f) else Color.Transparent, RoundedCornerShape(8.dp))
            .border(1.dp, if (active) Color.Transparent else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = if (active) tint else TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun RecentRow(session: SessionUi, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(statusColor(session.status), RoundedCornerShape(50))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            session.task,
            fontSize = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(session.status.name.lowercase(), fontSize = 10.sp, color = TextMuted)
    }
}

private fun statusColor(status: SessionStatus): Color = when (status) {
    SessionStatus.Done -> Success
    SessionStatus.Running -> AccentAmber
    SessionStatus.Error -> Color(0xFFEF4444)
    SessionStatus.Stopped -> TextMuted
    SessionStatus.Idle -> SurfaceAlt
}

private fun dialogContent(kind: String): Pair<String, List<Option>> = when (kind) {
    "model" -> "Model" to listOf(Option("BU Mini — fast, cheap", "bu-mini"), Option("BU Max — complex tasks", "bu-max"))
    "profile" -> "Browser profile" to listOf(Option("No profile", null), Option("Default profile", "default"))
    "workspace" -> "Workspace" to listOf(Option("No workspace", null), Option("Research", "research"))
    else -> "Proxy country" to listOf(
        Option("No proxy", null),
        Option("United States", "us"),
        Option("India", "in"),
        Option("Germany", "de")
    )
}

private fun applyOption(kind: String, value: String?, settings: AppSettings, vm: MainViewModel) {
    when (kind) {
        "model" -> value?.let(vm::setModel)
        "profile" -> vm.updateSettings(settings.copy(profileId = value))
        "workspace" -> vm.updateSettings(settings.copy(workspaceId = value))
        "proxy" -> vm.updateSettings(settings.copy(proxyCountryCode = value))
    }
}

private fun isSelected(kind: String, value: String?, settings: AppSettings): Boolean = when (kind) {
    "model" -> settings.model == value
    "profile" -> settings.profileId == value
    "workspace" -> settings.workspaceId == value
    else -> settings.proxyCountryCode == value
}
