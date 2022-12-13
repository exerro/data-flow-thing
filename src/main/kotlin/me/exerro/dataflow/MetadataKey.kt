package me.exerro.dataflow

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer

/** TODO */
abstract class MetadataKey<T>(
    /** TODO */
    val name: String,
    val serializer: KSerializer<T>,
    val appliesToNodes: Boolean = true,
    val appliesToConnections: Boolean = false,
) {
    /** TODO */
    object Label: MetadataKey<String>(
        name = "label",
        serializer = String.serializer(),
    )

    ////////////////////////////////////////////////////////////////////////////

    final override fun toString() = "MetadataKey '${this::class.qualifiedName ?: name}'"
}
