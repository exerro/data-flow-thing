package me.exerro.dataflow

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.nodes.transformers.Transform
import me.exerro.dataflow.nodes.aggregators.Aggregate
import me.exerro.dataflow.nodes.aggregators.AggregateUpdateMode
import me.exerro.dataflow.nodes.consumers.Consume
import me.exerro.dataflow.nodes.producers.Produce
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    val config = Configuration {
        val p1 = Produce(listOf(1, 2, 3, 4, 5))
            .setInterval(1000.milliseconds)
            .also { it.setDescription("1 .. 5") }
        val p2 = Produce(listOf("s1", "s2", "s3", "s4"))
            .setDelay(400.milliseconds)
            .setInterval(600.milliseconds)
            .also { it.setDescription("'s1' .. 's4'") }
        val inc = Transform<Int, Int> { it + 1 }
            .also { it.setDescription("Increment") }
        val rev = Transform<String, String> { it.reversed() }
            .also { it.setDescription("Reverse") }
        val p1s = Transform<Int, String>(transform = Any::toString)
            .also { it.setDescription("ToString") }
        val updateMode = AggregateUpdateMode.Periodically(500.milliseconds)
        val agg = Aggregate<String>(2, mode = updateMode) { (a, b) ->
            "\u001b[36mI got two new values: $a and $b\u001b[0m"
        }
        val end = Consume<String> {
            println("Received: $it")
        }
            .also { it.setDescription("Print") }

        inc.input.setName("x")
        inc.output.setName("x + 1")

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
