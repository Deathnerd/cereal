/**
 * Abstract base class for token stream parsers.
 *
 * Provides a generic framework for reading, peeking, and collecting tokens from a stream source.
 * Subclasses implement token-specific parsing logic while inheriting generic stream handling behavior
 * such as caching, peeking, and batch conversion to lists.
 *
 * Type parameters:
 * - `T`: The source stream type (contravariant in input)
 * - `R`: The token/result type (covariant in output)
 *
 * The pattern uses a template method approach where:
 * - Subclasses implement [readToken] for core parsing
 * - Subclasses implement [isTokenEOF] for EOF detection
 * - Base class handles caching, peeking, and list conversion
 *
 * @param stream The underlying source stream to read tokens from
 */
abstract class AbstractTokenStream<T, R>(open val stream: T) {
    /**
     * The current token that has been peeked but not yet consumed.
     *
     * This field caches the next token to be read, enabling the [peek] method
     * to return the same token without re-parsing. Set to null after [next] is called.
     */
    protected var currentToken: R? = null

    /**
     * Reads and returns the next token from the stream.
     *
     * Reads a new token using [readToken], caches it, and returns it.
     * The cached token remains until the next call to [next].
     *
     * @return The next token of type [R]
     */
    fun next(): R {
        currentToken = readToken()
        @Suppress("UnsafeCallOnNullableType")
        return currentToken!!
    }

    /**
     * Returns the next token without consuming it.
     *
     * Allows looking ahead to the next token without advancing the stream state.
     * Uses [currentToken] cache to avoid re-parsing if already peeked.
     * On first peek, reads a token using [readToken] and caches it.
     *
     * @return The next token of type [R] without consuming it
     */
    fun peek(): R {
        if (currentToken == null) {
            currentToken = readToken()
        }
        @Suppress("UnsafeCallOnNullableType")
        return currentToken!!
    }

    /**
     * Converts the entire token stream to a list.
     *
     * Repeatedly calls [next] to read all tokens until [isTokenEOF] returns true,
     * collecting them into a list. The EOF token is included in the result.
     *
     * @return A [List] of all tokens from the stream, ending with the EOF token
     */
    fun toList(): List<R> = mutableListOf<R>().apply {
        while (true) {
            val token = next()
            add(token)
            if (isTokenEOF(token)) break
        }
    }

    /**
     * Reads a single token from the stream.
     *
     * This is the core parsing method that subclasses must implement.
     * It should read the next token from [stream] and return it.
     *
     * @return The next token parsed from the stream
     */
    protected abstract fun readToken(): R

    /**
     * Determines whether the given token represents the end of stream.
     *
     * Subclasses implement this to identify EOF tokens based on their token type.
     * For example, JsonTokenStream checks if the token equals JsonToken.EOF.
     *
     * @param token The token to check
     * @return true if the token represents EOF, false otherwise
     */
    protected abstract fun isTokenEOF(token: R): Boolean
}
