package me.exerro.dataflow.internal

import me.exerro.dataflow.HasMetadata
import me.exerro.dataflow.MetadataKey

/** TODO */
internal class MetadataManager: HasMetadata {
    override fun <T> hasMetadata(key: MetadataKey<T>) =
        key in metadata

    override fun <T> getMetadataOrThrow(key: MetadataKey<T>): T {
        if (!hasMetadata(key))
            error("TODO")

        // we validate entries in the metadata so this is fine!
        @Suppress("UNCHECKED_CAST")
        return metadata[key] as T
    }

    override fun <T> setMetadata(key: MetadataKey<T>, value: T) {
        metadata[key] = value
    }

    override fun <T> removeMetadata(key: MetadataKey<T>) {
        metadata.remove(key)
    }

    override fun cloneMetadata(target: HasMetadata) {
        @Suppress("UNCHECKED_CAST") // it's fine! see getMetadataOrThrow
        for ((key, value) in metadata)
            target.setMetadata(key as MetadataKey<Any?>, value)
    }

    ////////////////////////////////////////////////////////////////////////////

    private val metadata = mutableMapOf<MetadataKey<*>, Any?>()
}
