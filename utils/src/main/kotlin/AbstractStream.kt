abstract class AbstractStream<in T, out R>(private val source: T) {
    abstract fun next(): R
    abstract fun peek(offset: Int = 0): R
    abstract fun reset()
    abstract fun error(message: String): Nothing
    abstract fun advance()
}