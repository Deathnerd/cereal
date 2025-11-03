class TokenStream(private val stream: CharacterStream) {
    private var currentToken: JsonToken? = null

    fun next(): JsonToken {
        currentToken = readToken()
        return currentToken!!
    }

    fun peek(): JsonToken {
        if (currentToken == null) {
            if(stream.eof) return JsonToken.EOF
            currentToken = readToken()
        }
        return currentToken!!
    }

    private fun readToken(): JsonToken {
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

    private fun readLeftBrace(): JsonToken.LeftBrace {
        val start = stream.index
        stream.advance();
        return JsonToken.LeftBrace(start)
    }

    private fun readRightBrace(): JsonToken.RightBrace {
        val start = stream.index
        stream.advance();
        return JsonToken.RightBrace(start)
    }

    private fun readLeftBracket(): JsonToken.LeftBracket {
        val start = stream.index
        stream.advance();
        return JsonToken.LeftBracket(start)
    }

    private fun readRightBracket(): JsonToken.RightBracket {
        val start = stream.index
        stream.advance();
        return JsonToken.RightBracket(start)
    }

    private fun readComma(): JsonToken.Comma {
        val start = stream.index
        stream.advance();
        return JsonToken.Comma(start)
    }

    private fun readColon(): JsonToken.Colon {
        val start = stream.index
        stream.advance();
        return JsonToken.Colon(start)
    }

    private fun readInvalidCharacter(): JsonToken.Invalid {
        // Just tokenize bad characters as invalid and we'll deal with them later
        val start = stream.index
        val char = stream.current
        stream.advance()
        return JsonToken.Invalid(char, start)
    }

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

    private fun readNumber(): JsonToken.Number {
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
        return JsonToken.Number(value, start, value.size)
    }

    private fun readString(): JsonToken.String {
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

        return JsonToken.String(value, start, value.size)
    }

    private fun skipWhitespace() {
        while (!stream.eof && isWhitespace(stream.current)) {
            stream.advance()
        }
    }

    fun toList(): List<JsonToken> = mutableListOf<JsonToken>().apply {
        while (true) {
            val token = next()
            add(token)
            if(token == JsonToken.EOF) break
        }
    }
}