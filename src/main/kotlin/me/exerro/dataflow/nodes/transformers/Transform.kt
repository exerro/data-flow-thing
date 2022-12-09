package me.exerro.dataflow.nodes.transformers

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.Node

/** TODO */
open class Transform<out T, in R>(
    private val transform: (T) -> R,
): Node() {
    /** TODO */
    val input by inputStream<T>(suppressName = true)

    /** TODO */
    val output by outputStream<R>(suppressName = true)

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(input)
    final override val outputs = listOf(output)

    context(CoroutineScope)
    final override suspend fun start() {
        while (true) {
            output.push(transform(input.pull()))
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setDescription("Transform")
    }
}
