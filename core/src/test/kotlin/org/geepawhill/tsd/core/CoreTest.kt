package org.geepawhill.tsd.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


class CoreTest {
    @Test
    fun testSomeLibraryMethod() {
        val classUnderTest = Library()
        assertThat(classUnderTest.someLibraryMethod()).isTrue()
        fail("This is a failure.")
    }
}
