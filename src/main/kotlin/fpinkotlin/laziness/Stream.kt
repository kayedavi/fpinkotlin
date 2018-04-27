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

    fun takeWhileViaUnfold(f: (A) -> Boolean): Stream<A> =
            unfold(this) {
                if (it is Cons && f(it.h())) Some(it.h() to it.t())
                else None
            }

    fun <B, C> zipWith(s2: Stream<B>, f: (A, B) -> C): Stream<C> =
            unfold(this to s2) {
                val s1 = it.first
                val s2 = it.second
                if (s1 is Cons && s2 is Cons) Some(f(s1.h(), s2.h()) to (s1.t() to s2.t()))
                else None
            }

    // special case of `zipWith`
    fun <B> zip(s2: Stream<B>): Stream<Pair<A, B>> =
            zipWith(s2) { x, y -> x to y }

    fun <B> zipAll(s2: Stream<B>): Stream<Pair<Option<A>, Option<B>>> =
            zipWithAll(s2) { it.first to it.second }

    fun <B, C> zipWithAll(s2: Stream<B>, f: (Pair<Option<A>, Option<B>>) -> C): Stream<C> =
            Stream.unfold(this to s2) {
                val s1 = it.first
                val s2 = it.second
                if (s1 is Cons && s2 === Empty) Some(f(Some(s1.h()) to Option.empty()) to (s1.t() to empty()))
                else if (s1 === Empty && s2 is Cons) Some(f(Option.empty<A>() to Some(s2.h())) to (empty<A>() to s2.t()))
                else if (s1 is Cons && s2 is Cons) Some(f(Some(s1.h()) to Some(s2.h())) to (s1.t() to s2.t()))
                else None
            }

    /*
    `s startsWith s2` when corresponding elements of `s` and `s2` are all equal, until the point that `s2` is exhausted. If `s` is exhausted first, or we find an element that doesn't match, we terminate early. Using non-strictness, we can compose these three separate logical steps--the zipping, the termination when the second stream is exhausted, and the termination if a nonmatching element is found or the first stream is exhausted.
    */
    fun <A> startsWith(s: Stream<A>): Boolean =
            zipAll(s).takeWhile { !it.second.isEmpty() }.forAll {
                it.first == it.second
            }

    /*
    The last element of `tails` is always the empty `Stream`, so we handle this as a special case, by appending it to the output.
    */
    fun tails(): Stream<Stream<A>> =
            unfold(this) {
                when (it) {
                    Empty -> None
                    is Cons -> Some(it to it.drop(1))
                }
            }.append { Stream(empty<A>()) }

    fun <A> hasSubsequence(s: Stream<A>): Boolean =
            tails().exists { it.startsWith(s) }

    /*
    The function can't be implemented using `unfold`, since `unfold` generates elements of the `Stream` from left to right. It can be implemented using `foldRight` though.

    The implementation is just a `foldRight` that keeps the accumulated value and the stream of intermediate results, which we `cons` onto during each iteration. When writing folds, it's common to have more state in the fold than is needed to compute the result. Here, we simply extract the accumulated list once finished.
    */
    fun <B> scanRight(z: B, f: (A, () -> B) -> B): Stream<B> {
        return foldRight({ z to Stream(z) }, { a, p0 ->
            val p1 by lazy { p0() }
            val b2 = f(a, p1::first)
            b2 to cons({ b2 }, p1::second)
        }).second
    }

    companion object {
        fun <A> cons(hd: () -> A, tl: () -> Stream<A>): Stream<A> {
            val head by lazy { hd() }
            val tail by lazy { tl() }
            return Cons({ head }, { tail })
        }

        fun <A> empty(): Stream<A> = Empty

        operator fun <A> invoke(vararg xs: A): Stream<A> =
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

        tailrec
        fun <A> Stream<A>.find(f: (A) -> Boolean): Option<A> = when (this) {
            is Empty -> None
            is Cons -> if (f(h())) Some(h()) else t().find(f)
        }
    }
}

object Empty : Stream<Nothing>()
data class Cons<out A>(val h: () -> A, val t: () -> Stream<A>) : Stream<A>()