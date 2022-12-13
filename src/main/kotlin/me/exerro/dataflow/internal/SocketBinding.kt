package me.exerro.dataflow.internal

import me.exerro.dataflow.InputStreamSocket
import me.exerro.dataflow.OutputStreamSocket
import me.exerro.dataflow.SocketConnection
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/** TODO */
internal class SocketBinding<T> internal constructor(
    val from: OutputStreamSocket<T>,
    val to: InputStreamSocket<T>,
    private val bufferSize: Int,
    private val overflowStrategy: SocketConnection.OverflowStrategy<T>,
    underflowBufferSize: Int,
) {
    suspend fun pull(): T {
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

    fun pullOrNull(): T? =
        lock.withLock {
            if (buffer.isNotEmpty()) {
                buffer.removeAt(0)
            }
            else {
                null
            }
        }

    fun push(value: T) {
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
}
