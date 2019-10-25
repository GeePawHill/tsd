package org.geepawhill.tsd.core

import org.assertj.core.api.Assertions.assertThat
import org.geepawhill.tsd.core.TsdWriter.IllegalKeyException
import org.geepawhill.tsd.core.TsdWriter.UnknownKeyException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class NestableTsd(val field1: String, val field2: String) : Tsd {

    override fun tsdPut(output: TsdWriter) {
        output["field1"] = field1
        output["field2"] = field2
    }

}

class TestingTsdBuilder : TsdBuilder {

    val calls = mutableListOf<String>()

    override fun open(node: String) {
        calls += "O-$node"
    }

    override fun leaf(node: String, value: String) {
        calls += "L-$node-$value"
    }

    override fun close(node: String) {
        calls += "C-$node"
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
        val list = listOf("a", "b", "c")
        output["key"] = list
        assertThat(output["key[0]"]).isEqualTo("a")
        assertThat(output["key[1]"]).isEqualTo("b")
        assertThat(output["key[2]"]).isEqualTo("c")
    }

    @Test
    fun `accepts tsds`() {
        output["tsd"] = NestableTsd("value1", "value2")
        assertThat(output["tsd.field1"]).isEqualTo("value1")
        assertThat(output["tsd.field2"]).isEqualTo("value2")
    }

    @Test
    fun `accepts explicit nests`() {
        output["nest"] = {
            output["field1"] = "value1"
            output["field2"] = "value2"
            output["grandchild"] = {
                output["field4"] = "value4"
            }
        }
        assertThat(output["nest.field1"]).isEqualTo("value1")
        assertThat(output["nest.field2"]).isEqualTo("value2")
        assertThat(output["nest.grandchild.field4"]).isEqualTo("value4")
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

    @Test
    fun `empty output doesn't call builder`() {
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).isEmpty()
    }

    @Test
    fun `builds one leaf`() {
        output["leaf"] = "value"
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("L-leaf-value")
    }

    @Test
    fun `builds two leafs`() {
        output["a"] = "value"
        output["b"] = "value"
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("L-a-value", "L-b-value")
    }

    @Test
    fun `builds two leafs in sorted order`() {
        output["b"] = "value"
        output["a"] = "value"
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("L-a-value", "L-b-value")
    }

    @Test
    fun `builds a simple 2-tree`() {
        output["parent"] = {
            output["child"] = "value"
        }
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("O-parent", "L-child-value", "C-parent")
    }

    @Test
    fun `builds a simple 3-tree`() {
        output["parent"] = {
            output["child"] = {
                output["grandchild"] = "value"
            }
        }
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("O-parent", "O-child", "L-grandchild-value", "C-child", "C-parent")
    }

    @Test
    fun `builds a multi-child 2-tree`() {
        output["parent"] = {
            output["child1"] = "value"
            output["child2"] = "value"
        }
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("O-parent", "L-child1-value", "L-child2-value", "C-parent")
    }

    @Test
    fun `builds a mixed multi-child tree`() {
        output["parent1"] = {
            output["child1"] = "value"
            output["child2"] = "value"
        }
        output["parent2"] = {
            output["child3"] = {
                output["grandchild1"] = "value"
                output["grandchild2"] = "value"
            }
        }
        val builder = TestingTsdBuilder()
        output.build(builder)
        assertThat(builder.calls).containsExactly("O-parent1", "L-child1-value", "L-child2-value", "C-parent1", "O-parent2", "O-child3", "L-grandchild1-value", "L-grandchild2-value", "C-child3", "C-parent2")
    }

}