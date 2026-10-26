package com.browseruse.android.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.browseruse.android.data.LiveClient
import com.browseruse.android.model.AppUiState
import com.browseruse.android.model.SessionStatus
import com.browseruse.android.model.SessionUi
import com.browseruse.android.ui.components.ActionPill
import com.browseruse.android.ui.components.Composer
import com.browseruse.android.ui.components.StepSection
import com.browseruse.android.ui.components.TopBar
import com.browseruse.android.ui.theme.AccentAmber
import com.browseruse.android.ui.theme.Border
import com.browseruse.android.ui.theme.ErrorRed
import com.browseruse.android.ui.theme.Success
import com.browseruse.android.ui.theme.SurfaceAlt
import com.browseruse.android.ui.theme.TextMuted
import com.browseruse.android.ui.theme.TextPrimary
import com.browseruse.android.ui.theme.TextSecondary
import com.browseruse.android.vm.MainViewModel

@Composable
fun SessionScreen(state: AppUiState, vm: MainViewModel) {
    val session = state.current ?: return
    if (state.showBrowser) {
        BrowserPanel(session, vm)
        return
    }

    var input by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = session.task.take(42),
            onBack = { vm.goHome() },
            trailing = {
                IconButton(onClick = { vm.toggleBrowser() }) {
                    Icon(Icons.Filled.Search, contentDescription = "Open browser", tint = TextSecondary)
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { UserBubble(session.task) }
            items(session.steps, key = { "${session.id}-${it.index}" }) { step ->
                StepSection(step)
            }
            session.finalAnswer?.let { answer ->
                item { FinalAnswer(answer) }
            }
            if (state.busy) {
                item { ThinkingRow() }
            }
            if (session.status == SessionStatus.Done) {
                item { ArtifactNote(session) }
            }
            session.error?.let { error ->
                item {
                    Text(error, color = ErrorRed, fontSize = 13.sp)
                }
            }
        }
        Box(
            Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Composer(
                value = input,
                onValueChange = { input = it },
                onSend = {
                    if (input.isNotBlank()) {
                        vm.startTask(input)
                        input = ""
                    }
                },
                busy = state.busy,
                onStop = { vm.stop() },
                placeholder = if (session.status == SessionStatus.Running) "Send a follow-up..." else "Ask a new task..."
            )
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            color = SurfaceAlt,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Text(text, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        }
    }
}

@Composable
private fun FinalAnswer(text: String) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Result", fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
    }
}

@Composable
private fun ThinkingRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = AccentAmber
        )
        Spacer(Modifier.width(8.dp))
        Text("Thinking...", fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun ArtifactNote(session: SessionUi) {
    val files = session.steps.flatMap { it.actions }.filter { it.name == "write_file" }.map { it.value }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceAlt, RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Artifact", fontSize = 11.sp, color = TextMuted)
        Spacer(Modifier.width(10.dp))
        Text(
            files.firstOrNull() ?: "results",
            fontSize = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text("demo", fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun BrowserPanel(session: SessionUi, vm: MainViewModel) {
    val url = vm.liveUrl()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf(if (url == null) "demo" else "connecting") }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val fallback = remember(session.id) { placeholderBitmap(session) }

    DisposableEffect(url) {
        if (url.isNullOrBlank()) {
            status = "demo"
            onDispose { }
        } else {
            val client = LiveClient(
                onFrame = { frame -> handler.post { bitmap = frame } },
                onStatus = { label -> handler.post { status = label } }
            )
            client.connect(url)
            onDispose { client.close() }
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        TopBar(
            title = "Agent's Browser",
            onBack = { vm.toggleBrowser() },
            trailing = { Text(status, fontSize = 10.sp, color = TextMuted) }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", color = TextSecondary, fontSize = 16.sp, modifier = Modifier.padding(6.dp))
            Text("→", color = TextMuted, fontSize = 16.sp, modifier = Modifier.padding(6.dp))
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Reload",
                tint = TextMuted,
                modifier = Modifier.size(16.dp).padding(horizontal = 2.dp)
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                color = SurfaceAlt,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    session.steps.lastOrNull()?.url ?: "about:blank",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabChip("browser-use", true)
            Spacer(Modifier.width(6.dp))
            TabChip("+", false)
        }
        Image(
            bitmap = (bitmap ?: fallback).asImageBitmap(),
            contentDescription = "Live browser",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(7.dp).background(if (status == "live") Success else AccentAmber, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                if (status == "live") "Live" else "Demo live view (no CDP stream)",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { status = "you control" }) {
                Text("Take over", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun TabChip(text: String, active: Boolean) {
    Row(
        modifier = Modifier
            .background(if (active) SurfaceAlt else Color.Transparent, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 11.sp, color = if (active) TextPrimary else TextMuted, maxLines = 1)
        if (active) {
            Spacer(Modifier.width(8.dp))
            Text("×", fontSize = 11.sp, color = TextMuted)
        }
    }
}

private fun placeholderBitmap(session: SessionUi): Bitmap {
    val width = 1280
    val height = 720
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.shader = LinearGradient(
        0f, 0f, width.toFloat(), height.toFloat(),
        android.graphics.Color.rgb(39, 39, 42),
        android.graphics.Color.rgb(9, 9, 11),
        Shader.TileMode.CLAMP
    )
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    paint.shader = null
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 44f
    paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    canvas.drawText("Cloud browser", 80f, 320f, paint)
    paint.typeface = Typeface.SANS_SERIF
    paint.color = android.graphics.Color.rgb(161, 161, 170)
    paint.textSize = 24f
    canvas.drawText("Agent is driving a real Chromium in the cloud", 80f, 370f, paint)
    paint.textSize = 20f
    canvas.drawText("Demo frame - start the backend and engine for a live CDP stream", 80f, 410f, paint)
    paint.color = android.graphics.Color.rgb(113, 113, 122)
    paint.textSize = 18f
    canvas.drawText(session.task.take(90), 80f, 640f, paint)
    return bitmap
}
