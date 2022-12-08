package me.exerro.dataflow.nodes.aggregators

import kotlin.time.Duration

/** TODO */
sealed interface AggregateUpdateMode {
    /** TODO */
    object OnAnyChanged: AggregateUpdateMode

    /** TODO */
    object OnAllChanged: AggregateUpdateMode

    /** TODO */
    data class Periodically(val interval: Duration): AggregateUpdateMode
}
