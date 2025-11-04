/**
 * Abstract base class for stream-based parsers.
 *
 * Provides a common interface for consuming elements from a source sequentially.
 * Subclasses implement the specific logic for handling different source types and element types.
 *
 * Type parameters:
 * - `T`: The source type (covariant in input) - the data being parsed
 * - `R`: The result/element type (covariant in output) - the parsed result
 *
 * @property source The source data to be parsed
 */
abstract class AbstractStream<in T, out R>(private val source: T) {
    /**
     * Reads and returns the next element from the stream.
     *
     * Advances the stream position and returns the element at that position.
     *
     * @return The next element in the stream
     * @throws Exception if called when no more elements are available
     */
    abstract fun next(): R

    /**
     * Returns an element at a specified offset without consuming it.
     *
     * Allows looking ahead in the stream without advancing the current position.
     *
     * @param offset The number of positions ahead to peek (default 0 means current position)
     * @return The element at the specified offset
     */
    abstract fun peek(offset: Int = 0): R

    /**
     * Resets the stream to its initial state.
     *
     * After calling this method, the stream will be positioned at the beginning
     * and ready to be read from the start again.
     */
    abstract fun reset()

    /**
     * Throws an exception with an error message.
     *
     * This is a convenience method for error handling that always throws,
     * making it useful in contexts that expect a non-returning expression.
     *
     * @param message The error message to include in the exception
     * @throws Exception Always throws with the provided message
     */
    abstract fun error(message: String): Nothing

    /**
     * Advances the stream position by one element.
     *
     * Moves the internal pointer to the next position in the stream.
     * Typically called after consuming an element.
     */
    abstract fun advance()
}
