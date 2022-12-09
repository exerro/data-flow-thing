package me.exerro.dataflow

import me.exerro.dataflow.SocketConnection.OverflowStrategy
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** TODO */
// TODO: split this into a public interface and an internal Binding type
class SocketConnection<T> internal constructor(
    val from: OutputStreamSocket<T>,
    val to: InputStreamSocket<T>,
    underflowBufferSize: Int,
) {
    /** TODO */
    infix fun withBufferSize(size: Int): SocketConnection<T> {
        bufferSize = size
        return this
    }

    /** TODO */
    infix fun withOverflowStrategy(strategy: OverflowStrategy<T>): SocketConnection<T> {
        overflowStrategy = strategy
        return this
    }

    /**
     * Strategy for handling overflows in the buffer of
     * [SocketConnections][SocketConnection].
     *
     * @see findDiscarded
     */
    fun interface OverflowStrategy<in T> {
        /**
         * Return the index of the value to drop, e.g. 0 would drop the first
         * (oldest) value, and [current].size would drop the [new] value.
         */
        fun findDiscarded(current: List<T>, new: T): Int
    }

    ////////////////////////////////////////////////////////////

    operator fun component1() = from
    operator fun component2() = to

    ////////////////////////////////////////////////////////////

    /** @see SocketConnection */
    companion object {
        /** TODO */
        const val DEFAULT_BUFFER_SIZE = 16

        /** TODO */
        val discardOldest = OverflowStrategy<Any?> { _, _ -> 0 }

        /** TODO */
        val discardNewest = OverflowStrategy<Any?> { current, _ -> current.size }
    }

    ////////////////////////////////////////////////////////////////////////////

    internal suspend fun pull(): T {
        lock.lock()

        return if (buffer.isNotEmpty()) {
            buffer.removeAt(0).also { lock.unlock() }
        }
        else {
            suspendCoroutine { cont ->
                val added = pullBuffer.offer(cont)
                lock.unlock()
                if (!added)
                    error("TODO")
            }
        }
    }

    internal fun pullOrNull(): T? =
        lock.withLock {
            if (buffer.isNotEmpty()) {
                buffer.removeAt(0)
            }
            else {
                null
            }
        }

    internal fun push(value: T) {
        lock.withLock {
            if (pullBuffer.isNotEmpty()) {
                assert(buffer.isEmpty())
                pullBuffer.take().resume(value)
                return
            }

            while (buffer.size >= bufferSize) {
                val index = overflowStrategy.findDiscarded(buffer, value) % (buffer.size + 1)

                if (index < buffer.size)
                    buffer.removeAt(index)
                else
                    return // we've been told to drop the new value so don't add it and stop
            }

            buffer.add(value)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private val lock = ReentrantLock()
    private val buffer = mutableListOf<T>()
    private val pullBuffer = ArrayBlockingQueue<Continuation<T>>(underflowBufferSize)
    private var bufferSize = DEFAULT_BUFFER_SIZE
    private var overflowStrategy: OverflowStrategy<T> = discardOldest
}
