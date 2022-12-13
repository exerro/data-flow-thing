package me.exerro.dataflow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import me.exerro.dataflow.internal.MetadataManager
import me.exerro.dataflow.internal.SocketBinding
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
        val value = binding.pull()
        this.value = value
        return value
    }

    /**
     * Attempt to pull a value, returning it immediately if one is available, or
     * returning null immediately otherwise.
     *
     * @see latestValue
     * @see pull
     * @see pullWithTimeout
     */
    fun pullOrNull(): T? {
        val value = binding.pullOrNull()
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
    context (CoroutineScope)
    suspend fun pullWithTimeout(timeout: Duration) =
        pullBefore(before = System.nanoTime() + timeout.inWholeNanoseconds)

    ////////////////////////////////////////////////////////////////////////////

    override fun equals(other: Any?) =
        other is InputStreamSocket<*> && node == other.node && id == other.id

    override fun hashCode() =
        node.hashCode() * 31 + id

    override fun toString() =
        "InputStreamSocket($node, $id)"

    ////////////////////////////////////////////////////////////////////////////

    internal fun isBound() =
        ::binding.isInitialized

    internal fun bind(binding: SocketBinding<@UnsafeVariance T>) {
        if (isBound())
            error("TODO")

        this.binding = binding
    }

    internal var value: @UnsafeVariance T? = null; private set

    ////////////////////////////////////////////////////////////////////////////

    context (CoroutineScope)
    private tailrec suspend fun pullBefore(before: Long): T? {
        // if we've hit the time limit, return a value or null, we don't care
        if (System.nanoTime() >= before)
            return pullOrNull()

        return when (val result = pullOrNull()) {
            null -> {
                yield()
                pullBefore(before)
            }
            else -> result
        }
    }

    private val metadata = mutableMapOf<MetadataKey<*>, Any?>()
    private lateinit var binding: SocketBinding<T>
}
