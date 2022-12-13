package me.exerro.dataflow.nodes.aggregators

import me.exerro.dataflow.InputValueSocket
import me.exerro.dataflow.MetadataKey
import me.exerro.dataflow.withMetadata

/** TODO */
open class Aggregate<T>(
    count: Int,
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
    private val aggregate: (List<T>) -> T,
): AggregateBase<T>(mode = mode) {
    /** TODO */
    constructor(
        count: Int,
        mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        aggregate: (T, T) -> T,
    ): this(count, mode, { items -> items.reduce(aggregate) })

    /** @see Aggregate */
    companion object {
        /** TODO */
        fun <T> sum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            add: (T, T) -> T,
        ) = Aggregate(count, mode, add).withMetadata(MetadataKey.Label, "Sum($count)")

        /** TODO */
        fun intSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = sum<Int>(count, mode) { a, b -> a + b }

        /** TODO */
        fun longSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = sum<Long>(count, mode) { a, b -> a + b }

        /** TODO */
        fun unsignedIntSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = sum<UInt>(count, mode) { a, b -> a + b }

        /** TODO */
        fun unsignedLongSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = sum<ULong>(count, mode) { a, b -> a + b }

        /** TODO */
        fun floatSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = sum<Float>(count, mode) { a, b -> a + b }

        /** TODO */
        fun doubleSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = sum<Double>(count, mode) { a, b -> a + b }

        /** TODO */
        fun concatenate(
            count: Int,
            separator: String = "",
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        ) = Aggregate(count, mode) { items -> items.joinToString(separator) }
            .withMetadata(MetadataKey.Label, when (separator) {
                ""   -> "Concatenate($count)"
                else -> "Concatenate($count, $separator)"
            })
    }

    ////////////////////////////////////////////////////////////////////////////

    final override val inputs: List<InputValueSocket<T>>

    final override suspend fun emit() {
        output.push(aggregate(inputs.map { it.latestValue }))
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        require(count > 0)
        inputs = (0 until count).map { inputValue() }

        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Aggregate($count)")
    }
}
