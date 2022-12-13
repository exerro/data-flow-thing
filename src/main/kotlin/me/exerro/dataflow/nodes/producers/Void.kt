package me.exerro.dataflow.nodes.producers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node
import kotlin.time.Duration

/** TODO */
open class Void<in T>: Node() {
    /** TODO */
    val output = outputStream<T>()

    ////////////////////////////////////////////////////////////////////////////

    final override val outputs = listOf(output)

    context(CoroutineScope)
    final override suspend fun start() {
        // do nothing
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Void")
    }
}
