package me.exerro.dataflow

import me.exerro.dataflow.internal.MetadataManager
import kotlin.time.Duration

/**
 * Input socket to a [Node], exposing a stream of values being pushed into the
 * socket.
 *
 * @see latestValue
 * @see pull
 * @see pullWithTimeout
 */
open class InputStreamSocket<out T> internal constructor(
    override val node: Node,
    override val id: Int,
    internal val parallelConsumers: Int,
): Socket, HasMetadata by MetadataManager() {
    /**
     * Latest value pulled by the socket, if present, or null otherwise.
     *
     * @see pull
     */
    open val latestValue: T? get() = value

    /**
     * Wait for a new value to be received and return it.
     *
     * @see latestValue
     * @see pullOrNull
     * @see pullWithTimeout
     */
    suspend fun pull(): T {
        val value = connection.pull()
        this.value = value
        return value
    }

    /**
     * TODO
     */
    fun pullOrNull(): T? {
        val value = connection.pullOrNull()
        if (value != null)
            this.value = value
        return value
    }

    /**
     * Wait for a new value to be received within the specified [timeout] and
     * return it. If no value is received within the timeout, `null` is
     * returned.
     *
     * @see latestValue
     * @see pull
     */
    suspend fun pullWithTimeout(timeout: Duration): T? =
        TODO()

    ////////////////////////////////////////////////////////////////////////////

    override fun equals(other: Any?) =
        other is InputStreamSocket<*> && node == other.node && id == other.id

    override fun hashCode() =
        node.hashCode() * 31 + id

    override fun toString() =
        "InputStreamSocket($node, $id)"

    ////////////////////////////////////////////////////////////////////////////

    internal fun hasConnection() =
        ::connection.isInitialized

    internal fun setConnection(connection: SocketConnection<@UnsafeVariance T>) {
        if (hasConnection())
            error("TODO")

        this.connection = connection
    }

    internal var value: @UnsafeVariance T? = null; private set

    ////////////////////////////////////////////////////////////////////////////

    private val metadata = mutableMapOf<MetadataKey<*>, Any?>()
    private lateinit var connection: SocketConnection<T>
}
