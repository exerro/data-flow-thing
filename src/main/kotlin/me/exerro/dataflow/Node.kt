package me.exerro.dataflow

/**
 * A [Node] is a unit of behaviour or functionality with a fixed set of
 * [Sockets][Socket].
 *
 * A [Node] has well-typed and static inputs and outputs in the form of
 * [InputStreamSockets][InputStreamSocket] and
 * [OutputStreamSockets][OutputStreamSocket].
 *
 * A [Node] has a single [start] method which should asynchronously run all its
 * behaviour.
 *
 * @see inputStream
 * @see inputValue
 * @see outputStream
 */
abstract class Node {
    /** Every input belonging to this node. */
    val inputs: List<InputStreamSocket<*>>

    /** Every output belonging to this node. */
    val outputs: List<OutputStreamSocket<*>>

    /** Run the behaviour of this node. */
    abstract suspend fun start()

    ////////////////////////////////////////////////////////////////////////////

    /** TODO */
    protected fun <T> inputStream(): InputStreamSocket<T> =
        TODO()

    /** TODO */
    protected fun <T> inputValue(): InputValueSocket<T> =
        TODO()

    /** TODO */
    protected fun <T> outputStream(): OutputStreamSocket<T> =
        TODO()

    ////////////////////////////////////////////////////////////////////////////

    // note: we separate this from the inputs/outputs so that the types of the
    //       public members are immutable but the types of the private members
    //       are mutable
    private val privateInputs = mutableListOf<InputStreamSocket<*>>()
    private val privateOutputs = mutableListOf<OutputStreamSocket<*>>()

    init {
        inputs = privateInputs
        outputs = privateOutputs
    }
}
