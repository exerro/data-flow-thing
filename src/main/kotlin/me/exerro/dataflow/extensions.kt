package me.exerro.dataflow

import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

/** TODO */
context (ConfigurationContext)
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Set<SocketConnection<*>>.filterIsSocketType(): Set<SocketConnection<T>> =
    filter { it.isType<T>() } .toSet() as Set<SocketConnection<T>>

////////////////////////////////////////////////////////////////

/** TODO */
context (ConfigurationContext)
inline infix fun <reified T> OutputStreamSocket<T>.connectsTo(
    input: InputStreamSocket<T>,
) = connect(from = this, input, typeOf<T>())

/** TODO */
context (ConfigurationContext)
inline infix fun <reified T> List<OutputStreamSocket<T>>.connectsTo(
    input: List<InputStreamSocket<T>>,
) = connect(outputs = this, input, typeOf<T>())

////////////////////////////////////////////////////////////////

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
