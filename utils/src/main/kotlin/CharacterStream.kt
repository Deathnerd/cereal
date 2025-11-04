/**
 * A character-based stream implementation for parsing input text.
 *
 * This class provides sequential access to individual characters in a string,
 * along with position tracking (index, column, and line number). It extends
 * [AbstractStream] with a specific implementation for character streams.
 *
 * The stream tracks:
 * - [index]: The current character position in the input
 * - [column]: The current column number on the current line (0-indexed)
 * - [line]: The current line number (1-indexed)
 *
 * @param source The input string to parse character by character
 */
class CharacterStream(private val source: String) : AbstractStream<String, Char>() {
    /**
     * The current position (index) in the input string.
     *
     * Starts at 0 for the first character. When [index] reaches [source.length],
     * the stream is at EOF.
     */
    var index = 0

    /**
     * The column number of the current character on its line.
     *
     * 0-indexed position within the current line. Resets to 0 when a newline
     * is encountered.
     */
    var column = 0

    /**
     * The line number of the current character.
     *
     * 1-indexed (first line is 1). Incremented when a newline character is encountered.
     */
    var line = 1

    /**
     * Whether the stream has reached the end of input.
     *
     * @return true if [index] is at or beyond the length of the input, false otherwise
     */
    val eof: Boolean get() = index >= source.length

    /**
     * The current character at the [index] position.
     *
     * Returns the character at the current position, or a null character ('\u0000') if EOF.
     *
     * @return The current character, or null character if at EOF
     */
    val current: Char get() = if (eof) '\u0000' else source[index]

    /**
     * Reads and returns the next character, advancing the stream position.
     *
     * Calls [advance] to move to the next position, then returns the current character.
     * Throws an exception if already at EOF.
     *
     * @return The character at the new position
     * @throws EndOfStreamException if the stream is at EOF
     */
    override fun next(): Char {
        advance()
        if (eof) throw EndOfStreamException("End of input reached")
        return current
    }

    /**
     * Advances the stream position to the next character.
     *
     * Updates the [index], and adjusts [line] and [column] appropriately:
     * - When a newline is encountered: increments [line] and resets [column] to 0
     * - For other characters: increments [column]
     *
     * Does nothing if already at EOF.
     */
    override fun advance() {
        if (eof) return
        // For now, we won't tokenize newlines. TODO: Tokenize newlines?
        if (current == '\n') {
            line++
            column = 0
        } else {
            column++
        }
        index++
    }

    /**
     * Peeks at a character at a specified offset ahead without consuming it.
     *
     * Returns the character at [index] + [offset] without advancing the stream.
     *
     * @param offset The number of positions ahead to peek (default 0 means current position)
     * @return The character at the specified offset
     * @throws IndexOutOfBoundsException if the offset exceeds the bounds of the input
     */
    override fun peek(offset: Int): Char = source[index + offset]

    /**
     * Resets the stream to its initial state.
     *
     * Resets [index] to 0, [column] to 0, and [line] to 1,
     * allowing the stream to be read again from the beginning.
     */
    override fun reset() {
        index = 0
        column = 0
        line = 1
    }

    /**
     * Throws an exception indicating an error at the current stream position.
     *
     * The exception message includes the current [index] and the provided error message
     * to help with debugging and error reporting.
     *
     * @param message The error message describing what went wrong
     * @throws StreamError Always throws with the error details
     */
    override fun error(message: String): Nothing = throw StreamError("Error at index $index: $message")
}
