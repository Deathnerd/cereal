import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*

@DisplayName("Token Tokenizer Tests")
class TokenTest {

    @Test
    @DisplayName("Simple key-value pair")
    fun simpleTest() {
        val input = """{"key": 123}"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"key\"".toCharArray(), 1, 5),
            Token.Colon(6),
            Token.Number("123".toCharArray(), 8, 3),
            Token.RightBrace(11),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Escaped quotes in strings")
    fun escapedStringTest() {
        val input = """{"key": "Hello, \"World\""}"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"key\"".toCharArray(), 1, 5),
            Token.Colon(6),
            Token.String(
                charArrayOf('"', 'H', 'e', 'l', 'l', 'o', ',', ' ', '\\', '"', 'W', 'o', 'r', 'l', 'd', '\\', '"', '"'),
                8, 18
            ),
            Token.RightBrace(26),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Invalid characters outside strings")
    fun invalidTokensTest() {
        val input = """{"key": \"World"}"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"key\"".toCharArray(), 1, 5),
            Token.Colon(6),
            Token.Invalid('\\', 8),
            Token.String(
                charArrayOf('"', 'W', 'o', 'r', 'l', 'd', '"'),
                9, 7
            ),
            Token.RightBrace(16),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Array with multiple string elements")
    fun arrayTest() {
        val input = """["a", "b", "c"]"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBracket(0),
            Token.String("\"a\"".toCharArray(), 1, 3),
            Token.Comma(4),
            Token.String("\"b\"".toCharArray(), 6, 3),
            Token.Comma(9),
            Token.String("\"c\"".toCharArray(), 11, 3),
            Token.RightBracket(14),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("All keywords: true, false, null")
    fun keywordsTest() {
        val input = """{"active": true, "deleted": false, "value": null}"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"active\"".toCharArray(), 1, 8),
            Token.Colon(9),
            Token.Keyword("true".toCharArray(), 11, 4),
            Token.Comma(15),
            Token.String("\"deleted\"".toCharArray(), 17, 9),
            Token.Colon(26),
            Token.Keyword("false".toCharArray(), 28, 5),
            Token.Comma(33),
            Token.String("\"value\"".toCharArray(), 35, 7),
            Token.Colon(42),
            Token.Keyword("null".toCharArray(), 44, 4),
            Token.RightBrace(48),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Various number formats: integers, decimals, negatives")
    fun numbersTest() {
        val input = """[123, -456, 3.14, -2.71, 0, 0.0]"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBracket(0),
            Token.Number("123".toCharArray(), 1, 3),
            Token.Comma(4),
            Token.Number("-456".toCharArray(), 6, 4),
            Token.Comma(10),
            Token.Number("3.14".toCharArray(), 12, 4),
            Token.Comma(16),
            Token.Number("-2.71".toCharArray(), 18, 5),
            Token.Comma(23),
            Token.Number("0".toCharArray(), 25, 1),
            Token.Comma(26),
            Token.Number("0.0".toCharArray(), 28, 3),
            Token.RightBracket(31),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Whitespace handling (spaces around tokens)")
    fun whitespaceTest() {
        val input = """{  "key"  :  "value"  }"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"key\"".toCharArray(), 3, 5),
            Token.Colon(10),
            Token.String("\"value\"".toCharArray(), 13, 7),
            Token.RightBrace(22),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Nested objects and arrays")
    fun nestedStructureTest() {
        val input = """{"user": {"name": "Alice", "age": 30}}"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"user\"".toCharArray(), 1, 6),
            Token.Colon(7),
            Token.LeftBrace(9),
            Token.String("\"name\"".toCharArray(), 10, 6),
            Token.Colon(16),
            Token.String("\"Alice\"".toCharArray(), 18, 7),
            Token.Comma(25),
            Token.String("\"age\"".toCharArray(), 27, 5),
            Token.Colon(32),
            Token.Number("30".toCharArray(), 34, 2),
            Token.RightBrace(36),
            Token.RightBrace(37),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Empty objects and arrays")
    fun emptyContainersTest() {
        val input = """{"empty_obj": {}, "empty_arr": []}"""
        val chars = CharacterStream(input)
        val tokens = TokenStream(chars)

        val expectedTokens = listOf(
            Token.LeftBrace(0),
            Token.String("\"empty_obj\"".toCharArray(), 1, 11),
            Token.Colon(12),
            Token.LeftBrace(14),
            Token.RightBrace(15),
            Token.Comma(16),
            Token.String("\"empty_arr\"".toCharArray(), 18, 11),
            Token.Colon(29),
            Token.LeftBracket(31),
            Token.RightBracket(32),
            Token.RightBrace(33),
            Token.EOF
        )

        assertTokens(tokens, expectedTokens)
    }

    @Test
    @DisplayName("Peek returns token without consuming it")
    fun peekDoesNotConsume() {
        val stream = CharacterStream("""{"key": "value"}""")
        val tokenStream = TokenStream(stream)

        // Multiple peeks return same token without advancing
        val peek1 = tokenStream.peek()
        val peek2 = tokenStream.peek()

        assertTrue(peek1 is Token.LeftBrace)
        assertEquals(peek1, peek2, "Multiple peeks should return same token")
    }

    @Test
    @DisplayName("Multiple peeks return same token")
    fun multiplePeeksReturnSame() {
        val stream = CharacterStream("""[1, 2, 3]""")
        val tokenStream = TokenStream(stream)

        val peek1 = tokenStream.peek()
        val peek2 = tokenStream.peek()
        val peek3 = tokenStream.peek()

        assertTrue(peek1 is Token.LeftBracket)
        assertEquals(peek1, peek2)
        assertEquals(peek2, peek3)
    }

    @Test
    @DisplayName("Peek caches token and multiple peeks return same")
    fun peekCachesToken() {
        val stream = CharacterStream("""{"x": 1}""")
        val tokenStream = TokenStream(stream)

        // Peek caches first token (LeftBrace)
        val peeked1 = tokenStream.peek()
        val peeked1Again = tokenStream.peek()

        assertTrue(peeked1 is Token.LeftBrace)
        assertEquals(peeked1, peeked1Again, "Multiple peeks should return same cached token")

        // Next reads a new token (String)
        val next1 = tokenStream.next()
        assertTrue(next1 is Token.String)

        // Now peek returns the newly read token
        val peeked2 = tokenStream.peek()
        assertEquals(next1, peeked2)
    }

    @Test
    @DisplayName("Peek returns EOF for empty stream")
    fun peekReturnsEOFForEmptyStream() {
        val stream = CharacterStream("""""")
        val tokenStream = TokenStream(stream)

        // Peek on empty stream should return EOF
        val peeked = tokenStream.peek()
        assertEquals(Token.EOF, peeked)

        // Multiple peeks should still return EOF
        val peeked2 = tokenStream.peek()
        assertEquals(Token.EOF, peeked2)
    }

    @Test
    @DisplayName("Peek sequence through entire token stream")
    fun peekSequenceThroughTokens() {
        val stream = CharacterStream("""[1, 2]""")
        val tokenStream = TokenStream(stream)

        // Peek and verify each token type
        assertTrue(tokenStream.peek() is Token.LeftBracket)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is Token.Number)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is Token.Comma)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is Token.Number)
        tokenStream.next() // consume

        assertTrue(tokenStream.peek() is Token.RightBracket)
        tokenStream.next() // consume

        assertEquals(Token.EOF, tokenStream.peek())
    }

    @Test
    @DisplayName("Peek consistency with toList")
    fun peekConsistencyWithToList() {
        val input = """[true, false, null]"""
        val stream1 = CharacterStream(input)
        val tokenStream1 = TokenStream(stream1)

        // Collect tokens using peek/next
        val peekedTokens = mutableListOf<Token>()
        while (true) {
            val token = tokenStream1.peek()
            peekedTokens.add(token)
            tokenStream1.next()
            if (token == Token.EOF) break
        }

        // Collect tokens using toList
        val stream2 = CharacterStream(input)
        val tokenStream2 = TokenStream(stream2)
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
        val tokenStream = TokenStream(stream)

        // First peek should skip whitespace and return LeftBrace
        val firstToken = tokenStream.peek()
        assertTrue(firstToken is Token.LeftBrace)
        assertEquals(2, (firstToken as Token.LeftBrace).startIndex)

        tokenStream.next()

        // Next peek should skip whitespace and return String
        val secondToken = tokenStream.peek()
        assertTrue(secondToken is Token.String)
    }

    private fun assertTokens(tokens: TokenStream, expectedTokens: List<Token>) {
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