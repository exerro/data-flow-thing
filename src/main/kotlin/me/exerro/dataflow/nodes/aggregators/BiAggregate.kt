package me.exerro.dataflow.nodes.aggregators

import me.exerro.dataflow.MetadataKey

/** TODO */
open class BiAggregate<out T1, out T2, in R>(
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
    private val aggregate: (T1, T2) -> R,
): AggregateBase<R>(mode) {
    /** TODO */
    val first = inputValue<T1>()

    /** TODO */
    val second = inputValue<T2>()

    ////////////////////////////////////////////////////////////////////////////

    final override suspend fun emit() {
        output.push(aggregate(first.latestValue, second.latestValue))
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "BiAggregate")
    }
}
