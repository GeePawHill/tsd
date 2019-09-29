package org.geepawhill.tsd.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class IllegalKeyException(key:String, message:String) : RuntimeException("'$key' is not a legal tsd key, (${message}).")

class TsdOutput {

    val soFar = mutableMapOf<String,String>()

    operator fun set(key:String,value:String) {
        checkSetKey(key)
        soFar[key] = value
    }

    operator fun get(key:String):String {
        return soFar[key]!!
    }

    private fun checkSetKey(key:String) {
        if(key.contains(".")) throw IllegalKeyException(key,"contains period")
        if(key.isBlank()) throw IllegalKeyException(key,"is blank")
        if(soFar[key]!=null) throw IllegalKeyException(key,"is already assigned")
    }
}

class TsdOutputTest {
    @Test
    fun `accepts string value`() {
        val output = TsdOutput()
        output["key"] = "value"
        assertThat(output["key"]).isEqualTo("value")
    }

    @Test
    fun `triangulate accepts string value`() {
        val output = TsdOutput()
        output["key"] = "abcd"
        assertThat(output["key"]).isEqualTo("abcd")
    }

    @Test
    fun `accepts multiple keys`() {
        val output = TsdOutput()
        output["key1"] = "abcd"
        output["key2"] = "efgh"
        assertThat(output["key1"]).isEqualTo("abcd")
        assertThat(output["key2"]).isEqualTo("efgh")
    }

    @Test
    fun `forbids reassigning keys`() {
        val output = TsdOutput()
        assertThrows<IllegalKeyException> {
            output["key"] = "abcd"
            output["key"] = "efgh"
        }
    }


    @Test
    fun `forbids periods in key`() {
        assertThrows<IllegalKeyException> {
            val output = TsdOutput()
            output["key."] = "value"
        }
    }

    @Test
    fun `forbids blank keys`() {
        assertThrows<IllegalKeyException> {
            val output = TsdOutput()
            output["    "] = "value"
        }
    }

    @Test
    fun `forbids empty keys`() {
        assertThrows<IllegalKeyException> {
            val output = TsdOutput()
            output[""] = "value"
        }
    }

}