/**
 * Builds and returns a character array by applying a user-provided block of operations
 * on a mutable list of characters.
 *
 * @param body A lambda block of operations to be applied on a `MutableList<Char>`. This can
 *             include adding, modifying, or removing characters to construct the desired array.
 * @return A `CharArray` containing the characters after the operations specified in the `body` block.
 */
fun buildCharArray(body: MutableList<Char>.() -> Unit): CharArray = mutableListOf<Char>().apply(body).toCharArray()
/**
 * Determines whether the given character is a whitespace character.
 *
 * A character is considered whitespace if it is one of the following:
 * - Space (' ')
 * - Tab ('\t')
 * - Newline ('\n')
 * - Carriage return ('\r')
 *
 * @param char The character to be checked.
 * @return `true` if the character is a whitespace character, `false` otherwise.
 */
fun isWhitespace(char: Char) = char == ' ' || char == '\t' || char == '\n' || char == '\r'
/**
 * Checks if the given character is a numerical digit (0-9).
 *
 * @param char The character to check.
 * @return `true` if the character is a digit, otherwise `false`.
 */
fun isDigit(char: Char) = char in '0'..'9'
/**
 * Determines if the given character is a letter (either uppercase or lowercase).
 *
 * @param char The character to check.
 * @return `true` if the character is an uppercase or lowercase letter, `false` otherwise.
 */
fun isLetter(char: Char) = char in 'a'..'z' || char in 'A'..'Z'
/**
 * Checks if the provided character is a double quote (`"`).
 *
 * @param char the character to evaluate
 * @return `true` if the character is a double quote, `false` otherwise
 */
fun isQuote(char: Char) = char == '"'