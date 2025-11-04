package com.deathnerd.cereal.json

import AbstractTokenStream
import CharacterStream
import buildCharArray
import isDigit
import isLetter
import isQuote
import isWhitespace

/**
 * A token stream that converts a character stream into a sequence of JSON tokens.
 *
 * This class wraps a [CharacterStream] and provides methods to read individual tokens
 * from the JSON input. It handles the parsing of all JSON token types including
 * structural tokens (braces, brackets, punctuation), literals (strings, numbers, keywords),
 * and whitespace skipping.
 *
 * Inherits generic token sequencing behavior (next, peek, toList) from [AbstractTokenStream]
 * and implements JSON-specific token parsing logic.
 *
 * @param stream The underlying character stream to read from
 */
@Suppress("TooManyFunctions")
class JsonTokenStream(override val stream: CharacterStream) : AbstractTokenStream<CharacterStream, JsonToken>(stream) {
    /**
     * Determines whether the given token represents the end of the JSON stream.
     *
     * @param token The token to check
     * @return true if the token is [JsonToken.EOF], false otherwise
     */
    override fun isTokenEOF(token: JsonToken): Boolean = token == JsonToken.EOF

    /**
     * Reads a single token from the character stream.
     *
     * This is the core tokenization logic that:
     * 1. Skips any leading whitespace
     * 2. Checks for EOF and returns [JsonToken.EOF] if reached
     * 3. Identifies the token type based on the current character
     * 4. Calls the appropriate parsing function for that token type
     *
     * @return The next [JsonToken] parsed from the stream
     */
    override fun readToken(): JsonToken {
        skipWhitespace()
        if (stream.eof) return JsonToken.EOF
        return when (stream.current) {
            '{' -> readLeftBrace()
            '}' -> readRightBrace()
            '[' -> readLeftBracket()
            ']' -> readRightBracket()
            ',' -> readComma()
            ':' -> readColon()
            '"' -> readString()
            in '0'..'9', '-' -> readNumber()
            in 'a'..'z' -> readKeyword()
            in 'A'..'Z' -> readKeyword()
            else -> readInvalidCharacter()
        }
    }

    /**
     * Reads a left brace token (`{`).
     *
     * @return A [JsonToken.LeftBrace] token
     */
    private fun readLeftBrace(): JsonToken.LeftBrace {
        val start = stream.index
        stream.advance()
        return JsonToken.LeftBrace(start)
    }

    /**
     * Reads a right brace token (`}`).
     *
     * @return A [JsonToken.RightBrace] token
     */
    private fun readRightBrace(): JsonToken.RightBrace {
        val start = stream.index
        stream.advance()
        return JsonToken.RightBrace(start)
    }

    /**
     * Reads a left bracket token (`[`).
     *
     * @return A [JsonToken.LeftBracket] token
     */
    private fun readLeftBracket(): JsonToken.LeftBracket {
        val start = stream.index
        stream.advance()
        return JsonToken.LeftBracket(start)
    }

    /**
     * Reads a right bracket token (`]`).
     *
     * @return A [JsonToken.RightBracket] token
     */
    private fun readRightBracket(): JsonToken.RightBracket {
        val start = stream.index
        stream.advance()
        return JsonToken.RightBracket(start)
    }

    /**
     * Reads a comma token (`,`).
     *
     * @return A [JsonToken.Comma] token
     */
    private fun readComma(): JsonToken.Comma {
        val start = stream.index
        stream.advance()
        return JsonToken.Comma(start)
    }

    /**
     * Reads a colon token (`:`).
     *
     * @return A [JsonToken.Colon] token
     */
    private fun readColon(): JsonToken.Colon {
        val start = stream.index
        stream.advance()
        return JsonToken.Colon(start)
    }

    /**
     * Reads an invalid or unexpected character.
     *
     * Used when the parser encounters a character that doesn't match any valid JSON token.
     * Records the offending character for later error reporting.
     *
     * @return A [JsonToken.Invalid] token
     */
    private fun readInvalidCharacter(): JsonToken.Invalid {
        // Just tokenize bad characters as invalid and we'll deal with them later
        val start = stream.index
        val char = stream.current
        stream.advance()
        return JsonToken.Invalid(char, start)
    }

    /**
     * Reads a keyword token (true, false, null).
     *
     * Consumes all consecutive letters starting from the current position
     * to form a keyword token.
     *
     * @return A [JsonToken.Keyword] token
     */
    private fun readKeyword(): JsonToken.Keyword {
        val start = stream.index
        val chars = buildCharArray {
            addLast(stream.current)
            stream.advance()
            while (!stream.eof && isLetter(stream.current)) {
                addLast(stream.current)
                stream.advance()
            }
        }
        return JsonToken.Keyword(chars, start, chars.size)
    }

    /**
     * Reads a number token.
     *
     * Parses numeric values including:
     * - Integers (e.g., 42)
     * - Negative numbers (e.g., -3.14)
     * - Decimal numbers (e.g., 3.14)
     *
     * Validates that numbers don't have multiple decimal points and throws
     * an error if one is encountered.
     *
     * @return A [JsonToken.Number] token
     * @throws Exception if the number has two decimal points
     */
    private fun readNumber(): JsonToken.Number {
        val start = stream.index
        val value = buildCharArray {
            var hasDecimal = stream.current == '.'
            addLast(stream.current)
            stream.advance()
            while (!stream.eof && (isDigit(stream.current) || stream.current == '.')) {
                if (stream.current == '.') {
                    if (hasDecimal) {
                        stream.error("Number has two decimal points")
                    }
                    hasDecimal = true
                }
                addLast(stream.current)
                stream.advance()
            }
        }
        return JsonToken.Number(value, start, value.size)
    }

    /**
     * Reads a string token.
     *
     * Parses a JSON string literal from the opening quote to the closing quote.
     * Handles escape sequences (including escaped quotes and backslashes).
     * Throws an error if the string reaches EOF without a closing quote.
     *
     * @return A [JsonToken.String] token (including the surrounding quotes)
     * @throws Exception if the string is unterminated (reaches EOF without closing quote)
     */
    private fun readString(): JsonToken.String {
        val start = stream.index
        val value = buildCharArray {
            addLast(stream.current)
            stream.advance()
            while (!isQuote(stream.current)) {
                if (stream.eof) {
                    stream.error("Unterminated string")
                }
                if (stream.current == '\\') {
                    addLast(stream.current)
                    stream.advance()
                }
                addLast(stream.current)
                stream.advance()
            }
            addLast(stream.current)
            stream.advance()
        }

        return JsonToken.String(value, start, value.size)
    }

    /**
     * Skips all consecutive whitespace characters in the stream.
     *
     * Whitespace includes spaces, tabs, newlines, and carriage returns.
     * Advances the stream past all whitespace without producing tokens.
     */
    private fun skipWhitespace() {
        while (!stream.eof && isWhitespace(stream.current)) {
            stream.advance()
        }
    }

}
