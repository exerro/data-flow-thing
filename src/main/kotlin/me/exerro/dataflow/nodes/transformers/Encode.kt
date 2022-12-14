package me.exerro.dataflow.nodes.transformers

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node

/** TODO */
class Encode<out T>(
    private val serializer: KSerializer<T>,
    private val encoder: (KSerializer<T>, T) -> ByteArray = { s, v ->
        @OptIn(ExperimentalSerializationApi::class)
        ProtoBuf.encodeToByteArray(s, v)
    },
): Node() {
    /** TODO */
    val decoded = inputStream<T>()

    /** TODO */
    val encoded = outputStream<ByteArray>()

    ////////////////////////////////////////////////////////////////////////////

    override val inputs = listOf(decoded)
    override val outputs = listOf(encoded)

    context(CoroutineScope)
    override suspend fun start() {
        while (true) {
            encoded.push(encoder(serializer, decoded.pull()))
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        setMetadata(MetadataKey.Label, "Encode")
    }
}
