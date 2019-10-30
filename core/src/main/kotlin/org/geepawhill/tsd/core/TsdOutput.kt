package org.geepawhill.tsd.core

import java.util.*

class TsdOutput : TsdWriter {
    val prefixes = Stack<String>()
    val keyToLeaf = mutableMapOf<String, String>()

    override operator fun set(key: String, value: String) {
        checkSetKey(key)
        keyToLeaf[addPrefix(key)] = value
    }

    override operator fun <T> set(key: String, value: T) {
        set(key, value.toString())
    }

    override operator fun set(key: String, value: Tsd) {
        checkSetKey(key)
        prefixes.push(key)
        value.tsdPut(this)
        prefixes.pop()
    }

    override operator fun <T> set(key: String, collection: Collection<T>) {
        var index = 0
        collection.forEach {
            set("$key[${index++}]", it)
        }
    }

    override operator fun set(key: String, op: () -> Unit) {
        checkSetKey(key)
        prefixes.push(key)
        op()
        prefixes.pop()
    }

    override fun build(builder: TsdBuilder) {
        val openParents = mutableListOf<String>()
        sortedEntries().forEach {
            val tokens = it.key.split(".")
            val entryParents = tokens.dropLast(1)
            closeFinishedParents(openParents, entryParents, builder)
            openNewParents(entryParents, openParents, builder)
            builder.leaf(tokens.last(), it.value)
        }
        openParents.reversed().forEach { builder.close(it) }
    }

    private fun openNewParents(entryParents: List<String>, openParents: MutableList<String>, builder: TsdBuilder) {
        entryParents.unmatched(openParents).forEach { toOpen ->
            builder.open(toOpen)
            openParents.add(toOpen)
        }
    }

    private fun closeFinishedParents(openParents: MutableList<String>, entryParents: List<String>, builder: TsdBuilder) {
        openParents.unmatched(entryParents).reversed().forEach { toClose ->
            builder.close(toClose)
            openParents.removeAt(openParents.size - 1)
        }
    }

    private fun sortedEntries() = keyToLeaf.entries.toList().sortedBy { it.key }

    private fun List<String>.unmatched(other: List<String>): List<String> {
        for (index in 0 until size) {
            if (index >= other.size || get(index) != other[index]) {
                return drop(index)
            }
        }
        return emptyList()
    }

    operator fun get(key: String): String {
        return keyToLeaf.getOrElse(key) { throw TsdWriter.UnknownKeyException(key) }
    }

    fun dump() {
        for (entry in keyToLeaf.entries) {
            println("${entry.key}=${entry.value}")
        }
    }

    private fun checkSetKey(key: String) {
        if (key.contains(".")) throw TsdWriter.IllegalKeyException(key, "contains period")
        if (key.isBlank()) throw TsdWriter.IllegalKeyException(key, "is blank")
        if (keyToLeaf[key] != null) throw TsdWriter.IllegalKeyException(key, "is already assigned")
    }

    private fun addPrefix(key: String) = if (!prefixes.empty()) prefixes.joinToString(".") + "." + key else key

}