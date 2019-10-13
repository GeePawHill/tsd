package org.geepawhill.tsd.core

interface TsdBuilder {
    fun open(path: String, node: String)
    fun leaf(path: String, node: String, value: String)
    fun close(path: String, node: String)
}