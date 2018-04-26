package fpinkotlin.gettingstarted

import fpinkotlin.gettingstarted.MyModule.abs
import fpinkotlin.gettingstarted.MyModule.factorial
import fpinkotlin.gettingstarted.MyModule.formatResult

// A comment!
/* Another comment */
/** A documentation comment */
object MyModule {
    fun abs(n: Int): Int =
            if (n < 0) -n
            else n

    private fun formatAbs(x: Int): String {
        val msg = "The absolute value of %d is %d"
        return msg.format(x, abs(x))
    }

    @JvmStatic
    fun main(args: Array<String>) =
            println(formatAbs(-42))

    // A definition of factorial, using a local, tail recursive function
    fun factorial(n: Int): Int {
        tailrec
        fun go(n: Int, acc: Int): Int =
                if (n <= 0) acc
                else go(n - 1, n * acc)

        return go(n, 1)
    }

    // Another implementation of `factorial`, this time with a `while` loop
    fun factorial2(n: Int): Int {
        var acc = 1
        var i = n
        while (i > 0) {
            acc *= i; i -= 1
        }
        return acc
    }

    // Exercise 1: Write a function to compute the nth fibonacci number

    // 0 and 1 are the first two numbers in the sequence,
    // so we start the accumulators with those.
    // At every iteration, we add the two numbers to get the next one.
    fun fib(n: Int): Int {
        tailrec
        fun loop(n: Int, prev: Int, cur: Int): Int =
                if (n == 0) prev
                else loop(n - 1, cur, prev + cur)
        return loop(n, 0, 1)
    }

    // This definition and `formatAbs` are very similar..
    private fun formatFactorial(n: Int): String {
        val msg = "The factorial of %d is %d."
        return msg.format(n, factorial(n))
    }

    // We can generalize `formatAbs` and `formatFactorial` to
    // accept a _function_ as a parameter
    fun formatResult(name: String, n: Int, f: (Int) -> Int): String {
        val msg = "The %s of %d is %d."
        return msg.format(name, n, f(n))
    }
}

object FormatAbsAndFactorial {

    // Now we can use our general `formatResult` function
    // with both `abs` and `factorial`
    @JvmStatic
    fun main(args: Array<String>) {
        println(formatResult("absolute value", -42, ::abs))
        println(formatResult("factorial", 7, ::factorial))
    }
}

// Functions get passed around so often in FP that it's
// convenient to have syntax for constructing a function
// *without* having to give it a name
object AnonymousFunctions {

    // Some examples of anonymous functions:
    @JvmStatic
    fun main(args: Array<String>) {
        println(formatResult("absolute value", -42, ::abs))
        println(formatResult("factorial", 7, ::factorial))
        println(formatResult("increment", 7, { x: Int -> x + 1 }))
        println(formatResult("increment2", 7, { x -> x + 1 }))
        println(formatResult("increment3", 7) { x -> x + 1 })
        println(formatResult("increment4", 7) { it + 1 })
        println(formatResult("increment5", 7, { x -> val r = x + 1; r }))
    }
}

object MonomorphicBinarySearch {

    // First, a findFirst, specialized to `String`.
    // Ideally, we could generalize this to work for any `Array` type.
    fun findFirst(ss: Array<String>, key: String): Int {
        tailrec
        fun loop(n: Int): Int =
        // If `n` is past the end of the array, return `-1`
        // indicating the key doesn't exist in the array.
                if (n >= ss.size) -1
                // `ss[n]` extracts the n'th element of the array `ss`.
                // If the element at `n` is equal to the key, return `n`
                // indicating that the element appears in the array at that index.
                else if (ss[n] == key) n
                else loop(n + 1) // Otherwise increment `n` and keep looking.
        // Start the loop at the first element of the array.
        return loop(0)
    }

}

object PolymorphicFunctions {

    // Here's a polymorphic version of `findFirst`, parameterized on
    // a function for testing whether an `A` is the element we want to find.
    // Instead of hard-coding `String`, we take a type `A` as a parameter.
    // And instead of hard-coding an equality check for a given key,
    // we take a function with which to test each element of the array.
    fun <A> findFirst(xs: Array<A>, p: (A) -> Boolean): Int {
        tailrec
        fun loop(n: Int): Int =
                if (n >= xs.size) -1
                // If the function `p` matches the current element,
                // we've found a match and we return its index in the array.
                else if (p(xs[n])) n
                else loop(n + 1)

        return loop(0)
    }


    // Exercise 2: Implement a polymorphic function to check whether
    // an `Array<A>` is sorted
    fun <A> isSorted(xs: Array<A>, gt: (A, A) -> Boolean): Boolean {
        tailrec
        fun go(n: Int): Boolean =
                if (n >= xs.size - 1) true
                else if (gt(xs[n], xs[n + 1])) false
                else go(n + 1)

        return go(0)
    }

    // Polymorphic functions are often so constrained by their type
    // that they only have one implementation! Here's an example:

    fun <A, B, C> partial1(a: A, f: (A, B) -> C): (B) -> C =
            { b: B -> f(a, b) }

    // Exercise 3: Implement `curry`.

    // Note that `->` associates to the right, so we could
    // write the return type as `A -> B -> C`
    fun <A, B, C> curry(f: (A, B) -> C): (A) -> ((B) -> C) =
            { a: A -> { b: B -> f(a, b) } }

    // NB: The `Function2` trait has a `curried` method already

    // Exercise 4: Implement `uncurry`
    fun <A, B, C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
            { a: A, b: B -> f(a)(b) }

    /*
    NB: There is a method on the `KFunction` object in the standard library,
    `KFunction.uncurried()` that you can use for uncurrying.
    Note that we can go back and forth between the two forms. We can curry
    and uncurry and the two forms are in some sense "the same". In FP jargon,
    we say that they are _isomorphic_ ("iso" = same; "morphe" = shape, form),
    a term we inherit from category theory.
    */

    // Exercise 5: Implement `compose`

    fun <A, B, C> compose(f: (B) -> C, g: (A) -> B): (A) -> C =
            { a: A -> f(g(a)) }
    
}