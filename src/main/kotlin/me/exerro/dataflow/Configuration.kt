package me.exerro.dataflow

import kotlinx.coroutines.*
import me.exerro.dataflow.internal.SocketBinding
import kotlin.reflect.KType

/** TODO */
class Configuration(
    allowUnboundInputs: Boolean = false,
    allowUnboundOutputs: Boolean = true,
    init: context (ConfigurationContext) () -> Unit,
) {
    /** TODO */
    val nodes: Set<Node>
        get() = privateConnections
            .flatMap { listOf(it.from.node, it.to.node) }
            .toSet()

    /** TODO */
    val connections: Set<SocketConnection<*>>
        get() = privateConnections

    /** TODO */
    val unboundInputs: Set<InputStreamSocket<*>>
        get() = nodes
            .flatMap { it.inputs }
            .filter { !it.isBound() }
            .toSet()

    /** TODO */
    val unboundOutputs: Set<OutputStreamSocket<*>>
        get() = nodes
            .flatMap { it.outputs }
            .filter { !it.hasAnyBindings() }
            .toSet()

    /** TODO */
    context (CoroutineScope)
    suspend fun start() {
        val jobs = nodes.map { node ->
            launch {
                node.internalStart()
            }
        }

        for (job in jobs)
            job.join()
    }

    /** Non-suspend equivalent of [start]. */
    fun startSync() {
        @OptIn(DelicateCoroutinesApi::class)
        val job = GlobalScope.launch {
            val jobs = nodes.map { node ->
                launch {
                    node.internalStart()
                }
            }

            for (job in jobs) {
                job.join()
            }
        }

        while (!job.isCompleted) {
            Thread.sleep(100)
        }
    }

    ////////////////////////////////////////////////////////////

    /** TODO */
    fun asGraphvizString(): String {
        val result = StringBuilder()
        var id = 0
        val nodeMap = nodes.associateWith { id++ }

        result.append("digraph G {\n")
        result.append("    rankdir=LR\n")
        result.append("    fontname=\"Helvetica,Arial,sans-serif\"\n")
        result.append("    edge[fontname=\"Helvetica,Arial,sans-serif\"]\n")
        result.append("    node[fontname=\"Helvetica,Arial,sans-serif\"]\n")

        fun addSockets(nodeId: Int, sockets: Iterable<Socket>) {
            for (socket in sockets) {
                val extra = if (socket.hasMetadata(MetadataKey.Label)) {
                    val label = socket.getMetadataOrThrow(MetadataKey.Label)
                    "[width=0 height=0 shape=rect style=rounded label=\"$label\"]"
                }
                else "[width=0.2 shape=point]"

                result.append("            n${nodeId}s${socket.id}$extra\n")
            }
        }

        for ((node, nodeId) in nodeMap) {
            result.append("    subgraph cluster_$nodeId {\n")
            result.append("        label=\"${node.describe()}\"\n")
            result.append("        style=\"rounded,filled\"\n")
            result.append("        fillcolor=\"#efefef\"\n")
            result.append("        margin=4\n")
            result.append("        color=\"#cacaca\"\n")
            result.append("        edge [style=invis]\n")
            result.append("        node [color=\"#cacaca\"]\n")
            result.append("        subgraph cluster_${nodeId}_inputs {\n")
            result.append("            style=invis\n")
            result.append("            label=\"\"\n")

            addSockets(nodeId, node.inputs)

            result.append("        }\n")
            result.append("        subgraph cluster_${nodeId}_outputs {\n")
            result.append("            style=invis\n")
            result.append("            label=\"\"\n")

            addSockets(nodeId, node.outputs)

            result.append("        }\n")

            for (inputSocket in node.inputs) {
                for (outputSocket in node.outputs) {
                    result.append("        n${nodeId}s${inputSocket.id} -> n${nodeId}s${outputSocket.id}\n")
                }
            }

            result.append("    }\n")
        }

        for (connection in connections) {
            val socket1 = connection.from
            val socket2 = connection.to
            val nodeId1 = nodeMap[socket1.node] ?: continue
            val nodeId2 = nodeMap[socket2.node] ?: continue
            result.append("    n${nodeId1}s${socket1.id} -> n${nodeId2}s${socket2.id}\n")
        }

        result.append("}")

        return result.toString()
    }

    ////////////////////////////////////////////////////////////////////////////

    /** TODO */
    private val privateConnections = mutableSetOf<MutableSocketConnection<*>>()

    init {
        val transformers = mutableListOf<ConfigurationTransformer>()
        var hasInit = false
        val context = object: ConfigurationContext {
            override fun <T> connect(
                from: OutputStreamSocket<T>,
                to: InputStreamSocket<T>,
                type: KType,
            ): MutableSocketConnection<T> {
                val connection = MutableSocketConnection(from, to, type)
                privateConnections += connection
                return connection
            }

            override fun disconnect(connection: SocketConnection<*>) {
                privateConnections.remove(connection)
            }

            override fun transform(transformer: ConfigurationTransformer) {
                if (hasInit)
                    error("TODO")

                transformers += transformer
            }
        }

        with (context, init)
        hasInit = true

        for (transformer in transformers) {
            with(context) { transformer.transform(this@Configuration) }
        }

        for (connection in privateConnections) {
            val binding = connection.createBinding(connection.to.parallelConsumers)
            // we restrict the types of the sockets when creating connection
            // models so these casts are fine
            @Suppress("UNCHECKED_CAST")
            connection.from.addBinding(binding as SocketBinding<Nothing>)
            @Suppress("UNCHECKED_CAST")
            connection.to.bind(binding as SocketBinding<Any?>)
        }

        if (!allowUnboundInputs && unboundInputs.isNotEmpty())
            error("TODO: $unboundInputs")

        if (!allowUnboundOutputs && unboundOutputs.isNotEmpty())
            error("TODO: $unboundOutputs")
    }
}
