package com.browseruse.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseruse.android.model.ActionStatus
import com.browseruse.android.model.Screen
import com.browseruse.android.model.StepStatus
import com.browseruse.android.model.UiAction
import com.browseruse.android.model.UiStep
import com.browseruse.android.ui.theme.AccentAmber
import com.browseruse.android.ui.theme.Bg
import com.browseruse.android.ui.theme.Border
import com.browseruse.android.ui.theme.ErrorRed
import com.browseruse.android.ui.theme.Success
import com.browseruse.android.ui.theme.Surface as SurfaceColor
import com.browseruse.android.ui.theme.SurfaceAlt
import com.browseruse.android.ui.theme.TextMuted
import com.browseruse.android.ui.theme.TextPrimary
import com.browseruse.android.ui.theme.TextSecondary

@Composable
fun TopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (onBack == null) 10.dp else 0.dp)
        )
        trailing()
    }
}

@Composable
fun BottomBar(current: Screen, onNavigate: (Screen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(SurfaceColor)
            .border(1.dp, Border),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomItem("Home", Icons.Filled.Home, current == Screen.Home) { onNavigate(Screen.Home) }
        BottomItem("Library", Icons.Filled.List, current == Screen.Library) { onNavigate(Screen.Library) }
        BottomItem("Settings", Icons.Filled.Settings, current == Screen.Settings) { onNavigate(Screen.Settings) }
    }
}

@Composable
private fun BottomItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) TextPrimary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 11.sp, color = if (selected) TextPrimary else TextMuted)
    }
}

@Composable
fun Composer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    busy: Boolean,
    onStop: () -> Unit,
    placeholder: String,
    footer: (@Composable RowScope.() -> Unit)? = null
) {
    Surface(
        color = SurfaceColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp, lineHeight = 22.sp),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 24.dp, max = 140.dp),
                    decorationBox = { inner ->
                        Box {
                            if (value.isEmpty()) {
                                Text(placeholder, color = TextMuted, fontSize = 15.sp)
                            }
                            inner()
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                if (busy) {
                    CircleAction(Icons.Filled.Clear, "Stop", ErrorRed, true, onStop)
                } else {
                    CircleAction(Icons.Filled.Send, "Send", Color.White, value.isNotBlank(), onSend)
                }
            }
            if (footer != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    content = footer
                )
            }
        }
    }
}

@Composable
fun CircleAction(
    icon: ImageVector,
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(if (enabled) color else color.copy(alpha = 0.3f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (color == Color.White) Color.Black else Color.White,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun SuggestionChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(SurfaceColor, RoundedCornerShape(50))
            .border(1.dp, Border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(text, fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun StepSection(step: UiStep) {
    var expanded by remember(step.index) { mutableStateOf(true) }
    val color = when (step.status) {
        StepStatus.Done -> Success
        StepStatus.Error -> ErrorRed
        StepStatus.Running -> AccentAmber
        StepStatus.Pending -> TextMuted
    }
    val symbol = when (step.status) {
        StepStatus.Done -> "✓"
        StepStatus.Error -> "×"
        StepStatus.Running -> "•"
        StepStatus.Pending -> "○"
    }
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(symbol, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(step.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Text(if (expanded) "▾" else "▸", color = TextMuted, fontSize = 12.sp)
        }
        if (expanded) {
            Column(Modifier.padding(start = 26.dp)) {
                step.thinking?.takeIf { it.isNotBlank() }?.let { LabeledText("Thinking", it) }
                step.evaluation?.takeIf { it.isNotBlank() }?.let { LabeledText("Evaluation", it) }
                step.memory?.takeIf { it.isNotBlank() }?.let { LabeledText("Memory", it) }
                step.nextGoal?.takeIf { it.isNotBlank() }?.let { LabeledText("Next goal", it) }
                step.actions.forEach { ActionPill(it) }
            }
        }
    }
}

@Composable
private fun LabeledText(label: String, text: String) {
    Column(Modifier.padding(vertical = 2.dp)) {
        Text(label.uppercase(), fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
        Text(text, fontSize = 13.sp, lineHeight = 19.sp, color = TextSecondary, maxLines = 8, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ActionPill(action: UiAction) {
    var expanded by remember(action.name, action.status) { mutableStateOf(false) }
    val tint = when (action.status) {
        ActionStatus.Done -> Success
        ActionStatus.Error -> ErrorRed
        ActionStatus.Running -> AccentAmber
        ActionStatus.Pending -> TextMuted
    }
    Column(Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceAlt, RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).background(tint, CircleShape))
            Spacer(Modifier.width(7.dp))
            Text(action.label, fontSize = 13.sp, color = TextSecondary)
            if (action.value.isNotBlank()) {
                Spacer(Modifier.width(6.dp))
                Text(
                    action.value,
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (action.status == ActionStatus.Done) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Success, modifier = Modifier.size(12.dp))
            }
        }
        if (expanded && !action.result.isNullOrBlank()) {
            Text(
                action.result,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun EmptyState(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}
