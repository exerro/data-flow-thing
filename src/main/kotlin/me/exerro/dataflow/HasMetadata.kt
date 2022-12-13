package me.exerro.dataflow

/** TODO */
interface HasMetadata {
    /** TODO */
    infix fun <T> hasMetadata(key: MetadataKey<T>): Boolean

    /** TODO */
    infix fun <T> getMetadataOrThrow(key: MetadataKey<T>): T

    /** TODO */
    fun <T> setMetadata(key: MetadataKey<T>, value: T)

    /** TODO */
    fun <T> removeMetadata(key: MetadataKey<T>)

    /**
     * Clones every metadata entry from this into [target].
     */
    fun cloneMetadata(target: HasMetadata)

    ////////////////////////////////////////////////////////////

    /** TODO */
    infix fun <T> getMetadataOrNull(key: MetadataKey<T>): T? {
        if (!hasMetadata(key))
            return null
        return getMetadataOrThrow(key)
    }

    /** TODO */
    fun <T> getMetadataOrElse(key: MetadataKey<T>, fn: () -> T): T {
        if (!hasMetadata(key))
            return fn()
        return getMetadataOrThrow(key)
    }
}
