package me.exerro.dataflow.nodes.aggregators

/** TODO */
open class BiAggregate<out T1, out T2, in R>(
    mode: AggregateUpdateMode = AggregateUpdateMode.OnAnyChanged,
    description: String = "BiAggregate",
    private val aggregate: (T1, T2) -> R,
): AggregateBase<R>(mode, description) {
    /** TODO */
    val input1 = inputValue<T1>()

    /** TODO */
    val input2 = inputValue<T2>()

    ////////////////////////////////////////////////////////////////////////////

    final override suspend fun emit() {
        output.push(aggregate(input1.latestValue, input2.latestValue))
    }
}
