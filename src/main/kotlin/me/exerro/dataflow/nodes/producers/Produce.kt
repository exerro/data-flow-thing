package me.exerro.dataflow.nodes.producers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.exerro.dataflow.Node
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** TODO */
open class Produce<in T>(
    private val items: Iterable<T>,
    private val description: String = "Produce",
    private val init: suspend context (CoroutineScope) () -> Unit = {},
    private val pre: suspend context (CoroutineScope) () -> Unit = {},
    private val post: suspend context (CoroutineScope) () -> Unit = {},
): Node() {
    /** TODO */
    val output = outputStream<T>()

    // TODO: .withDelay() etc?

    /** TODO */
    constructor(
        items: Iterable<T>,
        interval: Duration,
        delay: Duration = 0.milliseconds,
        description: String = "Produce",
    ): this(items, description = description, init = { delay(delay) }, post = { delay(interval) })

    ////////////////////////////////////////////////////////////////////////////

    final override val outputs = listOf(output)

    override fun describe() = description

    context(CoroutineScope)
    final override suspend fun start() {
        init(this@CoroutineScope)
        for (item in items) {
            pre(this@CoroutineScope)
            output.push(item)
            post(this@CoroutineScope)
        }
    }
}
