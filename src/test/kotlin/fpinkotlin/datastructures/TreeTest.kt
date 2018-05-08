package fpinkotlin.datastructures

import fpinkotlin.datastructures.Tree.Companion.depth
import fpinkotlin.datastructures.Tree.Companion.depthViaFold
import fpinkotlin.datastructures.Tree.Companion.fold
import fpinkotlin.datastructures.Tree.Companion.map
import fpinkotlin.datastructures.Tree.Companion.mapViaFold
import fpinkotlin.datastructures.Tree.Companion.maximum
import fpinkotlin.datastructures.Tree.Companion.maximumViaFold
import fpinkotlin.datastructures.Tree.Companion.size
import fpinkotlin.datastructures.Tree.Companion.sizeViaFold
import org.junit.Assert.assertEquals
import org.junit.Test

class TreeTest {
    private val tree = Branch(Branch(Leaf(1), Leaf(2)), Branch(Leaf(3), Leaf(4)))

    @Test
    fun `Get size of a tree`() {
        assertEquals(7, size(tree))
    }

    @Test
    fun `Get maximum value from a tree`() {
        assertEquals(4, maximum(tree))
    }

    @Test
    fun `Get depth`() {
        assertEquals(2, depth(tree))
    }

    @Test
    fun `Double the values in a tree using map`() {
        val expected = Branch(Branch(Leaf(2), Leaf(4)), Branch(Leaf(6), Leaf(8)))
        assertEquals(expected, map(tree) { it * 2 })
    }

    @Test
    fun `sum the values in a tree using fold`() {
        assertEquals(10, fold(tree, { it }) { x, y -> x + y })
    }

    @Test
    fun `Get sizeViaFold of a tree`() {
        assertEquals(7, sizeViaFold(tree))
    }

    @Test
    fun `Get maximumViaFold value from a tree`() {
        assertEquals(4, maximumViaFold(tree))
    }

    @Test
    fun `Get depthViaFold`() {
        assertEquals(2, depthViaFold(tree))
    }

    @Test
    fun `Double the values in a tree using mapViaFold`() {
        val expected = Branch(Branch(Leaf(2), Leaf(4)), Branch(Leaf(6), Leaf(8)))
        assertEquals(expected, mapViaFold(tree) { it * 2 })
    }
}