package me.exerro.dataflow

/**
 * Input socket to a [Node], exposing a stream of values being pushed into the
 * socket. Differs to an [InputStreamSocket] in that it will always have a value
 * by the time its corresponding node is started.
 *
 * @see latestValue
 * @see pull
 * @see pullWithTimeout
 */
class InputValueSocket<T> internal constructor(
    node: Node,
    id: Int,
): InputStreamSocket<T>(node, id) {
    /**
     * Latest value received by the socket.
     *
     * This should not be used outside a [Node]'s [start][Node.start] function.
     *
     * @see pull
     */
    override val latestValue: T get() = value!!

    override fun toString() =
        "InputValueSocket($node, $id)"
}
