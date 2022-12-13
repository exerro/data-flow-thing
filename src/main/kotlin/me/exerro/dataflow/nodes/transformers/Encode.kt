package me.exerro.dataflow.nodes.transformers

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node

/** TODO */
open class Encode<out T>(
    private val serializer: KSerializer<T>,
    private val encoder: (KSerializer<T>, T) -> ByteArray = { s, v ->
        @OptIn(ExperimentalSerializationApi::class)
        ProtoBuf.encodeToByteArray(s, v)
    },
): Node() {
    /** TODO */
    val input = inputStream<T>()

    /** TODO */
    val encoded = outputStream<ByteArray>()

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(input)
    final override val outputs = listOf(encoded)

    context(CoroutineScope)
    final override suspend fun start() {
        while (true) {
            encoded.push(encoder(serializer, input.pull()))
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Encode")
    }
}
