package me.exerro.dataflow

/** TODO */
interface ConfigurationContext {
    // TODO: handle buffering
    /** TODO */
    infix fun <T> OutputStreamSocket<T>.connectsTo(input: InputStreamSocket<T>)
}
