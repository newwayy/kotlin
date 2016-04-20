// WITH_RUNTIME
// INTENTION_TEXT: "Replace with 'mapIndexed{}.firstOrNull{}'"
fun foo(list: List<String>): Int? {
    <caret>for ((index, s) in list.withIndex()) {
        val x = s.length * index
        if (x > 0) return x
    }
    return null
}