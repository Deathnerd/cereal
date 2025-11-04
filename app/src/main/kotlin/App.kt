import com.deathnerd.cereal.json.JsonTokenStream

@ExcludeFromCoverage("Testing app functionality, not logic")
fun main(args: Array<String> = arrayOf()) {
    // Example usage: tokenize a simple JSON-like string
    val input = """{"name": "Alice", "age": 30}"""
    val stream = CharacterStream(input)
    val tokenStream = JsonTokenStream(stream)
    val tokens = tokenStream.toList()

    println("Input: $input")
    println("Tokens:")
    tokens.forEach { token ->
        println("  $token")
    }
}