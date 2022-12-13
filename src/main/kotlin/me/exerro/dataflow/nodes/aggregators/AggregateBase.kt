package me.exerro.dataflow.nodes.aggregators

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.exerro.dataflow.Node
import me.exerro.dataflow.OutputStreamSocket
import kotlin.time.Duration

/** TODO */
abstract class AggregateBase<in T>(
    private val mode: AggregateUpdateMode,
): Node() {
    /** TODO */
    val output by outputStream<T>(suppressLabel = true)

    ////////////////////////////////////////////////////////////////////////////

    /** TODO */
    context (CoroutineScope)
    protected abstract suspend fun emit()

    ////////////////////////////////////////////////////////////////////////////

    final override val outputs: List<OutputStreamSocket<T>> = listOf(output)

    context(CoroutineScope)
    final override suspend fun start() {
        emit()

        when (mode) {
            AggregateUpdateMode.OnAllChanged -> startAll()
            AggregateUpdateMode.OnAnyChanged -> startAny()
            is AggregateUpdateMode.Periodically -> startPeriodic(mode.interval)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    context(CoroutineScope)
    private suspend fun startAll() {
        while (true) {
            for (input in inputs) {
                input.pull()
            }

            emit()
        }
    }

    context(CoroutineScope)
    private suspend fun startAny() {
        val jobs = inputs.map { input ->
            launch {
                while (true) {
                    input.pull()
                    emit()
                }
            }
        }

        for (job in jobs)
            job.join()
    }

    context(CoroutineScope)
    private suspend fun startPeriodic(interval: Duration) {
        while (true) {
            delay(interval)

            for (input in inputs)
                input.pullOrNull()

            emit()
        }
    }
}
