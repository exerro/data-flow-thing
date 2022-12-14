package me.exerro.dataflow

import kotlinx.coroutines.CoroutineScope
import me.exerro.dataflow.internal.MetadataManager

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
// TODO: add protections from having a node in multiple configurations
abstract class Node: HasMetadata by MetadataManager() {
    /** Every input belonging to this node. */
    open val inputs: List<InputStreamSocket<*>>

    /** Every output belonging to this node. */
    open val outputs: List<OutputStreamSocket<*>>

    /**
     * Describe this node using its label or a generated string if there is no
     * label.
     */
    fun describe() = when {
        hasMetadata(MetadataKey.Label) -> getMetadataOrThrow(MetadataKey.Label)
        else -> "Node(${inputs.size} inputs, ${outputs.size} outputs)"
    }

    ////////////////////////////////////////////////////////////////////////////

    /** Run the behaviour of this node. */
    context (CoroutineScope)
    protected abstract suspend fun start()

    /** TODO */
    protected fun <T> inputStream(
        parallelConsumers: Int = 1,
    ): InputStreamSocket<T> {
        val socket = InputStreamSocket<T>(this, socketId++, parallelConsumers)
        privateInputs += socket
        return socket
    }

    /**
     * Similar to [inputStream] but returns an [InputValueSocket] instead. As a
     * result, the socket returned will have a value when asked, and this node
     * will not be [started][start] until the resultant socket has received a
     * value.
     *
     * @see inputStream
     */
    protected fun <T> inputValue(
        parallelConsumers: Int = 1,
    ): InputValueSocket<T> {
        val socket = InputValueSocket<T>(this, socketId++, parallelConsumers)
        privateInputs += socket
        return socket
    }

    /** TODO */
    protected fun <T> outputStream(): OutputStreamSocket<T> {
        val socket = OutputStreamSocket<T>(this, socketId++)
        privateOutputs += socket
        return socket
    }

    ////////////////////////////////////////////////////////////////////////////

    override fun toString() =
        describe()

    ////////////////////////////////////////////////////////////////////////////

    context (CoroutineScope)
    internal suspend fun internalStart() {
        for (input in inputs) {
            if (input !is InputValueSocket<*>)
                continue

            input.pull()
        }

        start()
    }

    ////////////////////////////////////////////////////////////////////////////

    private var socketId = 0

    // note: we separate this from the inputs/outputs so that the types of the
    //       public members are immutable but the types of the private members
    //       are mutable
    private val privateInputs = mutableListOf<InputStreamSocket<*>>()
    private val privateOutputs = mutableListOf<OutputStreamSocket<*>>()

    init {
        @Suppress("LeakingThis")
        inputs = privateInputs
        @Suppress("LeakingThis")
        outputs = privateOutputs
    }
}
