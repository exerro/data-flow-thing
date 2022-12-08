package me.exerro.dataflow.nodes.aggregators

import me.exerro.dataflow.InputValueSocket

/** TODO */
open class Aggregate<T>(
    count: Int,
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
    description: String = "Aggregate($count)",
    private val aggregate: (List<T>) -> T,
): AggregateBase<T>(mode = mode, description = description) {
    /** TODO */
    constructor(
        count: Int,
        mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
        description: String = "Aggregate($count)",
        aggregate: (T, T) -> T,
    ): this(count, mode, description, { items -> items.reduce(aggregate) })

    /** @see Aggregate */
    companion object {
        /** TODO */
        fun <T> sum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
            add: (T, T) -> T,
        ) = Aggregate(count, mode, description = description, add)

        /** TODO */
        fun intSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
        ) = sum<Int>(count, mode, description) { a, b -> a + b }

        /** TODO */
        fun longSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
        ) = sum<Long>(count, mode, description) { a, b -> a + b }

        /** TODO */
        fun unsignedIntSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
        ) = sum<UInt>(count, mode, description) { a, b -> a + b }

        /** TODO */
        fun unsignedLongSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
        ) = sum<ULong>(count, mode, description) { a, b -> a + b }

        /** TODO */
        fun floatSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
        ) = sum<Float>(count, mode, description) { a, b -> a + b }

        /** TODO */
        fun doubleSum(
            count: Int,
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = "Sum($count)",
        ) = sum<Double>(count, mode, description) { a, b -> a + b }

        /** TODO */
        fun concatenate(
            count: Int,
            separator: String = "",
            mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
            description: String = when (separator) {
                "" -> "Concatenate($count)"
                else -> "Concatenate($count, $separator)"
            },
        ) = Aggregate(count, mode, description) { items -> items.joinToString(separator) }
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
    }
}
