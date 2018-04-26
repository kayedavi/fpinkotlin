package fpinkotlin.datastructures

sealed class List<out A> { // `List` data type, parameterized on a type, `A`
    companion object { // `List` companion object. Contains functions for creating and working with lists.
        fun sum(ints: List<Int>): Int = when (ints) { // A function that uses pattern matching to add up a list of integers
            Nil -> 0 // The sum of the empty list is 0.
            is Cons -> ints.head + sum(ints.tail) // The sum of a list starting with `head` is `head` plus the sum of the rest of the list.
        }

        fun product(ds: List<Double>): Double = when (ds) {
            Nil -> 1.0
            is Cons -> if (ds.head == 0.0) 0.0 else ds.head * product(ds.tail)
        }

        operator fun <A> invoke(vararg array: A): List<A> = // Variadic function syntax
                if (array.isEmpty()) Nil
                else Cons(array[0], invoke(*array.sliceArray(1..array.lastIndex)))

        fun <A> append(a1: List<A>, a2: List<A>): List<A> =
                when (a1) {
                    Nil -> a2
                    is Cons -> Cons(a1.head, append(a1.tail, a2))
                }

        fun <A, B> foldRight(xs: List<A>, z: B, f: (A, B) -> B): B = // Utility functions
                when (xs) {
                    Nil -> z
                    is Cons -> f(xs.head, foldRight(xs.tail, z, f))
                }

        fun sum2(ns: List<Int>) =
                foldRight(ns, 0) { x, y -> x + y }

        fun product2(ns: List<Double>) =
                foldRight(ns, 1.0) { x, y -> x * y }

        /*
        3. The third case is the first that matches, with `x` bound to 1 and `y` bound to 2.
        */

        /*
        Although we could return `Nil` when the input list is empty, we choose to throw an exception instead. This is
        a somewhat subjective choice. In our experience, taking the tail of an empty list is often a bug, and silently
        returning a value just means this bug will be discovered later, further from the place where it was introduced.

        It's generally good practice when pattern matching to use `_` for any variables you don't intend to use on the
        right hand side of a pattern. This makes it clear the value isn't relevant.
        */
        fun <A> tail(l: List<A>): List<A> =
                when (l) {
                    Nil -> error("tail of empty list")
                    is Cons -> l.tail
                }

        /*
        If a function body consists solely of a match expression, we'll often put the match on the same line as the
        function signature, rather than introducing another level of nesting.
        */
        fun <A> setHead(l: List<A>, h: A): List<A> = when (l) {
            Nil -> error("setHead on empty list")
            is Cons -> Cons(h, l.tail)
        }

        /*
        Again, it's somewhat subjective whether to throw an exception when asked to drop more elements than the list
        contains. The usual default for `drop` is not to throw an exception, since it's typically used in cases where this
        is not indicative of a programming error. If you pay attention to how you use `drop`, it's often in cases where the
        length of the input list is unknown, and the number of elements to be dropped is being computed from something else.
        If `drop` threw an exception, we'd have to first compute or check the length and only drop up to that many elements.
        */
        fun <A> drop(l: List<A>, n: Int): List<A> =
                if (n <= 0) l
                else when (l) {
                    Nil -> Nil
                    is Cons -> drop(l.tail, n - 1)
                }

        /*
        Somewhat overkill, but to illustrate the feature we're using a _pattern guard_, to only match a `Cons` whose head
        satisfies our predicate, `f`. The syntax is to add `if <cond>` after the pattern, before the `=>`, where `<cond>` can
        use any of the variables introduced by the pattern.
        */
        fun <A> dropWhile(l: List<A>, f: (A) -> Boolean): List<A> =
                if (l is Cons && f(l.head)) dropWhile(l.tail, f)
                else l

        /*
        Note that we're copying the entire list up until the last element. Besides being inefficient, the natural recursive
        solution will use a stack frame for each element of the list, which can lead to stack overflows for
        large lists (can you see why?). With lists, it's common to use a temporary, mutable buffer internal to the
        function (with lazy lists or streams, which we discuss in chapter 5, we don't normally do this). So long as the
        buffer is allocated internal to the function, the mutation is not observable and RT is preserved.

        Another common convention is to accumulate the output list in reverse order, then reverse it at the end, which
        doesn't require even local mutation. We'll write a reverse function later in this chapter.
        */
        fun <A> init(l: List<A>): List<A> =
                when (l) {
                    Nil -> error("init of empty list")
                    is Cons -> if (l.tail === Nil) Nil else Cons(l.head, init(l.tail))
                }

        fun <A> init2(l: List<A>): List<A> {
            val buf = ArrayList<A>()
            tailrec
            fun go(cur: List<A>): List<A> = when (cur) {
                Nil -> error("init of empty list")
                is Cons -> if (cur.tail === Nil) List(*buf.toArray()) as List<A> else {
                    buf += cur.head; go(cur.tail)
                }
            }
            return go(l)
        }

        /*
        No, this is not possible! The reason is because _before_ we ever call our function, `f`, we evaluate its argument,
        which in the case of `foldRight` means traversing the list all the way to the end. We need _non-strict_ evaluation
        to support early termination---we discuss this in chapter 5.
        */

        /*
        We get back the original list! Why is that? As we mentioned earlier, one way of thinking about what `foldRight` "does"
        is it replaces the `Nil` constructor of the list with the `z` argument, and it replaces the `Cons` constructor with
        the given function, `f`. If we just supply `Nil` for `z` and `Cons` for `f`, then we get back the input list.

        foldRight(Cons(1, Cons(2, Cons(3, Nil))), Nil:List[Int])(Cons(_,_))
        Cons(1, foldRight(Cons(2, Cons(3, Nil)), Nil:List[Int])(Cons(_,_)))
        Cons(1, Cons(2, foldRight(Cons(3, Nil), Nil:List[Int])(Cons(_,_))))
        Cons(1, Cons(2, Cons(3, foldRight(Nil, Nil:List[Int])(Cons(_,_)))))
        Cons(1, Cons(2, Cons(3, Nil)))
        */

        fun <A> length(l: List<A>): Int =
                foldRight(l, 0) { _, acc -> acc + 1 }

        /*
        It's common practice to annotate functions you expect to be tail-recursive with the `tailrec` annotation. If the
        function is not tail-recursive, it will yield a compile error, rather than silently compiling the code and resulting
        in greater stack space usage at runtime.
        */
        tailrec
        fun <A, B> foldLeft(l: List<A>, z: B, f: (B, A) -> B): B = when (l) {
            Nil -> z
            is Cons -> foldLeft(l.tail, f(z, l.head), f)
        }

        fun sum3(l: List<Int>) = foldLeft(l, 0) { x, y -> x + y }
        fun product3(l: List<Double>) = foldLeft(l, 1.0) { x, y -> x * y }

        fun <A> length2(l: List<A>): Int = foldLeft(l, 0) { acc, h -> acc + 1 }

        fun <A> reverse(l: List<A>): List<A> = foldLeft(l, Nil as List<A>) { acc, h -> Cons(h, acc) }

        /*
        The implementation of `foldRight` in terms of `reverse` and `foldLeft` is a common trick for avoiding stack overflows
        when implementing a strict `foldRight` function as we've done in this chapter. (We'll revisit this in a later chapter,
        when we discuss laziness).

        The other implementations build up a chain of functions which, when called, results in the operations being performed
        with the correct associativity. We are calling `foldRight` with the `B` type being instantiated to `B => B`, then
        calling the built up function with the `z` argument. Try expanding the definitions by substituting equals for equals
        using a simple example, like `foldLeft(List(1,2,3), 0){ x, y -> x + y }` if this isn't clear. Note these implementations are
        more of theoretical interest - they aren't stack-safe and won't work for large lists.
        */
        fun <A, B> foldRightViaFoldLeft(l: List<A>, z: B, f: (A, B) -> B): B =
                foldLeft(reverse(l), z) { b, a -> f(a, b) }

        fun <A, B> foldRightViaFoldLeft_1(l: List<A>, z: B, f: (A, B) -> B): B =
            foldLeft(l, { b: B -> b }, { g, a -> { b -> g(f(a, b)) } })(z)
    }
}

object Nil : List<Nothing>() // A `List` constructor representing the empty list
/* Another data constructor, representing nonempty lists. Note that `tail` is another `List[A]`,
which may be `Nil` or another `Cons`.
 */
data class Cons<out A>(val head: A, val tail: List<A>) : List<A>()