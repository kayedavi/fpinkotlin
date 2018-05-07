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
    // `map2(f, acc) { h, t -> listOf(h) + t }` results in a value of type `Rand<List<A>>`
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

data class State<S, out A>(val run: (S) -> Pair<A, S>) {
    fun <B> map(f: (A) -> B): State<S, B> =
            flatMap { a -> unit<S, B>(f(a)) }

    fun <B, C> map2(sb: State<S, B>, f: (A, B) -> C): State<S, C> =
            flatMap { a -> sb.map { b -> f(a, b) } }

    fun <B> flatMap(f: (A) -> State<S, B>): State<S, B> = State { s ->
        val (a, s1) = run(s)
        f(a).run(s1)
    }

    companion object {
        fun <S, A> unit(a: A): State<S, A> =
                State { s -> a to s }

        // The idiomatic solution is expressed via foldRight
        fun <S, A> sequenceViaFoldRight(sas: List<State<S, A>>): State<S, List<A>> =
                sas.foldRight(unit(emptyList())) { f, acc -> f.map2(acc) { h, t -> listOf(h) + t } }

        // This implementation uses a loop internally and is the same recursion
        // pattern as a left fold. It is quite common with left folds to build
        // up a list in reverse order, then reverse it at the end.
        // (We could also use a collection.mutable.ListBuffer internally.)
        fun <S, A> sequence(sas: List<State<S, A>>): State<S, List<A>> {
            fun go(s: S, actions: List<State<S, A>>, acc: List<A>): Pair<List<A>, S> =
                    when (actions) {
                        emptyList<State<S, A>>() -> acc.reversed() to s
                        else -> {
                            val h = actions.first()
                            val t = actions.drop(1)
                            val (a, s2) = h.run(s)
                            go(s2, t, listOf(a) + acc)
                        }
                    }
            return State { s: S -> go(s, sas, emptyList()) }
        }

        // We can also write the loop using a left fold. This is tail recursive like the
        // previous solution, but it reverses the list _before_ folding it instead of after.
        // You might think that this is slower than the `foldRight` solution since it
        // walks over the list twice, but it's actually faster! The `foldRight` solution
        // technically has to also walk the list twice, since it has to unravel the call
        // stack, not being tail recursive. And the call stack will be as tall as the list
        // is long.
        fun <S, A> sequenceViaFoldLeft(l: List<State<S, A>>): State<S, List<A>> =
                l.reversed().fold(unit(emptyList())) { acc, f -> f.map2(acc) { h, t -> listOf(h) + t } }

        fun <S> modify(f: (S) -> S): State<S, Unit> =
                get<S>().flatMap { s -> // Gets the current state and assigns it to `s`.
                    set(f(s)).map { _ -> Unit } } // Sets the new state to `f` applied to `s`.ª

        fun <S> get(): State<S, S> = State { s -> s to s }

        fun <S> set(s: S): State<S, Unit> = State { _ -> Unit to s }
    }
}