package me.exerro.dataflow.nodes.separators

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.Node

/** TODO */
class Unpair<A, B>(): Node() {
    /** TODO */
    val input by inputStream<Pair<A, B>>(suppressName = true)

    /** TODO */
    val first by outputStream<A>()

    /** TODO */
    val second by outputStream<B>()

    ////////////////////////////////////////////////////////////////////////////

    override val inputs = listOf(input)
    override val outputs = listOf(first, second)

    context(CoroutineScope)
    override suspend fun start() {
        while (true) {
            val (a, b) = input.pull()
            first.push(a)
            second.push(b)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        setDescription("Unpair")
    }
}
