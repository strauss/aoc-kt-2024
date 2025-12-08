package aoc_2023

import aoc_util.allEquals
import aoc_util.extractInts
import aoc_util.readInput2023
import aoc_util.solve
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList

fun main() {
    val testLines = readInput2023("Day09_test")
    val testInput = parse(testLines)
    solve("Test", testInput, ::extrapolate)

    val lines = readInput2023("Day09")
    val input = parse(lines)
    solve("Result", input, ::extrapolate)
}

private fun extrapolate(input: List<List<Int>>): Pair<Long, Long> {
    var result1 = 0L
    var result2 = 0L
    for (currentList in input) {
        val (current1, current2) = extrapolateOne(currentList)
        result1 += current1
        result2 += current2
    }
    return result1 to result2
}

private fun extrapolateOne(list: List<Int>): Pair<Long, Long> {
    val workList = ArrayList<List<Int>>()
    workList.add(list)
    while (!(workList.last().allEquals() && workList.last().first() == 0)) {
        val currentList = workList.last()
        val nextList = PrimitiveIntArrayList()
        for (idx in 0..currentList.size - 2) {
            // one less, so we can compute the deltas
            nextList.add(currentList[idx + 1] - currentList[idx])
        }
        workList.add(nextList)
    }
    val right = workList.asSequence().map { it.last().toLong() }.sum()
    var left = 0L
    for (idx in 1..<workList.size) {
        val i = workList.size - idx - 1
        left = workList[i].first().toLong() - left
    }
    return left to right
}

private fun parse(lines: List<String>): List<List<Int>> {
    return buildList {
        for (line in lines) {
            add(line.extractInts())
        }
    }
}