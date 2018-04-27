package fpinkotlin.laziness

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse

sealed class Stream<out A> {

    // The natural recursive solution
    fun toListRecursive(): List<A> = when (this) {
        is Cons -> listOf(h()) + t().toListRecursive()
        else -> emptyList()
    }

    /*
    The above solution will stack overflow for large streams, since it's
    not tail-recursive. Here is a tail-recursive implementation. At each
    step we cons onto the front of the `acc` list, which will result in the
    reverse of the stream. Then at the end we reverse the result to get the
    correct order again.
    */
    fun toList(): List<A> {
        tailrec
        fun go(s: Stream<A>, acc: List<A>): List<A> = when (s) {
            is Cons -> go(s.t(), listOf(s.h()) + acc)
            else -> acc
        }
        return go(this, emptyList()).reversed()
    }

    /*
    In order to avoid the `reverse` at the end, we could write it using a
    mutable list buffer and an explicit loop instead. Note that the mutable
    list buffer never escapes our `toList` method, so this function is
    still _pure_.
    */
    fun toListFast(): List<A> {
        val buf = ArrayList<A>()
        tailrec
        fun go(s: Stream<A>): List<A> = when (s) {
            is Cons -> {
                buf += s.h()
                go(s.t())
            }
            else -> buf
        }
        return go(this)
    }

    /*
      Create a new Stream[A] from taking the n first elements from this. We can achieve that by recursively
      calling take on the invoked tail of a cons cell. We make sure that the tail is not invoked unless
      we need to, by handling the special case where n == 1 separately. If n == 0, we can avoid looking
      at the stream at all.
    */
    fun take(n: Int): Stream<A> =
            if (this is Cons && n > 1) cons(h, { t().take(n - 1) })
            else if (this is Cons && n == 1) cons(h, ::empty)
            else empty()

    /*
    It's a common Scala style to write method calls without `.` notation, as in `t() takeWhile f`.
    */
    fun takeWhile(f: (A) -> Boolean): Stream<A> =
            if (this is Cons && f(h())) cons(h, { t().takeWhile(f) })
            else empty()

    fun <B> foldRight(z: () -> B, f: (A, () -> B) -> B): B = // The arrow `=>` in front of the argument type `B` means that the function `f` takes its second argument by name and may choose not to evaluate it.
            when (this) {
                is Cons -> f(h(), { t().foldRight(z, f) }) // If `f` doesn't evaluate its second argument, the recursion never occurs.
                else -> z()
            }

    fun exists(p: (A) -> Boolean): Boolean =
            foldRight({ false }) { a, b -> p(a) || b() } // Here `b` is the unevaluated recursive step that folds the tail of the stream. If `p(a)` returns `true`, `b` will never be evaluated and the computation terminates early.

    /*
    Since `&&` is non-strict in its second argument, this terminates the traversal as soon as a nonmatching element is found.
    */
    fun forAll(f: (A) -> Boolean): Boolean =
            foldRight({ true }) { a, b -> f(a) && b() }

    fun takeWhile_1(f: (A) -> Boolean): Stream<A> =
            foldRight(::empty) { h, t ->
                if (f(h)) cons({ h }, t)
                else empty()
            }

    fun headOption(): Option<A> =
            foldRight({ None as Option<A> }) { h, _ -> Some(h) }

    fun <B> map(f: (A) -> B): Stream<B> =
            foldRight(::empty) { h, t -> cons({ f(h) }, t) }

    fun filter(f: (A) -> Boolean): Stream<A> =
            foldRight(::empty) { h, t ->
                if (f(h)) cons({ h }, t)
                else t()
            }

    fun <B> flatMap(f: (A) -> Stream<B>): Stream<B> =
            foldRight(::empty) { h, t -> f(h).append(t) }

    fun <B> mapViaUnfold(f: (A) -> B): Stream<B> =
            unfold(this) {
                when (it) {
                    is Cons -> Some(f(it.h()) to it.t())
                    else -> None
                }
            }

    fun takeViaUnfold(n: Int): Stream<A> =
            unfold(this to n) {
                val s = it.first
                val nn = it.second

                if (s is Cons && nn == 1) Some(s.h() to (s.t() to n - 1))
                else if (s is Cons && nn > 1) Some(s.h() to (s.t() to n - 1))
                else None
            }

    companion object {
        fun <A> cons(hd: () -> A, tl: () -> Stream<A>): Stream<A> {
            val head by lazy { hd() }
            val tail by lazy { tl() }
            return Cons({ head }, { tail })
        }

        fun <A> empty(): Stream<A> = Empty

        fun <A> invoke(vararg xs: A): Stream<A> =
                if (xs.isEmpty()) empty()
                else cons(xs::first, { invoke(*xs.sliceArray(1..xs.lastIndex)) })

        /*
          Create a new Stream[A] from this, but ignore the n first elements. This can be achieved by recursively calling
          drop on the invoked tail of a cons cell. Note that the implementation is also tail recursive.
        */
        tailrec
        fun <A> Stream<A>.drop(n: Int): Stream<A> =
                if (this is Cons && n > 0) t().drop(n - 1)
                else this

        fun <A> Stream<A>.append(s: () -> Stream<A>): Stream<A> =
                foldRight(s) { h, t -> cons({ h }, t) }

        fun <A, S> unfold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> {
            val o = f(z)
            return when (o) {
                is Some -> cons(o.t::first, { unfold(o.t.second, f) })
                None -> empty()
            }
        }

        /*
        The below two implementations use `fold` and `map` functions in the Option class to implement unfold, thereby doing away with the need to manually pattern match as in the above solution.
        */
        fun <A, S> unfoldViaFold(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> =
                f(z).fold(::empty) { p: Pair<A, S> -> cons(p::first, { unfold(p.second, f) }) }

        fun <A, S> unfoldViaMap(z: S, f: (S) -> Option<Pair<A, S>>): Stream<A> =
                f(z).map { p: Pair<A, S> -> cons(p::first, { unfold(p.second, f) }) }.getOrElse(::empty)
    }
}

object Empty : Stream<Nothing>()
data class Cons<out A>(val h: () -> A, val t: () -> Stream<A>) : Stream<A>()