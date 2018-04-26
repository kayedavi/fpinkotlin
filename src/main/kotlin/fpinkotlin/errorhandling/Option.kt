package fpinkotlin.errorhandling

import kotlin.math.pow

sealed class Option<out A> {
    fun <B> map(f: (A) -> B): Option<B> = when (this) {
        None -> None
        is Some -> Some(f(get))
    }

    fun <A> Option<A>.getOrElse(default: () -> A): A = when (this) {
        None -> default()
        is Some -> get
    }

    fun <B> flatMap(f: (A) -> Option<B>): Option<B> =
            map(f).getOrElse { None }

    /*
    Of course, we can also implement `flatMap` with explicit pattern matching.
    */
    fun <B> flatMap_1(f: (A) -> Option<B>): Option<B> = when (this) {
        None -> None
        is Some -> f(get)
    }

    fun <A> Option<A>.orElse(ob: () -> Option<A>): Option<A> =
            this.map { Some(it) }.getOrElse(ob)

    /*
    Again, we can implement this with explicit pattern matching.
    */
    fun <A> Option<A>.orElse_1(ob: () -> Option<A>): Option<A> = when (this) {
        None -> ob()
        else -> this
    }

    fun filter(f: (A) -> Boolean): Option<A> =
            if (this is Some && f(get)) this
            else None

    /*
    This can also be defined in terms of `flatMap`.
    */
    fun filter_1(f: (A) -> Boolean): Option<A> =
            flatMap { a -> if (f(a)) Some(a) else None }

    companion object {
        fun failingFn(i: Int): Int {
            // `val y: Int = ...` declares `y` as having type `Int`, and sets it equal to the right hand side of the `=`.
            val y: Int = throw Exception("fail!")
            try {
                val x = 42 + 5
                return x + y
            }
            // A `catch` block is just a pattern matching block like the ones we've seen. `case e: Exception` is a pattern
            // that matches any `Exception`, and it binds this value to the identifier `e`. The match returns the value 43.
            catch (e: Exception) {
                return 43
            }
        }

        fun failingFn2(i: Int): Int {
            try {
                val x = 42 + 5
                // A thrown Exception can be given any type; here we're annotating it with the type `Int`
                return x + ((throw Exception("fail!")) as Int)
            } catch (e: Exception) {
                return 43
            }
        }

        fun mean(xs: Iterable<Double>): Option<Double> =
                if (xs.none()) None
                else Some(xs.sum() / xs.count())

        fun variance(xs: Iterable<Double>): Option<Double> =
                mean(xs).flatMap { m -> mean(xs.map { x -> (x - m).pow(2) }) }

        // a bit later in the chapter we'll learn nicer syntax for
        // writing functions like this
        fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
                a.flatMap { aa -> b.map { bb -> f(aa, bb) } }

        /*
        Here's an explicit recursive version:
        */
        fun <A> sequence(a: List<Option<A>>): Option<List<A>> =
                if (a.isEmpty()) Some(emptyList())
                else {
                    val h = a.first()
                    val t = a.drop(1)
                    h.flatMap { hh -> sequence(t).map { listOf(hh) + it } }
                }

        /*
        It can also be implemented using `foldRight` and `map2`. The type annotation on `foldRight` is needed here; otherwise
        Scala wrongly infers the result type of the fold as `Some[Nil.type]` and reports a type error (try it!). This is an
        unfortunate consequence of Scala using subtyping to encode algebraic data types.
        */
        fun <A> sequence_1(a: List<Option<A>>): Option<List<A>> =
                a.foldRight(Some(emptyList())) { x, y -> map2(x, y) { h, t -> listOf(h) + t } }

        fun <A, B> traverse(a: List<A>, f: (A) -> Option<B>): Option<List<B>> =
                if (a.isEmpty()) Some(emptyList())
                else {
                    val h = a.first()
                    val t = a.drop(1)
                    map2(f(h), traverse(t, f)) { h, t -> listOf(h) + t }
                }

        fun <A, B> traverse_1(a: List<A>, f: (A) -> Option<B>): Option<List<B>> =
                a.foldRight(Some(emptyList())) { h, t -> map2(f(h), t) { hh, tt -> listOf(hh) + tt } }

        fun <A> sequenceViaTraverse(a: List<Option<A>>): Option<List<A>> =
                traverse(a) { x -> x }
    }
}

data class Some<out A>(val get: A) : Option<A>()
object None : Option<Nothing>()