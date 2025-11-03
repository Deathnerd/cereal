import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*

@DisplayName("Token Tokenizer Tests")
class JsonTokenTest {

    @Test
    @DisplayName("Simple key-value pair")
    fun simpleTest() {
        val input = """{"key": 123}"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"key\"".toCharArray(), 1, 5),
            JsonToken.Colon(6),
            JsonToken.Number("123".toCharArray(), 8, 3),
            JsonToken.RightBrace(11),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Escaped quotes in strings")
    fun escapedStringTest() {
        val input = """{"key": "Hello, \"World\""}"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"key\"".toCharArray(), 1, 5),
            JsonToken.Colon(6),
            JsonToken.String(
                charArrayOf('"', 'H', 'e', 'l', 'l', 'o', ',', ' ', '\\', '"', 'W', 'o', 'r', 'l', 'd', '\\', '"', '"'),
                8, 18
            ),
            JsonToken.RightBrace(26),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Invalid characters outside strings")
    fun invalidTokensTest() {
        val input = """{"key": \"World"}"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"key\"".toCharArray(), 1, 5),
            JsonToken.Colon(6),
            JsonToken.Invalid('\\', 8),
            JsonToken.String(
                charArrayOf('"', 'W', 'o', 'r', 'l', 'd', '"'),
                9, 7
            ),
            JsonToken.RightBrace(16),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Array with multiple string elements")
    fun arrayTest() {
        val input = """["a", "b", "c"]"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBracket(0),
            JsonToken.String("\"a\"".toCharArray(), 1, 3),
            JsonToken.Comma(4),
            JsonToken.String("\"b\"".toCharArray(), 6, 3),
            JsonToken.Comma(9),
            JsonToken.String("\"c\"".toCharArray(), 11, 3),
            JsonToken.RightBracket(14),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("All keywords: true, false, null")
    fun keywordsTest() {
        val input = """{"active": true, "deleted": false, "value": null}"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"active\"".toCharArray(), 1, 8),
            JsonToken.Colon(9),
            JsonToken.Keyword("true".toCharArray(), 11, 4),
            JsonToken.Comma(15),
            JsonToken.String("\"deleted\"".toCharArray(), 17, 9),
            JsonToken.Colon(26),
            JsonToken.Keyword("false".toCharArray(), 28, 5),
            JsonToken.Comma(33),
            JsonToken.String("\"value\"".toCharArray(), 35, 7),
            JsonToken.Colon(42),
            JsonToken.Keyword("null".toCharArray(), 44, 4),
            JsonToken.RightBrace(48),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Various number formats: integers, decimals, negatives")
    fun numbersTest() {
        val input = """[123, -456, 3.14, -2.71, 0, 0.0]"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBracket(0),
            JsonToken.Number("123".toCharArray(), 1, 3),
            JsonToken.Comma(4),
            JsonToken.Number("-456".toCharArray(), 6, 4),
            JsonToken.Comma(10),
            JsonToken.Number("3.14".toCharArray(), 12, 4),
            JsonToken.Comma(16),
            JsonToken.Number("-2.71".toCharArray(), 18, 5),
            JsonToken.Comma(23),
            JsonToken.Number("0".toCharArray(), 25, 1),
            JsonToken.Comma(26),
            JsonToken.Number("0.0".toCharArray(), 28, 3),
            JsonToken.RightBracket(31),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Whitespace handling (spaces around tokens)")
    fun whitespaceTest() {
        val input = """{  "key"  :  "value"  }"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"key\"".toCharArray(), 3, 5),
            JsonToken.Colon(10),
            JsonToken.String("\"value\"".toCharArray(), 13, 7),
            JsonToken.RightBrace(22),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Nested objects and arrays")
    fun nestedStructureTest() {
        val input = """{"user": {"name": "Alice", "age": 30}}"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"user\"".toCharArray(), 1, 6),
            JsonToken.Colon(7),
            JsonToken.LeftBrace(9),
            JsonToken.String("\"name\"".toCharArray(), 10, 6),
            JsonToken.Colon(16),
            JsonToken.String("\"Alice\"".toCharArray(), 18, 7),
            JsonToken.Comma(25),
            JsonToken.String("\"age\"".toCharArray(), 27, 5),
            JsonToken.Colon(32),
            JsonToken.Number("30".toCharArray(), 34, 2),
            JsonToken.RightBrace(36),
            JsonToken.RightBrace(37),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Empty objects and arrays")
    fun emptyContainersTest() {
        val input = """{"empty_obj": {}, "empty_arr": []}"""
        val chars = CharacterStream(input)
        val tokens = JsonTokenStream(chars)

        val expectedTokens = listOf(
            JsonToken.LeftBrace(0),
            JsonToken.String("\"empty_obj\"".toCharArray(), 1, 11),
            JsonToken.Colon(12),
            JsonToken.LeftBrace(14),
            JsonToken.RightBrace(15),
            JsonToken.Comma(16),
            JsonToken.String("\"empty_arr\"".toCharArray(), 18, 11),
            JsonToken.Colon(29),
            JsonToken.LeftBracket(31),
            JsonToken.RightBracket(32),
            JsonToken.RightBrace(33),
            JsonToken.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Peek returns token without consuming it")
    fun peekDoesNotConsume() {
        val stream = CharacterStream("""{"key": "value"}""")
        val tokenStream = JsonTokenStream(stream)

        // Multiple peeks return same token without advancing
        val peek1 = tokenStream.peek()
        val peek2 = tokenStream.peek()

        assertTrue(peek1 is JsonToken.LeftBrace)
        assertEquals(peek1, peek2, "Multiple peeks should return same token")
    }

    @Test
    @DisplayName("Multiple peeks return same token")
    fun multiplePeeksReturnSame() {
        val stream = CharacterStream("""[1, 2, 3]""")
        val tokenStream = JsonTokenStream(stream)

        val peek1 = tokenStream.peek()
        val peek2 = tokenStream.peek()
        val peek3 = tokenStream.peek()

        assertTrue(peek1 is JsonToken.LeftBracket)
        assertEquals(peek1, peek2)
        assertEquals(peek2, peek3)
    }

    @Test
    @DisplayName("Peek caches token and multiple peeks return same")
    fun peekCachesToken() {
        val stream = CharacterStream("""{"x": 1}""")
        val tokenStream = JsonTokenStream(stream)

        // Peek caches first token (LeftBrace)
        val peeked1 = tokenStream.peek()
        val peeked1Again = tokenStream.peek()

        assertTrue(peeked1 is JsonToken.LeftBrace)
        assertEquals(peeked1, peeked1Again, "Multiple peeks should return same cached token")

        // Next reads a new token (String)
        val next1 = tokenStream.next()
        assertTrue(next1 is JsonToken.String)

        // Now peek returns the newly read token
        val peeked2 = tokenStream.peek()
        assertEquals(next1, peeked2)
    }

    @Test
    @DisplayName("Peek returns EOF for empty stream")
    fun peekReturnsEOFForEmptyStream() {
        val stream = CharacterStream("""""")
        val tokenStream = JsonTokenStream(stream)

        // Peek on empty stream should return EOF
        val peeked = tokenStream.peek()
        assertEquals(JsonToken.EOF, peeked)

        // Multiple peeks should still return EOF
        val peeked2 = tokenStream.peek()
        assertEquals(JsonToken.EOF, peeked2)
    }

    @Test
    @DisplayName("Peek sequence through entire token stream")
    fun peekSequenceThroughTokens() {
        val stream = CharacterStream("""[1, 2]""")
        val tokenStream = JsonTokenStream(stream)

        // Peek and verify each token type
        assertTrue(tokenStream.peek() is JsonToken.LeftBracket)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is JsonToken.Number)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is JsonToken.Comma)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is JsonToken.Number)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is JsonToken.RightBracket)
        tokenStream.next() // consume

        assertEquals(JsonToken.EOF, tokenStream.peek())
    }

    @Test
    @DisplayName("Peek consistency with toList")
    fun peekConsistencyWithToList() {
        val input = """[true, false, null]"""
        val stream1 = CharacterStream(input)
        val tokenStream1 = JsonTokenStream(stream1)

        // Collect tokens using peek/next
        val peekedTokens = mutableListOf<JsonToken>()
        while (true) {
            val token = tokenStream1.peek()
            peekedTokens.add(token)
            tokenStream1.next()
            if (token == JsonToken.EOF) break
        }

        // Collect tokens using toList
        val stream2 = CharacterStream(input)
        val tokenStream2 = JsonTokenStream(stream2)
        val listedTokens = tokenStream2.toList()

        // Should match
        assertEquals(peekedTokens.size, listedTokens.size)
        for (i in peekedTokens.indices) {
            assertEquals(peekedTokens[i], listedTokens[i])
        }
    }

    @Test
    @DisplayName("Peek with whitespace skipping")
    fun peekWithWhitespaceSkipping() {
        val stream = CharacterStream("""  {  "key"  }  """)
        val tokenStream = JsonTokenStream(stream)

        // First peek should skip whitespace and return LeftBrace
        val firstToken = tokenStream.peek()
        assertTrue(firstToken is JsonToken.LeftBrace)
        assertEquals(2, (firstToken as JsonToken.LeftBrace).startIndex)

        tokenStream.next()

        // Next peek should skip whitespace and return String
        val secondToken = tokenStream.peek()
        assertTrue(secondToken is JsonToken.String)
    }

    private fun assertTokens(tokens: JsonTokenStream, expectedTokens: List<JsonToken>) {
        val actualTokens = tokens.toList()

        assertEquals(
            expectedTokens.size,
            actualTokens.size,
            "Token lists are not the same size"
        )

        for (i in actualTokens.indices) {
            assertEquals(
                expectedTokens[i],
                actualTokens[i],
                "Token at index $i is not the same. Expected: ${expectedTokens[i]}, Got: ${actualTokens[i]}"
            )
        }
    }
}