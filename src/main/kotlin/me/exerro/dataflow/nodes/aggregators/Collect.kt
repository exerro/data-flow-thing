package me.exerro.dataflow.nodes.aggregators

import me.exerro.dataflow.InputValueSocket
import me.exerro.dataflow.MetadataKey

/** TODO */
open class Collect<T>(
    count: Int,
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
): AggregateBase<List<T>>(mode = mode) {
    final override val inputs: List<InputValueSocket<T>>

    final override suspend fun emit() {
        output.push(inputs.map { it.latestValue })
    }

    ////////////////////////////////////////////////////////////////////////////

    init {
        require(count > 0)
        inputs = (0 until count).map { inputValue() }

        @Suppress("LeakingThis")
        setMetadata(MetadataKey.Label, "Collect($count)")
    }
}
