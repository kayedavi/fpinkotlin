package fpinkotlin.datastructures

sealed class Tree<out A> {
    companion object {


        fun <A> size(t: Tree<A>): Int = when (t) {
            is Leaf -> 1
            is Branch -> 1 + size(t.left) + size(t.right)
        }

        /*
        We're using the method `max` that exists on all `Int` values rather than an explicit `if` expression.

        Note how similar the implementation is to `size`. We'll abstract out the common pattern in a later exercise.
        */
        fun maximum(t: Tree<Int>): Int = when (t) {
            is Leaf -> t.value
            is Branch -> maxOf(maximum(t.left), maximum(t.right))
        }

        /*
        Again, note how similar the implementation is to `size` and `maximum`.
        */
        fun <A> depth(t: Tree<A>): Int = when (t) {
            is Leaf -> 0
            is Branch -> 1 + maxOf(depth(t.left), depth(t.right))
        }

        fun <A, B> map(t: Tree<A>, f: (A) -> B): Tree<B> = when (t) {
            is Leaf -> Leaf(f(t.value))
            is Branch -> Branch(map(t.left, f), map(t.right, f))
        }

        /*
        Like `foldRight` for lists, `fold` receives a "handler" for each of the data constructors of the type, and recursively
        accumulates some value using these handlers. As with `foldRight`, `fold(t, { Leaf(it) }, { l, r -> Branch(l, r)}) == t`, and we can use
        this function to implement just about any recursive function that would otherwise be defined by pattern matching.
        */
        fun <A, B> fold(t: Tree<A>, f: (A) -> B, g: (B, B) -> B): B = when (t) {
            is Leaf -> f(t.value)
            is Branch -> g(fold(t.left, f, g), fold(t.right, f, g))
        }

        fun <A> sizeViaFold(t: Tree<A>): Int =
                fold(t, { a -> 1 }, { x, y -> 1 + x + y })

        fun maximumViaFold(t: Tree<Int>): Int =
                fold(t, { a -> a }, { x, y -> maxOf(x, y) })

        fun <A> depthViaFold(t: Tree<A>): Int =
                fold(t, { a -> 0 }, { d1, d2 -> 1 + maxOf(d1, d2) })

        /*
        Note the type annotation required on the expression `Leaf(f(a))`. Without this annotation, we get an error like this:

        type mismatch;
          found   : fpinkotlin.datastructures.Branch<B>
          required: fpinkotlin.datastructures.Leaf<B>
             fold(t, { a -> Leaf(f(a)) }, { l, r -> Branch(l, r) })
                                      ^

        This error is an unfortunate consequence of Scala using subtyping to encode algebraic data types. Without the
        annotation, the result type of the fold gets inferred as `Leaf<B>` and it is then expected that the second argument
        to `fold` will return `Leaf<B>`, which it doesn't (it returns `Branch<B>`). Really, we'd prefer Scala to
        infer `Tree<B>` as the result type in both cases. When working with algebraic data types in Scala, it's somewhat
        common to define helper functions that simply call the corresponding data constructors but give the less specific
        result type:

          fun <A> leaf(a: A): Tree<A> = Leaf(a)
          fun <A> branch(l: Tree<A>, r: Tree<A>): Tree<A> = Branch(l, r)
        */
        fun <A, B> mapViaFold(t: Tree<A>, f: (A) -> B): Tree<B> =
                fold(t, { a -> Leaf(f(a)) as Tree<B> }, { l, r -> Branch(l, r) })
    }
}

data class Leaf<A>(val value: A) : Tree<A>()
data class Branch<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()