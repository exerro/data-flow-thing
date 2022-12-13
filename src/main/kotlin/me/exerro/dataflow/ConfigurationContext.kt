package me.exerro.dataflow

import kotlin.reflect.KType

/** TODO */
interface ConfigurationContext {
    /** TODO */
    fun <T> connect(
        from: OutputStreamSocket<T>,
        to: InputStreamSocket<T>,
        type: KType,
    ): MutableSocketConnection<T>

    /** TODO */
    fun connectVirtual(
        from: Node,
        to: Node,
    ): VirtualNodeConnection

    /** TODO */
    fun <T> connect(
        outputs: List<OutputStreamSocket<T>>,
        inputs: List<InputStreamSocket<T>>,
        type: KType,
    ): List<MutableSocketConnection<T>> {
        require(outputs.size == inputs.size)
        return outputs.zip(inputs).map { (a, b) -> connect(a, b, type) }
    }

    /** TODO */
    fun disconnect(
        connection: SocketConnection<*>,
    )

    /** TODO */
    fun disconnect(
        connection: VirtualNodeConnection,
    )

    /** TODO */
    fun transform(transformer: ConfigurationTransformer)
}
