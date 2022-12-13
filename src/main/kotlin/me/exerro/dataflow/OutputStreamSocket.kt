package me.exerro.dataflow

import me.exerro.dataflow.internal.MetadataManager
import me.exerro.dataflow.internal.SocketBinding

/** TODO */
class OutputStreamSocket<in T> internal constructor(
    override val node: Node,
    override val id: Int,
): Socket, HasMetadata by MetadataManager() {
    /** TODO */
    fun push(value: T) {
        for (connection in bindings)
            connection.push(value)
    }

    ////////////////////////////////////////////////////////////////////////////

    internal fun hasAnyBindings() =
        bindings.isNotEmpty()

    internal fun addBinding(binding: SocketBinding<@UnsafeVariance T>) {
        bindings += binding
    }

    ////////////////////////////////////////////////////////////////////////////

    private val bindings = mutableListOf<SocketBinding<T>>()
}
