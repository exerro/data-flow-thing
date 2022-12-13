package me.exerro.dataflow

import kotlinx.coroutines.*
import kotlin.reflect.KType

/** TODO */
fun interface ConfigurationTransformer {
    /** TODO */
    context (ConfigurationContext)
    fun transform(configuration: Configuration)
}
