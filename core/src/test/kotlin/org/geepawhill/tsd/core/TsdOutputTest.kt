package org.geepawhill.tsd.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class IllegalKeyException(key:String, message:String) : RuntimeException("'$key' is not a legal tsd key, (${message}).")
class UnknownKeyException(key:String) : RuntimeException("`$key` not found in TSD")

class TsdOutput {

    val soFar = mutableMapOf<String,String>()

    operator fun set(key:String,value:String) {
        checkSetKey(key)
        soFar[key] = value
    }

    operator fun <T> set(key:String,value:T) {
        set(key,value.toString())
    }

    operator fun <T> set(key:String,collection:Collection<T>) {
        var index = 0
        collection.forEach {
            set("$key[${index++}]",it)
        }
    }

    operator fun get(key:String):String {
        return soFar.getOrElse(key) { throw UnknownKeyException(key)}
    }

    private fun checkSetKey(key:String) {
        if(key.contains(".")) throw IllegalKeyException(key,"contains period")
        if(key.isBlank()) throw IllegalKeyException(key,"is blank")
        if(soFar[key]!=null) throw IllegalKeyException(key,"is already assigned")
    }
}

class TsdOutputTest {
    private val output = TsdOutput()

    @Test
    fun `accepts string value`() {
        output["key"] = "value"
        assertThat(output["key"]).isEqualTo("value")
    }

    @Test
    fun `triangulate accepts string value`() {
        output["key"] = "abcd"
        assertThat(output["key"]).isEqualTo("abcd")
    }

    @Test
    fun `key get must have value`() {
        assertThrows<UnknownKeyException> {
            output["key"]
        }

    }

    @Test
    fun `accepts generic toStringables`() {
        output["key"] = 3
        assertThat(output["key"].toInt()).isEqualTo(3)
    }

    @Test
    fun `accepts multiple keys`() {
        output["key1"] = "abcd"
        output["key2"] = "efgh"
        assertThat(output["key1"]).isEqualTo("abcd")
        assertThat(output["key2"]).isEqualTo("efgh")
    }

    @Test
    fun `accepts lists`() {
        val list = listOf("a","b","c")
        output["key"] = list
        assertThat(output["key[0]"]).isEqualTo("a")
        assertThat(output["key[1]"]).isEqualTo("b")
        assertThat(output["key[2]"]).isEqualTo("c")
    }

    @Test
    fun `forbids reassigning keys`() {
        assertThrows<IllegalKeyException> {
            output["key"] = "abcd"
            output["key"] = "efgh"
        }
    }


    @Test
    fun `forbids periods in key`() {
        assertThrows<IllegalKeyException> {
            output["key."] = "value"
        }
    }

    @Test
    fun `forbids blank keys`() {
        assertThrows<IllegalKeyException> {
            output["    "] = "value"
        }
    }

    @Test
    fun `forbids empty keys`() {
        assertThrows<IllegalKeyException> {
            output[""] = "value"
        }
    }

}