package me.exerro.dataflow.nodes.consumers

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node

/** TODO */
open class Consume<out T>(
    private val onItem: suspend context (CoroutineScope) (T) -> Unit,
): Node() {
    /** TODO */
    val input = inputStream<T>()

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(input)

    context(CoroutineScope)
    final override suspend fun start() {
        while (true)
            onItem(this@CoroutineScope, input.pull())
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Consume")
    }
}
