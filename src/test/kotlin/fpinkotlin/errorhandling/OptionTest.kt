package fpinkotlin.errorhandling

import org.junit.Assert.*
import org.junit.Test

class OptionTest {
    @Test
    fun testGetOrElse() {
        assertEquals(6, Some(6).getOrElse { 7 })
    }

    @Test
    fun testOrElse() {
        assertEquals(Some(6), (None as Option<Int>).orElse { Some(6) })
        assertEquals(Some(6), Some(6).orElse { None })
    }

    @Test
    fun testOrElse_1() {
        assertEquals(Some(6), (None as Option<Int>).orElse_1 { Some(6) })
        assertEquals(Some(6), Some(6).orElse_1 { None })
    }
}