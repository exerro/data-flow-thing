package me.exerro.dataflow.nodes.consumers

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node

/** TODO */
open class Discard<out T>: Node() {
    /** TODO */
    val input = inputStream<T>()

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(input)

    context(CoroutineScope)
    final override suspend fun start() {
        while (true)
            input.pull()
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Discard")
    }
}
