package me.exerro.dataflow.nodes.consumers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.exerro.dataflow.Node
import me.exerro.dataflow.OutputStreamSocket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/** TODO */
class UdpSend(
    private val port: Int,
    private val address: InetAddress,
): Node() {
    val input = inputStream<ByteArray>()

    ////////////////////////////////////////////////////////////////////////////

    override val outputs: List<OutputStreamSocket<*>>
        get() = super.outputs

    context(CoroutineScope)
    override suspend fun start() {
        withContext(Dispatchers.IO) {
            socket = DatagramSocket()
        }

        while (true) {
            val value = input.pull()
            val packet = DatagramPacket(value, value.size, address, port)

            withContext(Dispatchers.IO) {
                socket.send(packet)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private lateinit var socket: DatagramSocket
}
