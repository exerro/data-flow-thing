package me.exerro.dataflow

import kotlinx.serialization.builtins.serializer
import me.exerro.dataflow.nodes.transformers.Transform
import me.exerro.dataflow.nodes.aggregators.Aggregate
import me.exerro.dataflow.nodes.aggregators.AggregateUpdateMode
import me.exerro.dataflow.nodes.consumers.Consume
import me.exerro.dataflow.nodes.consumers.UdpSend
import me.exerro.dataflow.nodes.producers.Produce
import me.exerro.dataflow.nodes.producers.UdpReceive
import me.exerro.dataflow.nodes.transformers.Decode
import me.exerro.dataflow.nodes.transformers.Encode
import me.exerro.dataflow.nodes.transformers.UdpUnpack
import java.net.InetAddress
import kotlin.time.Duration.Companion.milliseconds

object IncrementAllNumbers: ConfigurationTransformer {
    context(ConfigurationContext)
    override fun transform(configuration: Configuration) {
        for (connection in configuration.connections.filterIsSocketType<String>()) {
            if (!(connection hasMetadata SplitUdp))
                continue

            val sendEncodeNode = Encode(String.serializer())
            sendEncodeNode.label = "UDP Encode"
            sendEncodeNode.input.label = "in"

            val sendNode = UdpSend(port  = 1234, address = InetAddress.getLocalHost())
            sendNode.label = "UDP Send"

            val receiveNode = UdpReceive(port = 1234)
            receiveNode.label = "UDP Receive"

            val receiveUnpackNode = UdpUnpack()
            receiveUnpackNode.label = "UDP Unpack"

            val receiveDecodeNode = Decode(String.serializer())
            receiveDecodeNode.label = "UDP Decode"
            receiveDecodeNode.output.label = "out"

            disconnect(connection)
            connection.from connectsTo sendEncodeNode.input
            sendEncodeNode.encoded connectsTo sendNode.input
            sendNode virtuallyConnectsTo receiveNode withLabel "udp"
            receiveNode.output connectsTo receiveUnpackNode.packets
            receiveUnpackNode.data connectsTo receiveDecodeNode.encoded
            val c2 = receiveDecodeNode.output connectsTo connection.to

            connection.cloneMetadata(c2)
        }
    }
}

object SplitUdp: MetadataKey<Unit>("split-udp", Unit.serializer())

fun main() {
    val config = Configuration(allowUnboundInputs = true) {
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
        (p2.output connectsTo rev.input withLabel "s").setMetadata(SplitUdp, Unit)
        rev.output connectsTo agg.inputs[1] withLabel "reverse(s)"
        agg.output connectsTo end.input
    }

    println(config.asGraphvizString())
    config.startSync()
}
