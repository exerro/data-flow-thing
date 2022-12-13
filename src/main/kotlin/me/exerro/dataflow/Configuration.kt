package me.exerro.dataflow

import kotlinx.coroutines.*

/** TODO */
class Configuration(
    allowUnboundInputs: Boolean = false,
    allowUnboundOutputs: Boolean = true,
    init: context (ConfigurationContext) () -> Unit,
) {
    /** TODO */
    val nodes: Set<Node>

    /** TODO */
    val connections: Set<SocketConnection<*>>

    /** TODO */
    val unboundInputs: List<InputStreamSocket<*>>

    /** TODO */
    val unboundOutputs: List<OutputStreamSocket<*>>

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

    init {
        val connections = mutableListOf<SocketConnection<*>>()

        with (object: ConfigurationContext {
            override fun <T> OutputStreamSocket<T>.connectsTo(input: InputStreamSocket<T>): SocketConnection<T> {
                val connection = SocketConnection(this, input, input.parallelConsumers)
                addConnection(connection)
                input.setConnection(connection)
                connections += connection
                return connection
            }
        }, init)

        nodes = connections
            .flatMap { listOf(it.from.node, it.to.node) }
            .toSet()

        val allInputs = nodes.flatMap { it.inputs }
        val allOutputs = nodes.flatMap { it.outputs }

        if (!allowUnboundInputs)
            for (input in allInputs) {
                if (!input.hasConnection())
                    error("TODO: $input")
            }

        if (!allowUnboundOutputs)
            for (output in allOutputs) {
                if (!output.hasConnection())
                    error("TODO")
            }

        unboundInputs = allInputs.filter { !it.hasConnection() }
        unboundOutputs = allOutputs.filter { !it.hasConnection() }
        this.connections = connections.toSet()
    }
}
