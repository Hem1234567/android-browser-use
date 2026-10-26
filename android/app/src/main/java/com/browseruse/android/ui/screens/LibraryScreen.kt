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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseruse.android.model.AppUiState
import com.browseruse.android.model.SessionStatus
import com.browseruse.android.model.SessionUi
import com.browseruse.android.ui.components.EmptyState
import com.browseruse.android.ui.components.TopBar
import com.browseruse.android.ui.theme.AccentAmber
import com.browseruse.android.ui.theme.Border
import com.browseruse.android.ui.theme.Success
import com.browseruse.android.ui.theme.Surface
import com.browseruse.android.ui.theme.TextMuted
import com.browseruse.android.ui.theme.TextSecondary
import com.browseruse.android.vm.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LibraryScreen(state: AppUiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Library")
        if (state.history.isEmpty()) {
            EmptyState("No runs yet", "Start a task from Home. Completed runs appear here.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.history, key = { it.id }) { session ->
                    LibraryRow(session) { vm.open(session) }
                }
            }
        }
    }
}

@Composable
private fun LibraryRow(session: SessionUi, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(statusColor(session.status), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                session.task,
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(session.status.name.lowercase(), fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.width(10.dp))
            Text(session.model, fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.width(10.dp))
            Text("${session.steps.size} steps", fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.weight(1f))
            Text(formatTime(session.startedAt), fontSize = 11.sp, color = TextMuted)
        }
    }
}

private fun statusColor(status: SessionStatus) = when (status) {
    SessionStatus.Done -> Success
    SessionStatus.Running -> AccentAmber
    SessionStatus.Error -> com.browseruse.android.ui.theme.ErrorRed
    SessionStatus.Stopped, SessionStatus.Idle -> TextMuted
}

private fun formatTime(epochMs: Long): String =
    SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(epochMs))
