package me.exerro.dataflow

/**
 * An input to/output from a [Node]. Exposes the [node] it belongs to and a
 * unique [id] for that socket.
 */
interface Socket {
    /** Node that this socket belongs to. */
    val node: Node

    /**
     * Unique ID of this socket within the node. Note that the [id] is unique
     * amongst all sockets of the node, not just inputs/outputs depending on
     * what this socket is.
     */
    val id: Int
}
