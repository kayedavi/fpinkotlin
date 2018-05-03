package fpinkotlin.state

import fpinkotlin.state.RNG.Companion.double
import fpinkotlin.state.RNG.Companion.nonNegativeInt

typealias Rand<A> = (RNG) -> Pair<A, RNG>

object State2 {
    val int: Rand<Int> = { it.nextInt() }

    fun <A> unit(a: A): Rand<A> =
            { rng -> a to rng }

    fun <A, B> map(s: Rand<A>, f: (A) -> B): Rand<B> =
            { rng ->
                val (a, rng2) = s(rng)
                f(a) to rng2
            }

    val _double: Rand<Double> =
            map(::nonNegativeInt) { it / (Int.MAX_VALUE.toDouble() + 1) }

    // This implementation of map2 passes the initial RNG to the first argument
    // and the resulting RNG to the second argument. It's not necessarily wrong
    // to do this the other way around, since the results are random anyway.
    // We could even pass the initial RNG to both `f` and `g`, but that might
    // have unexpected results. E.g. if both arguments are `RNG.int` then we would
    // always get two of the same `Int` in the result. When implementing functions
    // like this, it's important to consider how we would test them for
    // correctness.
    fun <A, B, C> map2(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> =
            { rng ->
                val (a, r1) = ra(rng)
                val (b, r2) = rb(r1)
                f(a, b) to r2
            }

    fun <A, B> both(ra: Rand<A>, rb: Rand<B>): Rand<Pair<A, B>> =
            map2(ra, rb) { x, y -> x to y }

    val randIntDouble: Rand<Pair<Int, Double>> =
            both(int, ::double)

    val randDoubleInt: Rand<Pair<Double, Int>> =
            both(::double, int)

    // In `sequence`, the base case of the fold is a `unit` action that returns
    // the empty list. At each step in the fold, we accumulate in `acc`
    // and `f` is the current element in the list.
    // `map2(f, acc)(_ :: _)` results in a value of type `Rand[List[A]]`
    // We map over that to prepend (cons) the element onto the accumulated list.
    //
    // We are using `foldRight`. If we used `foldLeft` then the values in the
    // resulting list would appear in reverse order. It would be arguably better
    // to use `foldLeft` followed by `reverse`. What do you think?
    fun <A> sequence(fs: List<Rand<A>>): Rand<List<A>> =
            fs.foldRight(unit(emptyList())) { f, acc ->
                map2(f, acc) { h, t -> listOf(h) + t }
            }

    // It's interesting that we never actually need to talk about the `RNG` value
    // in `sequence`. This is a strong hint that we could make this function
    // polymorphic in that type.

    fun _ints(count: Int): Rand<List<Int>> =
            sequence(List(count, { _ -> int }))

    fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> =
            { rng ->
                val (a, r1) = f(rng)
                g(a)(r1) // We pass the new state along
            }

    fun nonNegativeLessThan(n: Int): Rand<Int> {
        return flatMap(::nonNegativeInt) { i ->
            val mod = i % n
            if (i + (n - 1) - mod >= 0) unit(mod) else nonNegativeLessThan(n)
        }
    }

    fun <A, B> _map(s: Rand<A>, f: (A) -> B): Rand<B> =
            flatMap(s) { a -> unit(f(a)) }

    fun <A, B, C> _map2(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> =
            flatMap(ra) { a -> map(rb) { b -> f(a, b) } }
}