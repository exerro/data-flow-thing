package me.exerro.dataflow

/** TODO */
interface ConfigurationContext {
    /** TODO */
    infix fun <T> OutputStreamSocket<T>.connectsTo(
        input: InputStreamSocket<T>
    ): SocketConnection<T>
}
