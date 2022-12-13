package me.exerro.dataflow

import me.exerro.dataflow.internal.MetadataManager

/** TODO */
data class VirtualNodeConnection(
    /** TODO */
    val from: Node,
    /** TODO */
    val to: Node,
): HasMetadata by MetadataManager()
