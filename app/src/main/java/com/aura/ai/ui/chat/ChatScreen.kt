package com.aura.ai.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.ui.theme.MintAccent
import com.aura.ai.ui.theme.RoseError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(state.turns.size, state.turns.lastOrNull()?.text) {
        if (state.turns.isNotEmpty()) listState.animateScrollToItem(state.turns.lastIndex)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text("T1000", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = state.modelName?.let { "on-device · $it" } ?: "no model connected",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.modelReady) MintAccent else RoseError,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        )

        state.banner?.let { banner ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    banner,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        if (state.turns.isEmpty()) {
            EmptyState(Modifier.weight(1f))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.turns, key = { it.id }) { turn -> MessageBubble(turn) }
            }
        }

        Composer(
            input = state.input,
            busy = state.busy,
            listening = state.listening,
            speaking = state.speaking,
            partial = state.partialSpeech,
            enabled = state.modelReady,
            onInput = viewModel::onInputChange,
            onSend = { viewModel.send() },
            onMic = viewModel::toggleListening,
            onStopSpeak = viewModel::stopSpeaking,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("T1000", style = MaterialTheme.typography.headlineLarge, color = MintAccent)
            Spacer(Modifier.height(8.dp))
            Text(
                "A fully autonomous, on-device agent. Ask it anything — it will understand, plan, act, and verify its own work before answering.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageBubble(turn: ChatTurn) {
    val alignment = if (turn.isUser) Alignment.End else Alignment.Start
    Column(Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        if (!turn.isUser && turn.trace.isNotEmpty()) {
            AgentTrace(turn.trace, Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
        val bubbleColor = when {
            turn.isUser -> MaterialTheme.colorScheme.primary
            turn.error -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
            else -> MaterialTheme.colorScheme.surface
        }
        val textColor = when {
            turn.isUser -> MaterialTheme.colorScheme.onPrimary
            turn.error -> RoseError
            else -> MaterialTheme.colorScheme.onSurface
        }
        if (turn.text.isNotBlank() || !turn.isUser) {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.widthIn(max = 560.dp),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (turn.text.isBlank() && turn.streaming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MintAccent,
                        )
                    } else {
                        Text(turn.text, style = MaterialTheme.typography.bodyLarge, color = textColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun Composer(
    input: String,
    busy: Boolean,
    listening: Boolean,
    speaking: Boolean,
    partial: String,
    enabled: Boolean,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onStopSpeak: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            if (listening && partial.isNotBlank()) {
                Text(
                    partial,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MintAccent,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = onInput,
                    modifier = Modifier.weight(1f),
                    enabled = enabled && !busy,
                    placeholder = {
                        Text(if (listening) "Listening…" else "Message T1000")
                    },
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                )

                Spacer(Modifier.size(8.dp))

                val micTint = if (listening) RoseError else MintAccent
                IconButton(onClick = if (speaking) onStopSpeak else onMic, enabled = enabled) {
                    val icon = when {
                        speaking -> Icons.Filled.GraphicEq
                        listening -> Icons.Filled.Stop
                        else -> Icons.Filled.Mic
                    }
                    Icon(icon, contentDescription = if (listening) "Stop listening" else "Speak", tint = micTint)
                }

                IconButton(
                    onClick = onSend,
                    enabled = enabled && !busy && input.isNotBlank(),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (enabled && input.isNotBlank() && !busy) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (enabled && input.isNotBlank() && !busy) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
