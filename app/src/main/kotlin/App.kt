fun buildCharArray(body: MutableList<Char>.() -> Unit): CharArray = mutableListOf<Char>().apply(body).toCharArray()


fun isWhitespace(char: Char) = char == ' ' || char == '\t' || char == '\n' || char == '\r'
fun isDigit(char: Char) = char in '0'..'9'
fun isLetter(char: Char) = char in 'a'..'z' || char in 'A'..'Z'
fun isQuote(char: Char) = char == '"'


@ExcludeFromCoverage("Testing app functionality, not logic")
fun main(args: Array<String> = arrayOf()) {
    // Example usage: tokenize a simple JSON-like string
    val input = """{"name": "Alice", "age": 30}"""
    val stream = CharacterStream(input)
    val tokenStream = TokenStream(stream)
    val tokens = tokenStream.toList()

    println("Input: $input")
    println("Tokens:")
    tokens.forEach { token ->
        println("  $token")
    }
}