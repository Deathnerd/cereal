package com.deathnerd.cereal.json

import ExcludeFromCoverage

// TODO: Consider removing values from tokens and just have the start indexes and their lengths?

/**
 * Represents a JSON token parsed from input text.
 *
 * This sealed class defines the complete hierarchy of JSON token types. All tokens have a
 * [startIndex] indicating their position in the input and a [length] indicating how many
 * characters they occupy.
 *
 * The token hierarchy is organized as follows:
 * - [CharArrayBacked]: Base class for tokens that store their character values (Number, String, Keyword)
 * - Simple tokens: Structural elements with fixed length (braces, brackets, punctuation)
 * - Special tokens: EOF marker for stream termination
 *
 * @property startIndex The zero-based index in the input where this token begins
 * @property length The number of characters this token spans in the input
 */
sealed class JsonToken {
    /**
     * The zero-based index in the input string where this token begins.
     */
    abstract val startIndex: Int

    /**
     * The number of characters this token spans in the input string.
     */
    abstract val length: Int

    /**
     * Base class for tokens that store their character values as a [CharArray].
     *
     * This sealed class provides common functionality for token types that need to preserve
     * the exact character sequence from the input (Number, String, and Keyword tokens).
     * The equality and hash code implementations are shared across all subclasses to ensure
     * consistent behavior when comparing tokens.
     *
     * @property value The character array containing this token's value
     * @property startIndex The zero-based index in the input where this token begins
     * @property length The number of characters this token spans
     */
    sealed class CharArrayBacked(
        open val value: CharArray,
        final override val startIndex: Int,
        final override val length: Int
    ) : JsonToken() {
        /**
         * Checks equality with another object.
         *
         * Two CharArrayBacked tokens are equal if they are the same instance, or if they have
         * the same [startIndex], [length], and [value] content.
         *
         * @param other The object to compare with
         * @return true if the tokens are equal, false otherwise
         */
        @ExcludeFromCoverage(reason = "Boilerplate equals implementation for CharArray-backed tokens")
        final override fun equals(other: Any?): Boolean =
            if (this === other) {
                true
            } else if (other !is CharArrayBacked) {
                false
            } else {
                startIndex == other.startIndex &&
                        length == other.length &&
                        value.contentEquals(other.value)
            }

        /**
         * Computes the hash code for this token.
         *
         * The hash code is based on the [startIndex], [length], and [value] content,
         * ensuring consistent behavior with [equals].
         *
         * @return the hash code for this token
         */
        @ExcludeFromCoverage(reason = "Boilerplate hashCode implementation for CharArray-backed tokens")
        final override fun hashCode(): Int {
            var result = startIndex
            result = 31 * result + length
            result = 31 * result + value.contentHashCode()
            return result
        }

        /**
         * Returns a string representation of this token.
         *
         * The format includes the token's class name, the string value, start index, and length.
         *
         * @return a string representation of this token
         */
        override fun toString() =
            "${this::class.simpleName ?: "JsonToken"}(value=${value.concatToString()}, startIndex=${startIndex}, length=${length})"
    }

    /**
     * Represents a JSON number token.
     *
     * Contains the character sequence of a numeric value from the input.
     *
     * @param value The character array containing the number
     * @param startIndex The position of the first character of the number
     * @param length The number of characters in the number
     */
    class Number(value: CharArray, startIndex: Int, length: Int) :
        CharArrayBacked(value, startIndex, length)

    /**
     * Represents a JSON string token.
     *
     * Contains the character sequence of a string literal, including the surrounding quotes
     * and any escape sequences.
     *
     * @param value The character array containing the string (including quotes)
     * @param startIndex The position of the opening quote
     * @param length The number of characters in the string (including quotes)
     */
    class String(value: CharArray, startIndex: Int, length: Int) :
        CharArrayBacked(value, startIndex, length)

    /**
     * Represents a JSON keyword token (true, false, null).
     *
     * Contains the character sequence of a JSON keyword literal.
     *
     * @param value The character array containing the keyword
     * @param startIndex The position of the first character of the keyword
     * @param length The number of characters in the keyword
     */
    class Keyword(value: CharArray, startIndex: Int, length: Int) :
        CharArrayBacked(value, startIndex, length)

    /**
     * Represents a left curly brace token (`{`).
     *
     * @param startIndex The position of the opening brace
     */
    data class LeftBrace(override val startIndex: Int) : JsonToken() {
        /** The length of a single brace character. */
        override val length = 1
    }

    /**
     * Represents a right curly brace token (`}`).
     *
     * @param startIndex The position of the closing brace
     */
    data class RightBrace(override val startIndex: Int) : JsonToken() {
        /** The length of a single brace character. */
        override val length = 1
    }

    /**
     * Represents a left square bracket token (`[`).
     *
     * @param startIndex The position of the opening bracket
     */
    data class LeftBracket(override val startIndex: Int) : JsonToken() {
        /** The length of a single bracket character. */
        override val length = 1
    }

    /**
     * Represents a right square bracket token (`]`).
     *
     * @param startIndex The position of the closing bracket
     */
    data class RightBracket(override val startIndex: Int) : JsonToken() {
        /** The length of a single bracket character. */
        override val length = 1
    }

    /**
     * Represents a comma token (`,`).
     *
     * Used to separate elements in arrays and members in objects.
     *
     * @param startIndex The position of the comma
     */
    data class Comma(override val startIndex: Int) : JsonToken() {
        /** The length of a single comma character. */
        override val length = 1
    }

    /**
     * Represents a colon token (`:`).
     *
     * Used to separate keys from values in JSON objects.
     *
     * @param startIndex The position of the colon
     */
    data class Colon(override val startIndex: Int) : JsonToken() {
        /** The length of a single colon character. */
        override val length = 1
    }

    /**
     * Represents an invalid or unexpected character token.
     *
     * Used when the parser encounters a character that doesn't match any valid JSON token pattern.
     *
     * @param char The invalid character
     * @param startIndex The position of the invalid character
     */
    data class Invalid(val char: Char, override val startIndex: Int) : JsonToken() {
        /** The length of a single character. */
        override val length = 1
    }

    /**
     * Represents the end-of-file token.
     *
     * Signals that there are no more tokens to read from the input stream.
     * The [startIndex] and [length] are set to -1 as they are not meaningful for EOF.
     */
    data object EOF : JsonToken() {
        override val startIndex = -1
        override val length = -1
    }
}
