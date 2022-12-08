package me.exerro.dataflow

/** TODO */
class OutputStreamSocket<in T> internal constructor(
    override val node: Node,
    override val id: Int,
): Socket {
    /** TODO */
    fun push(value: T) {
        for (connection in connections)
            connection.push(value)
    }

    ////////////////////////////////////////////////////////////////////////////

    internal fun hasConnection() =
        connections.isNotEmpty()

    internal fun addConnection(connection: SocketConnection<@UnsafeVariance T>) {
        connections += connection
    }

    ////////////////////////////////////////////////////////////////////////////

    private val connections = mutableListOf<SocketConnection<T>>()
}
