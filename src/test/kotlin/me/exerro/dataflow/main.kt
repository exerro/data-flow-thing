package me.exerro.dataflow

class ProducerNode<T>(
    private val values: List<T>,
): Node() {
    constructor(vararg values: T): this(values.toList())

    val output = outputStream<T>()

    override suspend fun start() {
        for (value in values) {
            output.push(value)
        }
    }
}

class MiddleNode: Node() {
    val input1 = inputStream<Int>()
    val input2 = inputStream<String>()
    val output = outputStream<String>()

    override suspend fun start() {
        while (true) {
            val number = input1.pull()
            val string = input2.pull()

            output.push("I got two new values: $number and $string")
        }
    }
}

class FinalNode: Node() {
    val input = inputStream<String>()

    override suspend fun start() {
        while (true) {
            val value = input.pull()
            println("Received: $value")
        }
    }
}

fun main() {
    val config = Configuration {
        val p1 = ProducerNode(1, 2, 3, 4, 5)
        val p2 = ProducerNode("s1", "s2", "s3", "s4")
        val middle = MiddleNode()
        val end = FinalNode()

        p1.output connectsTo middle.input1
        p2.output connectsTo middle.input2
        middle.output connectsTo end.input
    }

    config.startSync()
}
