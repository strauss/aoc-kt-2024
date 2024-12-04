import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import kotlin.math.abs

fun main() {
    val exampleList: List<List<Int>> = parseInput(readInput("Day02_test"))
    exampleList[2].isSafeWithTolerance()

    val exampleResult = exampleList.asSequence()
        .filter { it.isSafe() }
        .count()

    println("Example Result: $exampleResult")
    val exampleResultWithTolerance = exampleList.asSequence()
        .filter { it.isSafeWithTolerance() }
        .count()
    println("Example Result with tolerance: $exampleResultWithTolerance")

    val inputList = parseInput(readInput("Day02"))
    val result = inputList.asSequence()
        .filter { it.isSafe() }
        .count()
    println("Result: $result")
    val resultWithTolerance = inputList.asSequence()
        .filter { it.isSafeWithTolerance() }
        .count()
    println("Result with tolerance: $resultWithTolerance")
}

fun List<Int>.isSafe(): Boolean = (this.isSorted() || this.reversed().isSorted()) && this.allClose(1, 3)

fun List<Int>.isSafeWithTolerance(): Boolean {
    if (this.isSafe()) {
        return true
    }
    for (i: Int in 0..lastIndex) {
        val reducedList: MutableList<Int> = PrimitiveIntArrayList()
        for (j: Int in 0..lastIndex) {
            if (i != j) {
                reducedList.add(this[j])
            }
        }
        if (reducedList.isSafe()) {
            return true
        }
    }
    return false
}

fun <C : Comparable<C>> List<C>.isSorted(): Boolean {
    if (this.isEmpty()) {
        return true
    }
    for (i: Int in 1..lastIndex) {
        if (this[i] < this[i - 1]) return false
    }
    return true
}

fun List<Int>.allClose(minDistance: Int = 1, maxDistance: Int = 3): Boolean {
    if (this.size < 2) {
        return true
    }
    for (i: Int in 1..lastIndex) {
        val distance = abs(this[i] - this[i - 1])
        if (distance < minDistance || distance > maxDistance) {
            return false
        }
    }
    return true
}

private fun parseInput(input: List<String>): List<List<Int>> {
    val result = mutableListOf<List<Int>>()
    val splitPattern = Regex("\\s+")
    input.forEach { line ->
        val split: List<String> = line.split(splitPattern)
        result.add(split.map { it.toInt() })
    }
    return result
}