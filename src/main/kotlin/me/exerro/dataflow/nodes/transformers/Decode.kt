package me.exerro.dataflow.nodes.transformers

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.protobuf.ProtoBuf
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.Node

/** TODO */
open class Decode<in T>(
    private val serializer: KSerializer<T>,
    private val decoder: (KSerializer<T>, ByteArray) -> T = { s, b ->
        @OptIn(ExperimentalSerializationApi::class)
        ProtoBuf.decodeFromByteArray(s, b)
    },
): Node() {
    /** TODO */
    val encoded = inputStream<ByteArray>()

    /** TODO */
    val output = outputStream<T>()

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs = listOf(encoded)
    final override val outputs = listOf(output)

    context(CoroutineScope)
    final override suspend fun start() {
        while (true) {
            output.push(decoder(serializer, encoded.pull()))
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Decode")
    }
}
