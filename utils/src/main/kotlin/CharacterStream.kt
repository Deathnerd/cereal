class CharacterStream(private val source: String) : AbstractStream<String, Char>(source) {
    var index = 0
    var column = 0
    var line = 1
    val current: Char get() = if (eof) '\u0000' else source[index]
    override fun next(): Char {
        advance()
        if (eof) throw Exception("End of input reached")
        return current
    }

    val eof: Boolean get() = index >= source.length
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

    override fun peek(offset: Int): Char = source[index + offset]
    override fun reset() {
        index = 0
        column = 0
        line = 1
    }

    override fun error(message: String): Nothing = throw Exception("Error at index $index: $message")
}