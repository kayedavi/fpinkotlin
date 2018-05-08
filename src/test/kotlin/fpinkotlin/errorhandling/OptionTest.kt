package fpinkotlin.errorhandling

import org.junit.Assert.*
import org.junit.Test

class OptionTest {
    private val noneOfInt: Option<Int> = None

    @Test
    fun testGetOrElse() {
        assertEquals(6, Some(6).getOrElse { 7 })
    }

    @Test
    fun testOrElse() {
        assertEquals(Some(6), noneOfInt.orElse { Some(6) })
        assertEquals(Some(6), Some(6).orElse { None })
    }

    @Test
    fun testOrElse_1() {
        assertEquals(Some(6), noneOfInt.orElse_1 { Some(6) })
        assertEquals(Some(6), Some(6).orElse_1 { None })
    }

    @Test
    fun `map a Some(5)`() {
        assertEquals(Some(6), Some(5).map { it + 1 })
    }

    @Test
    fun `map a None`() {
        assertEquals(None, noneOfInt.map { it + 1 })
    }
}