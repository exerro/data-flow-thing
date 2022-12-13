package me.exerro.dataflow

import kotlin.reflect.KProperty

operator fun <N: HasMetadata> N.provideDelegate(thisRef: Any?, prop: KProperty<*>) =
    lazy { withMetadata(MetadataKey.Label, prop.name) }

////////////////////////////////////////////////////////////////

/** TODO */
fun <N: HasMetadata, T> N.withMetadata(key: MetadataKey<T>, value: T): N {
    setMetadata(key, value)
    return this
}

////////////////////////////////////////////////////////////////

/** TODO */
infix fun <N: HasMetadata> N.withLabel(label: String) =
    withMetadata(MetadataKey.Label, label)

// TODO: generalise this into a delegate!
/** TODO */
var HasMetadata.label: String?
    get() = getMetadataOrNull(MetadataKey.Label)
    set(value) = when (value) {
        null -> removeMetadata(MetadataKey.Label)
        else -> setMetadata(MetadataKey.Label, value)
    }
