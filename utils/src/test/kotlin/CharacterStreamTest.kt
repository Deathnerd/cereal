import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows

@DisplayName("CharacterStream Tests")
class CharacterStreamTest {

    @Test
    @DisplayName("Initialize with non-empty string")
    fun initializeWithNonEmptyString() {
        val stream = CharacterStream("hello")

        assertEquals(0, stream.index)
        assertEquals(0, stream.column)
        assertEquals(1, stream.line)
        assertEquals('h', stream.current)
        assertFalse(stream.eof)
    }

    @Test
    @DisplayName("Initialize with empty string")
    fun initializeWithEmptyString() {
        val stream = CharacterStream("")

        assertEquals(0, stream.index)
        assertEquals(0, stream.column)
        assertEquals(1, stream.line)
        assertTrue(stream.eof)
    }

    @Test
    @DisplayName("Initialize with single character")
    fun initializeWithSingleCharacter() {
        val stream = CharacterStream("x")

        assertEquals(0, stream.index)
        assertEquals('x', stream.current)
        assertFalse(stream.eof)
    }

    @Test
    @DisplayName("Advance through characters linearly")
    fun advanceThroughCharacters() {
        val stream = CharacterStream("abc")

        assertEquals('a', stream.current)
        assertEquals(0, stream.column)

        stream.advance()
        assertEquals('b', stream.current)
        assertEquals(1, stream.index)
        assertEquals(1, stream.column)

        stream.advance()
        assertEquals('c', stream.current)
        assertEquals(2, stream.index)
        assertEquals(2, stream.column)
    }

    @Test
    @DisplayName("Advance past end of string (EOF boundary)")
    fun advancePastEnd() {
        val stream = CharacterStream("ab")

        stream.advance() // index = 1
        stream.advance() // index = 2 (EOF)
        assertTrue(stream.eof)

        // Advancing when already at EOF should not change state
        stream.advance()
        assertEquals(2, stream.index)
        assertTrue(stream.eof)
    }

    @Test
    @DisplayName("EOF detection at exact boundary")
    fun eofDetection() {
        val stream = CharacterStream("hi")

        assertFalse(stream.eof)
        stream.advance()
        assertFalse(stream.eof)
        stream.advance()
        assertTrue(stream.eof)
    }

    @Test
    @DisplayName("Column tracking increments with each character")
    fun columnTracking() {
        val stream = CharacterStream("hello")

        assertEquals(0, stream.column)
        stream.advance()
        assertEquals(1, stream.column)
        stream.advance()
        assertEquals(2, stream.column)
        stream.advance()
        assertEquals(3, stream.column)
    }

    @Test
    @DisplayName("Line tracking with single newline")
    fun lineTrackingSingleNewline() {
        val stream = CharacterStream("ab\ncd")

        assertEquals(1, stream.line)
        stream.advance() // advance over 'a' → now at 'b'
        assertEquals(1, stream.column)
        stream.advance() // advance over 'b' → now at '\n'
        assertEquals(2, stream.column)
        assertEquals(1, stream.line) // line still 1, we're at the newline character
        stream.advance() // advance over '\n' → now at 'c'
        assertEquals(2, stream.line) // line incremented when we advanced over the newline
        assertEquals(0, stream.column) // column = 0 because 'c' is first char on new line
    }

    @Test
    @DisplayName("Line tracking with multiple newlines")
    fun lineTrackingMultipleNewlines() {
        val stream = CharacterStream("a\nb\nc")

        assertEquals(1, stream.line)
        stream.advance() // 'b'
        stream.advance() // '\n'
        assertEquals(2, stream.line)
        stream.advance() // 'b'
        stream.advance() // '\n'
        assertEquals(3, stream.line)
        stream.advance() // 'c'
        assertEquals(3, stream.line)
    }

    @Test
    @DisplayName("Column resets to 0 after newline")
    fun columnResetsAfterNewline() {
        val stream = CharacterStream("abc\nxyz")

        // Advance to and over the newline
        stream.advance() // advance over 'a' → at 'b'
        stream.advance() // advance over 'b' → at 'c'
        stream.advance() // advance over 'c' → at '\n'
        assertEquals(3, stream.column) // We're AT the newline, column is 3

        stream.advance() // advance over '\n' → at 'x', column resets, line increments
        assertEquals(0, stream.column) // Column reset when advancing over newline
        assertEquals(2, stream.line) // Line incremented when advancing over newline
        stream.advance() // advance over 'x' → at 'y'
        assertEquals(1, stream.column) // column now 1
    }

    @Test
    @DisplayName("Peek with zero offset returns current character")
    fun peekWithZeroOffset() {
        val stream = CharacterStream("hello")

        assertEquals('h', stream.peek(0))
        stream.advance()
        assertEquals('e', stream.peek(0))
    }

    @Test
    @DisplayName("Peek with positive offset looks ahead")
    fun peekWithPositiveOffset() {
        val stream = CharacterStream("hello")

        assertEquals('h', stream.peek(0))
        assertEquals('e', stream.peek(1))
        assertEquals('l', stream.peek(2))
        assertEquals('l', stream.peek(3))
        assertEquals('o', stream.peek(4))
    }

    @Test
    @DisplayName("Peek does not advance stream")
    fun peekDoesNotAdvance() {
        val stream = CharacterStream("hello")

        assertEquals(0, stream.index)
        stream.peek(3) // Peek but don't use result
        assertEquals(0, stream.index)
        assertEquals('h', stream.current)
    }

    @Test
    @DisplayName("Peek beyond string bounds throws exception")
    fun peekBeyondBounds() {
        val stream = CharacterStream("hi")

        assertThrows<StringIndexOutOfBoundsException> {
            stream.peek(10)
        }
    }

    @Test
    @DisplayName("Reset returns to initial state")
    fun resetReturnsToInitialState() {
        val stream = CharacterStream("hello")

        // Advance multiple times
        stream.advance()
        stream.advance()
        stream.advance()
        assertEquals(3, stream.index)
        assertEquals(3, stream.column)

        // Reset
        stream.reset()

        assertEquals(0, stream.index)
        assertEquals(0, stream.column)
        assertEquals(1, stream.line)
        assertEquals('h', stream.current)
    }

    @Test
    @DisplayName("Reset after newlines returns to initial state")
    fun resetAfterNewlines() {
        val stream = CharacterStream("a\nb\nc")

        // Advance through multiple lines
        stream.advance()
        stream.advance() // to newline
        stream.advance()
        stream.advance() // to newline
        stream.advance()

        assertEquals(5, stream.index)
        assertEquals(3, stream.line)

        // Reset should restore all to initial
        stream.reset()

        assertEquals(0, stream.index)
        assertEquals(0, stream.column)
        assertEquals(1, stream.line)
    }

    @Test
    @DisplayName("Next advances and returns new current")
    fun nextAdvancesAndReturns() {
        val stream = CharacterStream("abcd")

        stream.advance()
        stream.advance()
        // Now at index 2, current = 'c'

        // next() advances then returns the new current
        val result = stream.next()
        assertEquals('d', result)
        assertEquals(3, stream.index)
    }

    @Test
    @DisplayName("Next throws when at EOF")
    fun nextThrowsAtEOF() {
        val stream = CharacterStream("a")

        stream.advance()
        assertTrue(stream.eof)

        assertThrows<Exception> {
            stream.next()
        }
    }

    @Test
    @DisplayName("Error throws exception with message")
    fun errorThrowsException() {
        val stream = CharacterStream("hello")

        val exception = assertThrows<Exception> {
            stream.error("Test error message")
        }

        assertTrue(exception.message?.contains("Test error message") ?: false)
        assertTrue(exception.message?.contains("index 0") ?: false)
    }

    @Test
    @DisplayName("Error includes correct index in message")
    fun errorIncludesCorrectIndex() {
        val stream = CharacterStream("hello")

        stream.advance()
        stream.advance()
        stream.advance()

        val exception = assertThrows<Exception> {
            stream.error("Something went wrong")
        }

        assertTrue(exception.message?.contains("index 3") ?: false)
    }

    @Test
    @DisplayName("Complex multi-line navigation")
    fun complexMultiLineNavigation() {
        val input = "line1\nline2\nline3"
        val stream = CharacterStream(input)

        // Read through first line
        assertEquals('l', stream.current)
        assertEquals(0, stream.column)
        assertEquals(1, stream.line)

        // Advance 5 characters to reach the newline
        (0 until 5).forEach { _ -> stream.advance() }

        assertEquals('\n', stream.current)
        assertEquals(1, stream.line) // Line still 1, we're AT the newline, not past it
        assertEquals(5, stream.column)

        // Advance past the newline to the second line
        stream.advance()
        assertEquals('l', stream.current)
        assertEquals(2, stream.line) // Line incremented when we advanced over the newline
        assertEquals(0, stream.column) // Column = 0 because 'l' is first char on new line
    }

    @Test
    @DisplayName("Whitespace characters are treated normally")
    fun whitespaceCharacters() {
        val stream = CharacterStream("a b\tc")

        assertEquals('a', stream.current)
        stream.advance()
        assertEquals(' ', stream.current)
        assertEquals(1, stream.column)
        stream.advance()
        assertEquals('b', stream.current)
        assertEquals(2, stream.column)
        stream.advance()
        assertEquals('\t', stream.current)
        assertEquals(3, stream.column)
        stream.advance()
        assertEquals('c', stream.current)
        assertEquals(4, stream.column)
    }

    @Test
    @DisplayName("Consecutive newlines increment line counter")
    fun consecutiveNewlines() {
        val stream = CharacterStream(
            """a


b"""
        )

        assertEquals(1, stream.line)
        assertEquals('a', stream.current)
        stream.advance() // first '\n'
        assertEquals('\n', stream.current)
        stream.advance() // second '\n'
        assertEquals(2, stream.line)
        stream.advance() // third '\n'
        assertEquals(3, stream.line)
        stream.advance() // the b on the 4th line
        assertEquals('b', stream.current)
        assertEquals(4, stream.line)
    }

    @Test
    @DisplayName("Index correctly increments with each advance")
    fun indexIncrementsCorrectly() {
        val stream = CharacterStream("01234567")

        for (expectedIndex in 0 until 8) {
            assertEquals(expectedIndex, stream.index)
            stream.advance()
        }

        assertEquals(8, stream.index)
    }

    @Test
    @DisplayName("Peek with large offset within bounds")
    fun peekWithLargeOffsetWithinBounds() {
        val stream = CharacterStream("0123456789")

        assertEquals('0', stream.peek(0))
        assertEquals('5', stream.peek(5))
        assertEquals('9', stream.peek(9))
    }

    @Test
    @DisplayName("State consistency after multiple advances and peeks")
    fun stateConsistencyAfterMultipleOperations() {
        val stream = CharacterStream("test")

        // Peek multiple times from index 0
        assertEquals('t', stream.peek(0))
        assertEquals('e', stream.peek(1))
        assertEquals('s', stream.peek(2))
        assertEquals('t', stream.peek(3))

        // Index should not have changed
        assertEquals(0, stream.index)

        // Advance to index 1
        stream.advance()

        // Peek from new position (index 1)
        assertEquals('e', stream.peek(0))
        assertEquals('s', stream.peek(1))
        assertEquals('t', stream.peek(2))

        // Index should be 1
        assertEquals(1, stream.index)
    }
}
