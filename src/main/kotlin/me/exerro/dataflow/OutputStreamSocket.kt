package me.exerro.dataflow

/** TODO */
class OutputStreamSocket<T> internal constructor(
    override val node: Node,
    override val id: Int,
): Socket {
/** TODO */
    fun push(value: T) {
        TODO()
    }
}
