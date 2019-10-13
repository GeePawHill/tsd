package org.geepawhill.tsd.core

interface TsdBuilder {
    fun open(node: String)
    fun leaf(node: String, value: String)
    fun close(node: String)
}