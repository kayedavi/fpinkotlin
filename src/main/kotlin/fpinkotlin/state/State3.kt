package fpinkotlin.state

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