package me.exerro.dataflow

/** TODO */
class OutputStreamSocket<in T> internal constructor(
    override val node: Node,
    override val id: Int,
    name: String?,
): Socket {
    /** TODO */
    fun push(value: T) {
        for (connection in connections)
            connection.push(value)
    }

    ////////////////////////////////////////////////////////////////////////////

    override var name: String? = name; private set

    override fun setName(name: String?): OutputStreamSocket<T> {
        this.name = name
        return this
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
