package me.exerro.dataflow.nodes.transformers

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node

/** TODO */
class Decode<in T>(
    private val serializer: KSerializer<T>,
    private val decoder: (KSerializer<T>, ByteArray) -> T = { s, b ->
        @OptIn(ExperimentalSerializationApi::class)
        ProtoBuf.decodeFromByteArray(s, b)
    },
): Node() {
    /** TODO */
    val encoded = inputStream<ByteArray>()

    /** TODO */
    val decoded = outputStream<T>()

    ////////////////////////////////////////////////////////////////////////////

    override val inputs = listOf(encoded)
    override val outputs = listOf(decoded)

    context(CoroutineScope)
    override suspend fun start() {
        while (true) {
            decoded.push(decoder(serializer, encoded.pull()))
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        setMetadata(MetadataKey.Label, "Decode")
    }
}
