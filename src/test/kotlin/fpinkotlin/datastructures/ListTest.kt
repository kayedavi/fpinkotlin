package fpinkotlin.datastructures

import fpinkotlin.datastructures.List.Companion.add1
import fpinkotlin.datastructures.List.Companion.addPairwise
import fpinkotlin.datastructures.List.Companion.append
import fpinkotlin.datastructures.List.Companion.appendViaFoldRight
import fpinkotlin.datastructures.List.Companion.concat
import fpinkotlin.datastructures.List.Companion.doubleToString
import fpinkotlin.datastructures.List.Companion.drop
import fpinkotlin.datastructures.List.Companion.dropWhile
import fpinkotlin.datastructures.List.Companion.filter
import fpinkotlin.datastructures.List.Companion.filterViaFlatMap
import fpinkotlin.datastructures.List.Companion.filter_1
import fpinkotlin.datastructures.List.Companion.filter_2
import fpinkotlin.datastructures.List.Companion.flatMap
import fpinkotlin.datastructures.List.Companion.foldLeft
import fpinkotlin.datastructures.List.Companion.foldLeftViaFoldRight
import fpinkotlin.datastructures.List.Companion.foldRight
import fpinkotlin.datastructures.List.Companion.foldRightViaFoldLeft
import fpinkotlin.datastructures.List.Companion.foldRightViaFoldLeft_1
import fpinkotlin.datastructures.List.Companion.hasSubsequence
import fpinkotlin.datastructures.List.Companion.init
import fpinkotlin.datastructures.List.Companion.init2
import fpinkotlin.datastructures.List.Companion.length
import fpinkotlin.datastructures.List.Companion.length2
import fpinkotlin.datastructures.List.Companion.map
import fpinkotlin.datastructures.List.Companion.map_1
import fpinkotlin.datastructures.List.Companion.map_2
import fpinkotlin.datastructures.List.Companion.product
import fpinkotlin.datastructures.List.Companion.product2
import fpinkotlin.datastructures.List.Companion.product3
import fpinkotlin.datastructures.List.Companion.reverse
import fpinkotlin.datastructures.List.Companion.setHead
import fpinkotlin.datastructures.List.Companion.sum
import fpinkotlin.datastructures.List.Companion.sum2
import fpinkotlin.datastructures.List.Companion.sum3
import fpinkotlin.datastructures.List.Companion.tail
import fpinkotlin.datastructures.List.Companion.zipWith
import org.junit.Assert.*
import org.junit.Test

class ListTest {
    private val list = List(1, 2, 3, 4)

    @Test
    fun sumNumbers() {
        assertEquals(10, sum(list))
    }

    @Test
    fun productOfNumbers() {
        val dList = List(1.0, 2.0, 3.0, 4.0)
        assertEquals(24.0, product(dList), 0.01)
    }

    @Test
    fun `append two lists`() {
        val list2 = List(5, 6, 7, 8)
        assertEquals(List(1, 2, 3, 4, 5, 6, 7, 8), append(list, list2))
    }

    @Test
    fun `foldRight should be able to sum a list`() {
        assertEquals(12, foldRight(list, 2) { x, y -> x + y })
    }

    @Test
    fun sum2Numbers() {
        assertEquals(10, sum2(list))
    }

    @Test
    fun product2OfNumbers() {
        val dList = List(1.0, 2.0, 3.0, 4.0)
        assertEquals(24.0, product2(dList), 0.01)
    }

    @Test
    fun `tail of a list`() {
        assertEquals(List(2, 3, 4), tail(list))
    }

    @Test
    fun `set the head of a list`() {
        assertEquals(List(5, 2, 3, 4), setHead(list, 5))
    }

    @Test
    fun `drop the first 2 elements of a list`() {
        assertEquals(List(3, 4), drop(list, 2))
    }

    @Test
    fun `drop the elements while a predicate is true`() {
        assertEquals(List(4), dropWhile(list) { it < 4 })
    }

    @Test
    fun `get the init of a list`() {
        assertEquals(List(1, 2, 3), init(list))
    }

    @Test
    fun `get the init of a list using TCO`() {
        assertEquals(List(1, 2, 3), init2(list))
    }

    @Test
    fun `determine the length of a list`() {
        assertEquals(4, length(list))
    }

    @Test
    fun `foldLeft to get the sum of a list`() {
        assertEquals(13, foldLeft(list, 3) { x, y -> x + y })
    }

    @Test
    fun sum3Numbers() {
        assertEquals(10, sum3(list))
    }

    @Test
    fun product3OfNumbers() {
        val dList = List(1.0, 2.0, 3.0, 4.0)
        assertEquals(24.0, product3(dList), 0.01)
    }

    @Test
    fun `determine the length2 of a list`() {
        assertEquals(4, length2(list))
    }

    @Test
    fun `reverse a list`() {
        assertEquals(List(4, 3, 2, 1), reverse(list))
    }

    @Test
    fun `foldRightViaFoldLeft should be able to sum a list`() {
        assertEquals(12, foldRightViaFoldLeft(list, 2) { x, y -> x + y })
    }

    @Test
    fun `foldRightViaFoldLeft_1 should be able to sum a list`() {
        assertEquals(12, foldRightViaFoldLeft_1(list, 2) { x, y -> x + y })
    }

    @Test
    fun `foldLeftViaFoldRight should be able to sum a list`() {
        assertEquals(12, foldLeftViaFoldRight(list, 2) { x, y -> x + y })
    }

    @Test
    fun `appendViaFoldRight two lists`() {
        val list2 = List(5, 6, 7, 8)
        assertEquals(List(1, 2, 3, 4, 5, 6, 7, 8), appendViaFoldRight(list, list2))
    }

    @Test
    fun `concat a list of a list`() {
        val inputList = List(List(1, 2, 3), List(4, 5, 6))
        assertEquals(List(1, 2, 3, 4, 5, 6), concat(inputList))
    }

    @Test
    fun `add 1 to each element of a list`() {
        assertEquals(List(2, 3, 4, 5), add1(list))
    }

    @Test
    fun `convert a list of doubles to a list of strings`() {
        val dList = List(1.0, 2.0, 3.0, 4.0)
        assertEquals(List("1.0", "2.0", "3.0", "4.0"), doubleToString(dList))
    }

    @Test
    fun `map a list`() {
        assertEquals(List(2, 4, 6, 8), map(list) { it * 2 })
    }

    @Test
    fun `map_1 a list`() {
        assertEquals(List(2, 4, 6, 8), map_1(list) { it * 2 })
    }

    @Test
    fun `map_2 a list`() {
        assertEquals(List(2, 4, 6, 8), map_2(list) { it * 2 })
    }

    @Test
    fun `filter for even numbers`() {
        assertEquals(List(2, 4), filter(list) { it % 2 == 0 })
    }

    @Test
    fun `filter_1 for even numbers`() {
        assertEquals(List(2, 4), filter_1(list) { it % 2 == 0 })
    }

    @Test
    fun `filter_2 for even numbers`() {
        assertEquals(List(2, 4), filter_2(list) { it % 2 == 0 })
    }

    @Test
    fun `flatMap to flatten a list of lists`() {
        assertEquals(List(1, 1, 2, 2, 3, 3, 4, 4), flatMap(list) { List(it, it) })
    }

    @Test
    fun `filterViaFlatMap for even numbers`() {
        assertEquals(List(2, 4), filterViaFlatMap(list) { it % 2 == 0 })
    }

    @Test
    fun `add corresponding elements of two lists`() {
        val list2 = List(10, 20, 30)
        assertEquals(List(11, 22, 33), addPairwise(list, list2))
    }

    @Test
    fun `zipWith addition`() {
        val list2 = List(10, 20, 30)
        assertEquals(List(11, 22, 33), zipWith(list, list2) { a, b -> a + b })
    }

    @Test
    fun `has a subsequence`() {
        val sub = List(3, 4)
        assertTrue(hasSubsequence(list, sub))
    }

    @Test
    fun `does not have a subsequence`() {
        val sub = List(5, 4)
        assertFalse(hasSubsequence(list, sub))
    }
}