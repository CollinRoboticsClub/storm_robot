package me.arianb.storm_robot.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.arianb.storm_robot.TestThing
import me.arianb.storm_robot.dashboard.cameraFeed.CameraWindow
import me.arianb.storm_robot.theme.PaddingExtraSmall
import me.arianb.storm_robot.theme.PaddingSmall
import me.arianb.storm_robot.theme.Typography


@Composable
fun DashboardScreen(contentPadding: PaddingValues) {
    val dashboardCommonPadding = Modifier.padding(all = PaddingSmall)

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .padding(PaddingExtraSmall)
    ) {
        Header()

        Row(
            Modifier.fillMaxSize()
        ) {
            // Left Column
            Column(
                Modifier.weight(0.25f)
                    .then(dashboardCommonPadding)
            ) {
                // FIXME: placeholder
                Text("some errors or something ig")
            }

            // Middle Column
            Column(
                Modifier.weight(0.5f)
                    .then(dashboardCommonPadding)
            ) {
                CameraWindow()
                TestThing()
            }

            // Right Column
            Column(
                Modifier.weight(0.25f)
                    .then(dashboardCommonPadding)
            ) {
                WeatherStationInfraredMessages()
            }
        }
    }
}

@Composable
fun Header() {
    CompositionLocalProvider(LocalTextStyle provides Typography.headlineMedium) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp, max = 64.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusText()
                MatchTimer()
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsButton()
            }
        }
    }
}
