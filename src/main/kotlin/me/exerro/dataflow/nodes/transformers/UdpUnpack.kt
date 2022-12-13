package me.exerro.dataflow.nodes.transformers

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node
import java.net.DatagramPacket

/** TODO */
open class UdpUnpack: Node() {
    /** TODO */
    val packets = inputStream<DatagramPacket>()

    /** TODO */
    val data = outputStream<ByteArray>()

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(packets)
    final override val outputs = listOf(data)

    context(CoroutineScope)
    final override suspend fun start() {
        while (true) {
            val packet = packets.pull()
            val result = ByteArray(packet.length)
            packet.data.copyInto(result, 0, 0, result.size)
            data.push(result)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "UdpUnpack")
    }
}
