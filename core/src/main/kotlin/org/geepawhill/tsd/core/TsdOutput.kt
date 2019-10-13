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
        val opens = mutableListOf<String>()
        keyToLeaf.entries.toList().sortedBy { it.key }.forEach {
            val nodes = it.key.split(".")
            val parents = nodes.dropLast(1)
            val firstDifference = findFirstDifference(opens, parents)
            closeOpens(builder, opens, firstDifference)
            if (firstDifference != -1) {
                for (toOpen in firstDifference until parents.size) {
                    builder.open(parents[toOpen])
                    opens.add(parents[toOpen])
                }
            }
            builder.leaf(nodes.last(), it.value)
        }
        if (opens.isNotEmpty()) closeOpens(builder, opens, 0)
    }

    private fun closeOpens(builder: TsdBuilder, opens: MutableList<String>, firstDifference: Int) {
        if (firstDifference == -1) return
        var toRemove = opens.size - 1
        while (toRemove >= firstDifference) {
            builder.close(opens[toRemove])
            opens.removeAt(toRemove)
            toRemove -= 1
        }
    }

    private fun findFirstDifference(opens: MutableList<String>, parents: List<String>): Int {
        for (nodeIndex in 0 until opens.size) {
            if (nodeIndex < parents.size) {
                if (parents[nodeIndex] != opens[nodeIndex]) return nodeIndex
            }
        }
        if (parents.size > opens.size) return 0
        return -1
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