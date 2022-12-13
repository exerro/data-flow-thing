package me.exerro.dataflow

import me.exerro.dataflow.internal.SocketBinding
import kotlin.reflect.KType

/** TODO */
class MutableSocketConnection<T>(
    from: OutputStreamSocket<T>,
    to: InputStreamSocket<T>,
    type: KType,
): SocketConnection<T>(from, to, type) {
    /** TODO */
    override var bufferSize = DEFAULT_BUFFER_SIZE; private set

    /** TODO */
    override var overflowStrategy: OverflowStrategy<T> = discardOldest; private set

    /** TODO */
    infix fun withBufferSize(size: Int): MutableSocketConnection<T> {
        bufferSize = size
        return this
    }

    /** TODO */
    infix fun withOverflowStrategy(strategy: OverflowStrategy<T>): MutableSocketConnection<T> {
        overflowStrategy = strategy
        return this
    }

    ////////////////////////////////////////////////////////////////////////////

    internal fun createBinding(underflowBufferSize: Int) =
        SocketBinding(from, to, bufferSize, overflowStrategy, underflowBufferSize)
}
