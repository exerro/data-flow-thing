package me.exerro.dataflow.nodes.separators

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.Node

/** TODO */
open class Unpair<A, B>(
    private val description: String = "Unpair",
): Node() {
    /** TODO */
    val input = inputStream<kotlin.Pair<A, B>>()

    /** TODO */
    val output1 = outputStream<A>()

    /** TODO */
    val output2 = outputStream<B>()

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(input)
    final override val outputs = listOf(output1, output2)

    override fun describe() = description

    context(CoroutineScope)
    final override suspend fun start() {
        while (true) {
            val (a, b) = input.pull()
            output1.push(a)
            output2.push(b)
        }
    }
}
