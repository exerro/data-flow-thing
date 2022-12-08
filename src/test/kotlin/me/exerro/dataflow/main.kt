package me.exerro.dataflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class ProducerNode<T>(
    private val timeout: Long,
    private val values: List<T>,
): Node() {
    val output = outputStream<T>()

    override fun describe() =
        "Producer of $values"

    context (CoroutineScope)
    override suspend fun start() {
        for (value in values) {
            delay(timeout)
            output.push(value)
        }
    }
}

class MiddleNode: Node() {
    val input1 = inputStream<Int>()
    val input2 = inputStream<String>()
    val output = outputStream<String>()

    override fun describe() =
        "Fancy aggregator"

    context (CoroutineScope)
    override suspend fun start() {
        while (true) {
            val number = input1.pull()
            val string = input2.pull()

            output.push("\u001b[36mI got two new values: $number and $string\u001b[0m")
        }
    }
}

class FinalNode: Node() {
    val input = inputStream<String>()

    override fun describe() =
        "Consumer of strings"

    context (CoroutineScope)
    override suspend fun start() {
        while (true) {
            val value = input.pull()
            println("Received: $value")
        }
    }
}

fun main() {
    val config = Configuration {
        val p1 = ProducerNode(1000, listOf(1, 2, 3, 4, 5))
        val p2 = ProducerNode(500, listOf("s1", "s2", "s3", "s4"))
        val middle = MiddleNode()
        val end = FinalNode()

        p1.output connectsTo middle.input1
        p2.output connectsTo middle.input2
        middle.output connectsTo end.input
    }

    config.startSync()
}
