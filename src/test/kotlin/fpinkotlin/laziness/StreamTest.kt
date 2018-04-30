package fpinkotlin.laziness

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamTest {
    @Test
    fun constant() {
        val fours = Stream.constant(4).take(5).toList()
        assertEquals(listOf(4, 4, 4, 4, 4), fours)
    }
}