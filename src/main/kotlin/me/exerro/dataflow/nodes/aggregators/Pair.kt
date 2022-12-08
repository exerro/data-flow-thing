package me.exerro.dataflow.nodes.aggregators

import kotlin.Pair

/** TODO */
class Pair<A, B>(
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
    description: String = "Pair",
): BiAggregate<A, B, Pair<A, B>>(mode = mode, description = description, aggregate = { a, b -> a to b })
