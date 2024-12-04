import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import kotlin.math.abs

fun main() {
    val testInput = readInput("Day01_test")
    println("Test results")
    part1(testInput).println()
    part2(testInput).println()
    println()

    val input = readInput("Day01")
    println("Results")
    part1(input).println()
    part2(input).println()
}

private fun part1(input: List<String>): Int {
    val (leftList, rightList) = parseInput(input)
    val sorted1: MutableList<Int> = ArrayList(leftList)
    sorted1.sort()
    val sorted2: MutableList<Int> = ArrayList(rightList)
    sorted2.sort()

    val absDifferenceList = ArrayList<Int>()

    val it1 = sorted1.iterator()
    val it2 = sorted2.iterator()

    while (it1.hasNext()) {
        assert(it2.hasNext())
        absDifferenceList.add(abs(it1.next() - it2.next()))
    }

    return absDifferenceList.sum()
}

private fun part2(input: List<String>): Int {
    val (leftList, rightList) = parseInput(input)
    val countMap: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    rightList.forEach {
        countMap[it] = countMap.getOrZero(it) + 1
    }
    var similarityScore = 0
    leftList.forEach {
        similarityScore += it * countMap.getOrZero(it)
    }

    return similarityScore
}

private fun parseInput(input: List<String>): Pair<List<Int>, List<Int>> {
    val leftList: MutableList<Int> = PrimitiveIntArrayList()
    val rightList: MutableList<Int> = PrimitiveIntArrayList()
    val splitPattern = Regex("\\s+")

    for (line in input) {
        val split = line.split(splitPattern)
        leftList.add(split[0].toInt())
        rightList.add(split[1].toInt())
    }

    return Pair(leftList, rightList)
}

private fun Map<Int, Int>.getOrZero(key: Int) = this[key] ?: 0
