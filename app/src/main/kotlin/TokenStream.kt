class TokenStream(private val stream: CharacterStream) {
    private var currentToken: Token? = null

    fun next(): Token {
        currentToken = readToken()
        return currentToken!!
    }

    fun peek(): Token {
        if (currentToken == null) {
            if(stream.eof) return Token.EOF
            currentToken = readToken()
        }
        return currentToken!!
    }

    private fun readToken(): Token {
        skipWhitespace()
        if (stream.eof) return Token.EOF
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

    private fun readLeftBrace(): Token.LeftBrace {
        val start = stream.index
        stream.advance();
        return Token.LeftBrace(start)
    }

    private fun readRightBrace(): Token.RightBrace {
        val start = stream.index
        stream.advance();
        return Token.RightBrace(start)
    }

    private fun readLeftBracket(): Token.LeftBracket {
        val start = stream.index
        stream.advance();
        return Token.LeftBracket(start)
    }

    private fun readRightBracket(): Token.RightBracket {
        val start = stream.index
        stream.advance();
        return Token.RightBracket(start)
    }

    private fun readComma(): Token.Comma {
        val start = stream.index
        stream.advance();
        return Token.Comma(start)
    }

    private fun readColon(): Token.Colon {
        val start = stream.index
        stream.advance();
        return Token.Colon(start)
    }

    private fun readInvalidCharacter(): Token.Invalid {
        // Just tokenize bad characters as invalid and we'll deal with them later
        val start = stream.index
        val char = stream.current
        stream.advance()
        return Token.Invalid(char, start)
    }

    private fun readKeyword(): Token.Keyword {
        val start = stream.index
        val chars = buildCharArray {
            addLast(stream.current)
            stream.advance()
            while (!stream.eof && isLetter(stream.current)) {
                addLast(stream.current)
                stream.advance()
            }
        }
        return Token.Keyword(chars, start, chars.size)
    }

    private fun readNumber(): Token.Number {
        val start = stream.index
        val value = buildCharArray {
            var hasDecimal = stream.current == '.'
            addLast(stream.current)
            stream.advance()
            while(!stream.eof && (isDigit(stream.current) || stream.current == '.')) {
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
        return Token.Number(value, start, value.size)
    }

    private fun readString(): Token.String {
        val start = stream.index
        val value = buildCharArray {
            addLast(stream.current)
            stream.advance()
            while (!isQuote(stream.current)) {
                if (stream.eof) {
                    stream.error("Unterminated string")
                }
                if(stream.current == '\\') {
                    addLast(stream.current)
                    stream.advance()
                }
                addLast(stream.current)
                stream.advance()
            }
            addLast(stream.current)
            stream.advance()
        }

        return Token.String(value, start, value.size)
    }

    private fun skipWhitespace() {
        while (!stream.eof && isWhitespace(stream.current)) {
            stream.advance()
        }
    }

    fun toList(): List<Token> = mutableListOf<Token>().apply {
        while (true) {
            val token = next()
            add(token)
            if(token == Token.EOF) break
        }
    }
}