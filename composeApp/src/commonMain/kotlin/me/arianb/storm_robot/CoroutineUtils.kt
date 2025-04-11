package me.arianb.storm_robot

import arrow.resilience.Schedule
import arrow.resilience.retry
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
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

class ResilientService<T> {
    constructor(coroutineScope: CoroutineScope, flow: Flow<T>, block: suspend (T) -> Unit) {
        coroutineScope.launch {
            flow.collectLatest { value ->
                schedule<Throwable>().retry {
                    block(value)
                }
            }
        }
    }

    //    fun restart() {
//        coroutineScope.launch {
//            stop().join()
//            start()
//        }
//    }
//
//    fun stop(): Job =
//        coroutineScope.launch {
//            supervisorScope {
//                job.cancelAndJoin()
//            }
//        }
    companion object {
        private inline fun <reified E : Throwable> schedule() = Schedule.forever<E>().log { t, retryCount: Long ->
            println("throwable: $t, retry #$retryCount")
        }
    }
}
