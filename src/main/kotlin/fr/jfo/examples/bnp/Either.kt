package fr.jfo.examples.bnp

sealed class Either<out A, out B> {
    internal abstract val isRight: Boolean
    internal abstract val isLeft: Boolean

    fun right(): B? = when (this) {
        is Right -> this.value
        is Left -> null
    }

    fun left(): A? = when (this) {
        is Right -> null
        is Left -> this.value
    }

    class Left<out A>(val value: A): Either<A, Nothing>() {
        override val isLeft = true
        override val isRight = false

        override fun toString(): String {
            return "Left(value=$value)"
        }
    }

    class Right<out B>(val value: B): Either<Nothing, B>() {
        override val isLeft = false
        override val isRight = true

        override fun toString(): String {
            return "Right(value=$value)"
        }
    }

    override fun hashCode(): Int {
        var result = isRight.hashCode()
        result = 31 * result + isLeft.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Either<*, *>
        if (other.isRight && this.isLeft) return false
        if (other.isLeft && this.isLeft) return false

        return when (other) {
            is Left<*> -> other.value?.equals(this.left())!!
            is Right<*> -> other.value?.equals(this.right())!!
        }
    }

}
