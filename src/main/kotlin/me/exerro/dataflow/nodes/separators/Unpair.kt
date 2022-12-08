package me.exerro.dataflow.nodes.separators

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.Node

/** TODO */
class Unpair<A, B>(): Node() {
    /** TODO */
    val input = inputStream<Pair<A, B>>()

    /** TODO */
    val output1 = outputStream<A>()

    /** TODO */
    val output2 = outputStream<B>()

    ////////////////////////////////////////////////////////////////////////////

    override val inputs = listOf(input)
    override val outputs = listOf(output1, output2)

    context(CoroutineScope)
    override suspend fun start() {
        while (true) {
            val (a, b) = input.pull()
            output1.push(a)
            output2.push(b)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        setDescription("Unpair")
    }
}
