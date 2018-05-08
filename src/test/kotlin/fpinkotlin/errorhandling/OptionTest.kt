package fpinkotlin.errorhandling

import fpinkotlin.errorhandling.Option.Companion.map2
import fpinkotlin.errorhandling.Option.Companion.mean
import fpinkotlin.errorhandling.Option.Companion.sequence
import fpinkotlin.errorhandling.Option.Companion.sequenceViaTraverse
import fpinkotlin.errorhandling.Option.Companion.sequence_1
import fpinkotlin.errorhandling.Option.Companion.traverse
import fpinkotlin.errorhandling.Option.Companion.traverse_1
import fpinkotlin.errorhandling.Option.Companion.variance
import org.junit.Assert.assertEquals
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

    @Test
    fun `Use flatMap to add increment a number`() {
        assertEquals(Some(7), Some(6).flatMap { Some(it + 1) })
    }

    @Test
    fun `Use flatMap_1 to add increment a number`() {
        assertEquals(Some(7), Some(6).flatMap_1 { Some(it + 1) })
    }

    @Test
    fun `filter for positive value`() {
        assertEquals(Some(6), Some(6).filter { it % 2 == 0 })
        assertEquals(None, Some(5).filter { it % 2 == 0 })
    }

    @Test
    fun `Calculate the mean of a list`() {
        assertEquals(Some(15.0), mean(listOf(10.0, 20.0)))
        assertEquals(None, mean(emptyList()))
    }

    @Test
    fun `Calculate the variance of a list`() {
        val actual = variance(listOf(10.0, 10.6, 17.8)).getOrElse { throw AssertionError() }
        assertEquals(12.56, actual, 0.001)
    }

    @Test
    fun `Multiply two Options using map2`() {
        assertEquals(Some(8), map2(Some(2), Some(4)) { x, y -> x * y })
        assertEquals(None, map2(Some(2), noneOfInt) { x, y -> x * y })
    }

    @Test
    fun `Sequence of list of Somes`() {
        val list = listOf(Some(4), Some(8), Some(-1))
        assertEquals(Some(listOf(4, 8, -1)), sequence(list))
    }

    @Test
    fun `sequence of list of Somes and a None`() {
        val list = listOf(Some(4), None, Some(8), Some(-1))
        assertEquals(None, sequence(list))
    }

    @Test
    fun `sequence_1 of list of Somes`() {
        val list = listOf(Some(4), Some(8), Some(-1))
        assertEquals(Some(listOf(4, 8, -1)), sequence_1(list))
    }

    @Test
    fun `traverse a list, adding 1 to each element`() {
        val list = listOf(4, 8, -1)
        assertEquals(Some(listOf(5, 9, 0)), traverse(list) { x: Int -> Some(x + 1) })
    }

    @Test
    fun `traverse_1 a list, adding 1 to each element`() {
        val list = listOf(4, 8, -1)
        assertEquals(Some(listOf(5, 9, 0)), traverse_1(list) { x: Int -> Some(x + 1) })
    }

    @Test
    fun `sequenceViaTraverse of list of Somes`() {
        val list = listOf(Some(4), Some(8), Some(-1))
        assertEquals(Some(listOf(4, 8, -1)), sequenceViaTraverse(list))
    }

    @Test
    fun `sequenceViaTraverse of list of Somes and a None`() {
        val list = listOf(Some(4), None, Some(8), Some(-1))
        assertEquals(None, sequenceViaTraverse(list))
    }
}