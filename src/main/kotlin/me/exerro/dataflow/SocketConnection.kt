package me.exerro.dataflow

import me.exerro.dataflow.SocketConnection.OverflowStrategy
import me.exerro.dataflow.internal.MetadataManager
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/** TODO */
abstract class SocketConnection<T> internal constructor(
    /** TODO */
    val from: OutputStreamSocket<T>,
    /** TODO */
    val to: InputStreamSocket<T>,
    /** TODO */
    val type: KType,
): HasMetadata by MetadataManager() {
    /** TODO */
    open val bufferSize: Int = DEFAULT_BUFFER_SIZE

    /** TODO */
    open val overflowStrategy: OverflowStrategy<T> = discardOldest

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

    inline fun <reified T> isType() =
        type == typeOf<T>()

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

        /** TODO */
        fun <T> create(
            from: OutputStreamSocket<T>,
            to: InputStreamSocket<T>,
            type: KType,
        ) = MutableSocketConnection(from, to, type)

        /** TODO */
        inline fun <reified T> create(
            from: OutputStreamSocket<T>,
            to: InputStreamSocket<T>,
        ) = MutableSocketConnection(from, to, typeOf<T>())
    }
}
