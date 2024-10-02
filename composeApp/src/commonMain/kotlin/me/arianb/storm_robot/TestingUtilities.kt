package me.arianb.storm_robot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import storm_robot.composeapp.generated.resources.Res
import storm_robot.composeapp.generated.resources.compose_multiplatform
import kotlin.time.Duration
import kotlin.time.TimeSource

// TEST: just a little testing button, remove later
@Composable
fun TestThing() {
    var showContent by remember { mutableStateOf(false) }
    val buttonText = remember { "Click me!" }

    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        showContent = !showContent
        coroutineScope.launch {
//            buttonText = networkString()
        }
    }) {
        Text(buttonText)
    }
    AnimatedVisibility(showContent) {
        val greeting = remember { "hello lol" }
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(Res.drawable.compose_multiplatform), null)
            Text("Compose: $greeting")
        }
    }
}

class MeasureCountPerTime(private val interval: Duration) {
    private val timeSource = TimeSource.Monotonic
    private var currentCount = 0
    private var lastCount = 0
    private var lastMark = timeSource.markNow()

    fun check() {
        currentCount++
        if ((lastMark + interval).hasPassedNow()) {
            val difference = currentCount - lastCount

            println("current performance: $difference times per $interval")

            // Reset
            lastCount = currentCount
            lastMark = timeSource.markNow()
        }
    }
}
