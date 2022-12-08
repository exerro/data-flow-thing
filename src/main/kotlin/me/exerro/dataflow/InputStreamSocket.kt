package me.exerro.dataflow

import kotlin.time.Duration

/**
 * Input socket to a [Node], exposing a stream of values being pushed into the
 * socket.
 *
 * @see latestValue
 * @see pull
 * @see pullWithTimeout
 */
open class InputStreamSocket<T> internal constructor(
    override val node: Node,
    override val id: Int,
): Socket {
    /**
     * Latest value received by the socket, if present, or null otherwise.
     *
     * @see pull
     */
    open val latestValue: T? get() = value

    /**
     * Wait for a new value to be received and return it.
     *
     * @see latestValue
     * @see pullWithTimeout
     */
    suspend fun pull(): T =
        TODO()

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

    internal var value: T? = null
}
