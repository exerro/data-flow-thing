package me.exerro.dataflow

import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KProperty

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
abstract class Node {
    /** Every input belonging to this node. */
    open val inputs: List<InputStreamSocket<*>>

    /** Every output belonging to this node. */
    open val outputs: List<OutputStreamSocket<*>>

    /** TODO */
    open fun setDescription(description: String): Node {
        this.description = description
        return this
    }

    /** TODO */
    open fun describe() = when (::description.isInitialized) {
        true -> description
        else -> "Node(${inputs.size} inputs, ${outputs.size} outputs)"
    }

    companion object {
        /** TODO */
        // TODO: is this even useful? you won't be able to connect the sockets
        //       nicely
        operator fun invoke(
            init: Node.() -> suspend context (CoroutineScope) () -> Unit
        ) = object: Node() {
            val startFn: suspend (CoroutineScope) -> Unit = init()

            context(CoroutineScope) override suspend fun start() {
                startFn(this@CoroutineScope)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    /** Run the behaviour of this node. */
    context (CoroutineScope)
    protected abstract suspend fun start()

    /** TODO */
    protected fun <T> createInputStream(
        parallelConsumers: Int = 1,
        name: String? = null,
    ): InputStreamSocket<T> {
        val socket = InputStreamSocket<T>(this, socketId++, name, parallelConsumers)
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
    protected fun <T> createInputValue(
        parallelConsumers: Int = 1,
        name: String? = null,
    ): InputValueSocket<T> {
        val socket = InputValueSocket<T>(this, socketId++, name, parallelConsumers)
        privateInputs += socket
        return socket
    }

    /** TODO */
    protected fun <T> createOutputStream(
        name: String? = null,
    ): OutputStreamSocket<T> {
        val socket = OutputStreamSocket<T>(this, socketId++, name)
        privateOutputs += socket
        return socket
    }

    /** TODO */
    protected fun <T> inputStream(
        parallelConsumers: Int = 1,
        suppressName: Boolean = false,
    ) = SocketNameDelegate(suppressName) { name ->
        val socket = InputStreamSocket<T>(this, socketId++, name, parallelConsumers)
        privateInputs += socket
        socket
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
        suppressName: Boolean = false,
    ) = SocketNameDelegate(suppressName) { name ->
        val socket = InputValueSocket<T>(this, socketId++, name, parallelConsumers)
        privateInputs += socket
        socket
    }

    /** TODO */
    protected fun <T> outputStream(
        suppressName: Boolean = false,
    ) = SocketNameDelegate(suppressName) { name ->
        val socket = OutputStreamSocket<T>(this, socketId++, name)
        privateOutputs += socket
        socket
    }

    /** TODO */
    protected class SocketNameDelegate<T: Any>(
        private val suppressName: Boolean,
        private val getValue: (String?) -> T,
    ) {
        operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): SocketDelegate<T> {
            return SocketDelegate(getValue(prop.name.takeIf { !suppressName }))
        }
    }

    /** TODO */
    protected class SocketDelegate<T>(
        private val value: T,
    ) {
        operator fun getValue(thisRef: Any?, prop: KProperty<*>) =
            value
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

    private lateinit var description: String

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
