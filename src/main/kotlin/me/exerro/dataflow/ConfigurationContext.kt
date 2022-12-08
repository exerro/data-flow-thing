package me.exerro.dataflow

/** TODO */
interface ConfigurationContext {
    /** TODO */
    infix fun <T> OutputStreamSocket<T>.connectsTo(
        input: InputStreamSocket<T>,
    ): SocketConnection<T>

    infix fun <T> List<OutputStreamSocket<T>>.connectsTo(
        inputs: List<InputStreamSocket<T>>,
    ): List<SocketConnection<T>> {
        require(size == inputs.size)
        return zip(inputs).map { (a, b) -> a connectsTo b }
    }
}
