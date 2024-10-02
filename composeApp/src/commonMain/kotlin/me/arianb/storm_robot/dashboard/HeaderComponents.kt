package me.arianb.storm_robot.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import me.arianb.storm_robot.theme.PaddingNormal
import kotlin.time.TimeSource

@Composable
fun StatusText() {
    // FIXME: placeholder
    Text("Status: TODO")
}

@Composable
fun MatchTimer() {
    val timeSource = remember { TimeSource.Monotonic }
    var startTime by remember { mutableStateOf(timeSource.markNow()) }

    var numSecondsPassed by remember {
        mutableStateOf(
            (timeSource.markNow() - startTime).inWholeSeconds
        )
    }

    val timerString by derivedStateOf {
        "${numSecondsPassed / 60}:${(numSecondsPassed % 60).toString().padStart(2, '0')}"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Match Timer: $timerString")
        IconButton(
            onClick = { startTime = timeSource.markNow() },
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null
            )
        }
    }

    // re-evaluate timer every N seconds by simply pausing for N seconds between updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            numSecondsPassed = (timeSource.markNow() - startTime).inWholeSeconds

            // simulate random lag to be sure my code will actually show the real time (it does)
            /*
            if (numSecondsPassed.toInt() == 7) {
                delay(2500)
            }
            */
        }
    }
}

@Composable
fun SettingsButton() {
    var isSettingsDialogOpen by remember { mutableStateOf(false) }
    IconButton(onClick = { isSettingsDialogOpen = true }) {
        Icon(Icons.Default.Settings, null)
    }

    val closeSettingsDialogLambda = remember { { isSettingsDialogOpen = false } }

    if (isSettingsDialogOpen) {
        Dialog(
            onDismissRequest = closeSettingsDialogLambda,
        ) {
            Card(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxWidth()
                    .height(375.dp)
                    .padding(PaddingNormal),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(PaddingNormal),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("aaaaa")
                    Text("aaaaa")
                    Text("aaaaa")
                    Text("aaaaa")
                }
            }
        }
    }
}
