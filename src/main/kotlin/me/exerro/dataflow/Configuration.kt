package me.exerro.dataflow

/** TODO */
class Configuration(
    init: context (ConfigurationContext) () -> Unit,
) {
    /** TODO */
    val nodes: List<Node> get() = TODO()

    /** TODO */
    suspend fun start() {
        TODO()
    }

    /** Non-suspend equivalent of [start]. */
    fun startSync() {
        TODO()
    }
}
