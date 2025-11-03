// TODO: Consider removing values from tokens and just have the start indexes and their lengths?

sealed class Token {
    abstract val startIndex: Int
    abstract val length: Int

    // Sealed subclass for all CharArray-backed tokens
    sealed class CharArrayBacked(
        open val value: CharArray,
        final override val startIndex: Int,
        final override val length: Int
    ) : Token() {
        // Shared implementation - written once for all CharArray-backed tokens
        @ExcludeFromCoverage(reason = "Boilerplate equals implementation for CharArray-backed tokens")
        final override fun equals(other: Any?): Boolean =
            if (this === other) true
            else if (other !is CharArrayBacked) false
            else startIndex == other.startIndex &&
                    length == other.length &&
                    value.contentEquals(other.value)

        @ExcludeFromCoverage(reason = "Boilerplate hashCode implementation for CharArray-backed tokens")
        final override fun hashCode(): Int {
            var result = startIndex
            result = 31 * result + length
            result = 31 * result + value.contentHashCode()
            return result
        }

        override fun toString() =
            "${this::class.simpleName}(value=${value.concatToString()}, startIndex=${startIndex}, length=${length})"
    }

    // Regular classes (not data class) - they inherit the logic above
    class Number(value: CharArray, startIndex: Int, length: Int) :
        CharArrayBacked(value, startIndex, length)

    class String(value: CharArray, startIndex: Int, length: Int) :
        CharArrayBacked(value, startIndex, length)

    class Keyword(value: CharArray, startIndex: Int, length: Int) :
        CharArrayBacked(value, startIndex, length)

    // Simple tokens stay as data classes
    data class LeftBrace(override val startIndex: Int) : Token() {
        override val length = 1
    }

    data class RightBrace(override val startIndex: Int) : Token() {
        override val length = 1
    }

    data class LeftBracket(override val startIndex: Int) : Token() {
        override val length = 1
    }

    data class RightBracket(override val startIndex: Int) : Token() {
        override val length = 1
    }

    data class Comma(override val startIndex: Int) : Token() {
        override val length = 1
    }

    data class Colon(override val startIndex: Int) : Token() {
        override val length = 1
    }

    data class Invalid(val char: Char, override val startIndex: Int) : Token() {
        override val length = 1
    }

    data object EOF : Token() {
        override val startIndex = -1
        override val length = -1
    }
}