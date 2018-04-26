package fpinkotlin.errorhandling

sealed class Either<out E, out A> {
    fun <B> map(f: (A) -> B): Either<E, B> =
            when (this) {
                is Right -> Right(f(get))
                is Left -> Left(get)
            }

    companion object {
        fun <E, A> Either<E, A>.orElse(b: () -> Either<E, A>): Either<E, A> =
                when (this) {
                    is Left -> b()
                    is Right -> Right(get)
                }

        fun <E, A, B, C> Either<E, A>.map2(b: Either<E, B>, f: (A, B) -> C): Either<E, C> =
                flatMap { a -> b.map { b1 -> f(a, b1) } }

        fun mean(xs: Iterable<Double>): Either<String, Double> =
                if (xs.none())
                    Left("mean of empty list!")
                else
                    Right(xs.sum() / xs.count())

        fun safeDiv(x: Int, y: Int): Either<Exception, Int> =
                try {
                    Right(x / y)
                } catch (e: Exception) {
                    Left(e)
                }

        fun <A> Try(a: () -> A): Either<Exception, A> =
                try {
                    Right(a())
                } catch (e: Exception) {
                    Left(e)
                }

        fun <E, A, B> traverse(es: List<A>, f: (A) -> Either<E, B>): Either<E, List<B>> =
                if (es.isEmpty()) Right(emptyList())
                else {
                    val h = es.first()
                    val t = es.drop(1)
                    f(h).map2(traverse(t, f)) { hh, tt -> listOf(hh) + tt }
                }

        fun <E, A, B> traverse_1(es: List<A>, f: (A) -> Either<E, B>): Either<E, List<B>> =
                es.foldRight(Right(emptyList())) { a, b -> f(a).map2(b) { h, t -> listOf(h) + t } }

        fun <E, A> sequence(es: List<Either<E, A>>): Either<E, List<A>> =
                traverse(es) { x -> x }

        fun <E, A, B> Either<E, A>.flatMap(f: (A) -> Either<E, B>): Either<E, B> =
                when (this) {
                    is Left -> Left(get)
                    is Right -> f(get)
                }
    }
}

data class Left<out E>(val get: E) : Either<E, Nothing>()
data class Right<out A>(val get: A) : Either<Nothing, A>()