package me.exerro.dataflow.nodes.aggregators

import me.exerro.dataflow.InputValueSocket

/** TODO */
open class Collect<T>(
    count: Int,
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
    description: String = "Collect($count)",
): AggregateBase<List<T>>(mode = mode, description = description) {
    final override val inputs: List<InputValueSocket<T>>

    final override suspend fun emit() {
        output.push(inputs.map { it.latestValue })
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        require(count > 0)
        inputs = (0 until count).map { inputValue() }
    }
}
