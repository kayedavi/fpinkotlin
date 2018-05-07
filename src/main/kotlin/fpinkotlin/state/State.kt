package fpinkotlin.state

import arrow.core.Tuple3

interface RNG {
    fun nextInt(): Pair<Int, RNG> // Should generate a random `Int`. We'll later define other functions in terms of `nextInt`.

    companion object {
        // NB - this was called SimpleRNG in the book text

        data class Simple(val seed: Long) : RNG {
            override fun nextInt(): Pair<Int, RNG> {
                val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL // `and` is bitwise AND. We use the current seed to generate a new seed.
                val nextRNG = Simple(newSeed) // The next state, which is an `RNG` instance created from the new seed.
                val n = (newSeed shr 16).toInt() // `shr` is right binary shift with zero fill. The value `n` is our new pseudo-random integer.
                return n to nextRNG // The return value is a tuple containing both a pseudo-random integer and the next `RNG` state.
            }
        }

        // We need to be quite careful not to skew the generator.
        // Since `Int.MIN_VALUE` is 1 smaller than `-(Int.MAX_VALUE)`,
        // it suffices to increment the negative numbers by 1 and make them positive.
        // This maps Int.MIN_VALUE to Int.MAX_VALUE and -1 to 0.
        fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
            val (i, r) = rng.nextInt()
            return (if (i < 0) -(i + 1) else i) to r
        }

        // We generate an integer >= 0 and divide it by one higher than the
        // maximum. This is just one possible solution.
        fun double(rng: RNG): Pair<Double, RNG> {
            val (i, r) = nonNegativeInt(rng)
            return i / (Int.MAX_VALUE.toDouble() + 1) to r
        }

        fun boolean(rng: RNG): Pair<Boolean, RNG> {
            val (i, rng2) = rng.nextInt()
            return (i % 2 == 0) to rng2
        }

        fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
            val (i, r1) = rng.nextInt()
            val (d, r2) = double(r1)
            return i to d to r2
        }

        fun double3(rng: RNG): Pair<Tuple3<Double, Double, Double>, RNG> {
            val (d1, r1) = double(rng)
            val (d2, r2) = double(r1)
            val (d3, r3) = double(r2)
            return Tuple3(d1, d2, d3) to r3
        }

        // There is something terribly repetitive about passing the RNG along
        // every time. What could we do to eliminate some of this duplication
        // of effort?

        // A simple recursive solution
        fun ints(count: Int, rng: RNG): Pair<List<Int>, RNG> =
                if (count == 0)
                    emptyList<Int>() to rng
                else {
                    val (x, r1) = rng.nextInt()
                    val (xs, r2) = ints(count - 1, r1)
                    listOf(x) + xs to r2
                }

        // A tail-recursive solution
        fun ints2(count: Int, rng: RNG): Pair<List<Int>, RNG> {
            tailrec
            fun go(count: Int, r: RNG, xs: List<Int>): Pair<List<Int>, RNG> =
                    if (count == 0)
                        xs to r
                    else {
                        val (x, r2) = r.nextInt()
                        go(count - 1, r2, listOf(x) + xs)
                    }
            return go(count, rng, emptyList())
        }
    }
}