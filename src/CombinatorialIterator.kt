/**
 * This class is perfectly suited for solving riddles. It takes an [iterable] as input. It generates all possible combinations of length [depth] for
 * the given input [iterable]. If [distinct] is set to true, it will avoid duplicates resulting in the creation of permutations of the input
 * [iterable].
 */
class CombinatorialIterator<T>(
    private val iterable: Iterable<T>,
    private val depth: Int,
    private val distinct: Boolean = false
) {

    /**
     * The given function [f] describes, what should happen with each resulting combination. The input list of this function is one combination as
     * list.
     */
    fun iterate(f: (List<T>) -> Unit) {
        internalIterate(emptyList(), f)
    }

    private fun internalIterate(items: List<T>, f: (List<T>) -> Unit) {
        if (items.size == depth) {
            f.invoke(items)
        } else {
            iterable.forEach {
                if (!distinct || !items.contains(it)) {
                    internalIterate(items.plus(it), f)
                }
            }
        }
    }
}