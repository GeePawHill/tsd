package org.geepawhill.tsd.core

interface TsdWriter {
    class IllegalKeyException(key: String, message: String) : RuntimeException("'$key' is not a legal tsd key, (${message}).")
    class UnknownKeyException(key: String) : RuntimeException("`$key` not found in TSD")

    operator fun set(key: String, value: String)
    operator fun <T> set(key: String, value: T)
    operator fun set(key: String, value: Tsd)
    operator fun <T> set(key: String, collection: Collection<T>)
    operator fun set(key: String, op: () -> Unit)
}