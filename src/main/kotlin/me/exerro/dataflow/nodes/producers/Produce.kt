package me.exerro.dataflow.nodes.producers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import me.exerro.dataflow.Node
import kotlin.time.Duration

/** TODO */
open class Produce<in T>(
    private val items: Iterable<T>,
    private val init: suspend context (CoroutineScope) () -> Unit = {},
    private val pre: suspend context (CoroutineScope) () -> Unit = {},
    private val post: suspend context (CoroutineScope) () -> Unit = {},
): Node() {
    /** TODO */
    val output by outputStream<T>(suppressName = true)

    /** TODO */
    fun setDelay(delay: Duration) =
        Produce(items, init = init + { delay(delay) }, pre, post = post)

    /** TODO */
    fun setInterval(interval: Duration) =
        Produce(items, init, pre, post = post + { delay(interval) })

    ////////////////////////////////////////////////////////////////////////////

    final override val outputs = listOf(output)

    context(CoroutineScope)
    final override suspend fun start() {
        init(this@CoroutineScope)
        for (item in items) {
            pre(this@CoroutineScope)
            output.push(item)
            post(this@CoroutineScope)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private operator fun (suspend context (CoroutineScope) () -> Unit).plus(
        other: suspend context (CoroutineScope) () -> Unit
    ): suspend context (CoroutineScope) () -> Unit = {
        this(getCoroutineScopeFix)
        other(getCoroutineScopeFix)
    }

    /** Needed 'cause the Kotlin compiler is a bit dumb right now :( */
    context (CoroutineScope)
    private val getCoroutineScopeFix get() = this@CoroutineScope

    init {
        @Suppress("LeakingThis")
        setDescription("Produce")
    }
}
