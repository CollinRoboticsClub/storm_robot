package me.arianb.storm_robot.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import me.arianb.storm_robot.theme.PaddingNormal

@Composable
fun SettingsDialog(
    onDismissRequest: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.userPreferences.collectAsState()

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier
                .animateContentSize()
                .wrapContentSize()
                .padding(PaddingNormal),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(PaddingNormal),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                var serverIPString by remember { mutableStateOf(settings.serverHost) }
                var serverPort by remember { mutableStateOf(settings.serverPort.toString()) }
                TextField(
                    value = serverIPString,
                    label = { Text("Server IP") },
                    onValueChange = { serverIPString = it },
                )
                TextField(
                    value = serverPort,
                    label = { Text("Server Port") },
                    onValueChange = { serverPort = it },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            with(settingsViewModel) {
                                setServerHost(serverIPString)
                                setServerPort(serverPort.toInt())
                            }
                            onDismissRequest()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
