package me.exerro.dataflow.nodes.producers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.exerro.dataflow.Node
import me.exerro.dataflow.OutputStreamSocket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/** TODO */
class UdpReceive(
    private val port: Int,
    private val address: InetAddress? = null,
    private val bufferSize: Int = 4096,
): Node() {
    val output = outputStream<DatagramPacket>()

    ////////////////////////////////////////////////////////////////////////////

    override val outputs: List<OutputStreamSocket<*>>
        get() = super.outputs

    context(CoroutineScope)
    override suspend fun start() {
        withContext(Dispatchers.IO) {
            socket = when (address != null) {
                true -> DatagramSocket(port, address)
                else -> DatagramSocket(port)
            }
        }

        val buffer = ByteArray(bufferSize)

        while (true) {
            val packet = DatagramPacket(buffer, buffer.size)

            withContext(Dispatchers.IO) {
                socket.receive(packet)
            }

            output.push(packet)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private lateinit var socket: DatagramSocket
}
