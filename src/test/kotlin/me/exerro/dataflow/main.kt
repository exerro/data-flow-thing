package me.exerro.dataflow

import me.exerro.dataflow.nodes.transformers.Transform
import me.exerro.dataflow.nodes.aggregators.Aggregate
import me.exerro.dataflow.nodes.aggregators.AggregateUpdateMode
import me.exerro.dataflow.nodes.consumers.Consume
import me.exerro.dataflow.nodes.producers.Produce
import kotlin.time.Duration.Companion.milliseconds

object IncrementAllNumbers: ConfigurationTransformer {
    context(ConfigurationContext)
    override fun transform(configuration: Configuration) {
        for (connection in configuration.connections.filterIsSocketType<Int>()) {
            val newNode = Transform<Int, Int> { it + 1 }
            newNode.label = "Inserted incrementer"
            newNode.input.label = "in"
            newNode.output.label = "out"

            disconnect(connection)
            connection.from connectsTo newNode.input
            newNode.output connectsTo connection.to
        }
    }
}

fun main() {
    val config = Configuration {
        val p1 = Produce(listOf(1, 2, 3, 4, 5))
            .withMetadata(MetadataKey.Label, "1 .. 5")
            .setInterval(1000.milliseconds)
        val p2 = Produce(listOf("s1", "s2", "s3", "s4"))
            .withMetadata(MetadataKey.Label, "'s1' .. 's4'")
            .setDelay(400.milliseconds)
            .setInterval(600.milliseconds)
        val inc by Transform<Int, Int> { it + 1 }
//            .withMetadata(MetadataKey.Label, "Increment")
        val rev = Transform<String, String> { it.reversed() }
            .withMetadata(MetadataKey.Label, "Reverse")
        val p1s = Transform<Int, String>(transform = Any::toString)
            .withMetadata(MetadataKey.Label, "ToString")
        val updateMode = AggregateUpdateMode.Periodically(500.milliseconds)
        val agg = Aggregate<String>(2, mode = updateMode) { (a, b) ->
            "\u001b[36mI got two new values: $a and $b\u001b[0m"
        }
        val end = Consume<String> { println("Received: $it") }
            .withMetadata(MetadataKey.Label, "Print")

        transform(IncrementAllNumbers)

        inc.input.label = "x"
        inc.output.label = "x + 1"

        p1.output connectsTo inc.input
        inc.output connectsTo p1s.input
        p1s.output connectsTo agg.inputs[0] withBufferSize 1
        p2.output connectsTo rev.input
        rev.output connectsTo agg.inputs[1]
        agg.output connectsTo end.input
    }

    println(config.asGraphvizString())
    config.startSync()
}
