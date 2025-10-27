package me.arianb.storm_robot

import co.touchlab.kermit.Logger
import kotlin.time.Duration
import kotlin.time.TimeSource

class MeasureCountPerTime(private val interval: Duration) {
    private val timeSource = TimeSource.Monotonic
    var currentCount = 0
        private set
    private var lastCount = 0
    private var lastMark = timeSource.markNow()

    fun check() {
        currentCount++
        if ((lastMark + interval).hasPassedNow()) {
            val difference = currentCount - lastCount

            Logger.d("current performance: $difference times per $interval")

            // Reset
            lastCount = currentCount
            lastMark = timeSource.markNow()
        }
    }
}
