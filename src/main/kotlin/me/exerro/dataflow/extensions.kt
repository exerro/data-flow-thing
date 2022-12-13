package me.exerro.dataflow

/** TODO */
fun <N: HasMetadata, T> N.withMetadata(key: MetadataKey<T>, value: T): N {
    setMetadata(key, value)
    return this
}

// TODO: generalise this into a delegate!
var HasMetadata.label: String?
    get() = getMetadataOrNull(MetadataKey.Label)
    set(value) = when (value) {
        null -> removeMetadata(MetadataKey.Label)
        else -> setMetadata(MetadataKey.Label, value)
    }
