package me.exerro.dataflow

import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine

/** TODO */
class Configuration(
    allowUnboundInputs: Boolean = false,
    allowUnboundOutputs: Boolean = true,
    init: context (ConfigurationContext) () -> Unit,
) {
    /** TODO */
    val nodes: Set<Node>

    /** TODO */
    val unboundInputs: List<InputStreamSocket<*>>

    /** TODO */
    val unboundOutputs: List<OutputStreamSocket<*>>

    /** TODO */
    context (CoroutineScope)
    suspend fun start() {
        val jobs = nodes.map { node ->
            launch {
                node.internalStart()
            }
        }

        for (job in jobs)
            job.join()
    }

    /** Non-suspend equivalent of [start]. */
    fun startSync() {
        @OptIn(DelicateCoroutinesApi::class)
        val job = GlobalScope.launch {
            val jobs = nodes.map { node ->
                launch {
                    node.internalStart()
                }
            }

            for (job in jobs) {
                job.join()
            }
        }

        while (!job.isCompleted) {
            Thread.sleep(100)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        val connections = mutableListOf<SocketConnection<*>>()

        with (object: ConfigurationContext {
            override fun <T> OutputStreamSocket<T>.connectsTo(input: InputStreamSocket<T>): SocketConnection<T> {
                val connection = SocketConnection(this, input, input.parallelConsumers)
                addConnection(connection)
                input.setConnection(connection)
                connections += connection
                return connection
            }
        }, init)

        nodes = connections
            .flatMap { listOf(it.from.node, it.to.node) }
            .toSet()

        val allInputs = nodes.flatMap { it.inputs }
        val allOutputs = nodes.flatMap { it.outputs }

        if (!allowUnboundInputs)
            for (input in allInputs) {
                if (!input.hasConnection())
                    error("TODO")
            }

        if (!allowUnboundOutputs)
            for (output in allOutputs) {
                if (!output.hasConnection())
                    error("TODO")
            }

        unboundInputs = allInputs.filter { !it.hasConnection() }
        unboundOutputs = allOutputs.filter { !it.hasConnection() }
    }
}
