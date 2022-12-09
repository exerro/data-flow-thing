package me.exerro.dataflow.nodes.aggregators

import kotlin.Pair

/** TODO */
class Pair<A, B>(
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
): BiAggregate<A, B, Pair<A, B>>(
    mode = mode,
    aggregate = { a, b -> a to b }
) {
    init {
        setDescription("Pair")
    }
}
