package me.arianb.storm_robot

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class RestartableJob(
    private val coroutineScope: CoroutineScope,
    private val block: suspend CoroutineScope.() -> Unit,
    private val handler: CoroutineExceptionHandler
) {
    val isActive: Boolean get() = job.isActive
    private var job: Job = Job()

    fun start(): Job =
        coroutineScope.launch {
            supervisorScope {
                job = launch(handler, block = block)
            }
        }

    fun restart() {
        coroutineScope.launch {
            stop().join()
            start()
        }
    }

    fun stop(): Job =
        coroutineScope.launch {
            supervisorScope {
                job.cancelAndJoin()
            }
        }
}