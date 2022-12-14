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

/** Marks node connections that should be split up across a UDP channel. */
object SplitUdp: MetadataKey<Unit>("split-udp", Unit.serializer())

/**
 * [ConfigurationTransformer] that replaces connections marked as [SplitUdp]
 * with a set of nodes and connections to implement that UDP channel.
 */
object SplitUdpTransform: ConfigurationTransformer {
    // `transform` is called by our Configuration
    context(ConfigurationContext)
    override fun transform(configuration: Configuration) {
        // Iterate through every connection that has a String type
        for (connection in configuration.connections.filterIsSocketType<String>()) {
            // if the connection isn't marked SplitUdp, skip it
            if (!(connection hasMetadata SplitUdp))
                continue

            // Build and connect nodes
            // -> encode -> send -> receive -> unpack -> decode ->
            // Unpack takes a UDP packet and returns just the data

            val sendEncodeNode = Encode(String.serializer())
            sendEncodeNode.label = "UDP Encode"
            sendEncodeNode.decoded.label = "in"

            val sendNode = UdpSend(port  = 1234, address = InetAddress.getLocalHost())
            sendNode.label = "UDP Send"

            val receiveNode = UdpReceive(port = 1234)
            receiveNode.label = "UDP Receive"

            val receiveUnpackNode = UdpUnpack()
            receiveUnpackNode.label = "UDP Unpack"

            val receiveDecodeNode = Decode(String.serializer())
            receiveDecodeNode.label = "UDP Decode"
            receiveDecodeNode.decoded.label = "out"

            // Create the connections
            connection.from connectsTo sendEncodeNode.decoded
            sendEncodeNode.encoded connectsTo sendNode.input
            sendNode virtuallyConnectsTo receiveNode withLabel "udp"
            receiveNode.output connectsTo receiveUnpackNode.packets
            receiveUnpackNode.data connectsTo receiveDecodeNode.encoded
            val c2 = receiveDecodeNode.decoded connectsTo connection.to

            // Remove the old connection, and clone its metadata into the latest
            // connection we made
            disconnect(connection)
            connection.cloneMetadata(c2)
        }
    }
}

fun main() {
    // Create a configuration by specifying all the nodes and connections we
    // want to include.
    val config = Configuration(allowUnboundInputs = true) {
        // Create nodes that do the stuff in isolation.
        // p1 produces the integers 1 to 5 inclusive
        val p1 = Produce(listOf(1, 2, 3, 4, 5))
            // We can assign metadata for arbitrary keys. Here, we're giving the
            // node a label.
            .withMetadata(MetadataKey.Label, "1 .. 5")
            // This is a [Produce] specific function to set the interval between
            // successive items being pushed.
            .setInterval(1000.milliseconds)
        // p2 produces the strings s1, s2, s3, and s4
        val p2 = Produce(listOf("s1", "s2", "s3", "s4"))
            .withMetadata(MetadataKey.Label, "'s1' .. 's4'")
            .setDelay(400.milliseconds)
            .setInterval(600.milliseconds)
        val increment by Transform<Int, Int> { it + 1 }
        //            ^^
        //            using 'by' instead of '=' allows Kotlin to know the name
        //            of the variable we're using, and automatically sets the
        //            label metadata for us (see extensions.kt)
        val reverse by Transform<String, String> { it.reversed() }
        val toString by Transform<Int, String>(transform = Any::toString)
        // agg turns two inputs X and Y into "I got two new values: X and Y"
        val agg = Aggregate<String>(2, mode = AggregateUpdateMode.OnAnyChanged) { (a, b) ->
            "\u001b[36mI got two new values: $a and $b\u001b[0m" }
        val print by Consume<String> { println("Received: $it") }

        // Specify the labels for each end of the increment node explicitly.
        // They default to what the node assigns them, which is nothing.
        increment.input.label = "x"
        increment.output.label = "x + 1"

        // Specify the connections between nodes
        p1.output connectsTo increment.input
        increment.output connectsTo toString.input

        // This is a normal connection, but has a smaller buffer size. When
        // items are being produced quicker than they are consumed, this buffer
        // will quickly overflow, resulting in a configurable overflow strategy
        // being used to drop items from the buffer.
        toString.output connectsTo agg.inputs[0] withBufferSize 1
        //                                       ^^^^^^^^^^^^^^^^ buffer size changed here

        // Connections can be given metadata. In this case, we're tagging the
        // connection with `SplitUdp` so that it's picked up by the
        // SplitUdpTransform [ConfigurationTransformer].
        (p2.output connectsTo reverse.input withLabel "s").setMetadata(SplitUdp)
        //                                                ^^^^^^^^^^^^^^^^^^^^^^

        // We can also assign labels to connections.
        reverse.output connectsTo agg.inputs[1] withLabel "reverse(s)"
        //                                      ^^^^^^^^^^^^^^^^^^^^^^

        agg.output connectsTo print.input

        // Transform the configuration according to [SplitUdpTransform]. In this
        // case, connections marked as [SplitUdp] are broken up into a pipeline
        // that sends messages over UDP.
        transform(SplitUdpTransform)
    }

    // Print the GraphViz visualisation of our configuration.
    println(config.asGraphvizString())

    // Start running the configuration.
    config.startSync()
}
