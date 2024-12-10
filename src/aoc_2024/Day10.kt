package aoc_2024

import aoc_util.PrimitiveMultiDimArray
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.array.PrimitiveIntArray
import java.util.*

fun main() {
    val testList: List<String> = readInput2024("Day10_test")
    val testArray = parseInputAsMultiDimIntArray(testList)
    val testResult1 = part1(testArray)
    println("Test result 1: $testResult1")
    val testResult2 = part2(testArray)
    println("Test result 2: $testResult2")

    val list: List<String> = readInput2024("Day10")
    val array = parseInputAsMultiDimIntArray(list)
    val result1 = part1(array)
    println("Result 1: $result1")
    val result2 = part2(array)
    println("Result 2: $result2")
}

private fun part1(array: PrimitiveMultiDimArray<Int>): Int {
    val startPositions = searchFor(array, 0)
    var sum = 0
    for (pos in startPositions) {
        sum = countReachableTargets(array, pos, 9)
    }
    return sum
}

private fun searchFor(array: PrimitiveMultiDimArray<Int>, searchFor: Int): List<Pair<Int, Int>> {
    val result = ArrayList<Pair<Int, Int>>()
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    for (j in 0 until height) {
        for (i in 0 until width) {
            val currentHeight = array[j, i]
            if (currentHeight == searchFor) {
                result.add(Pair(j, i))
            }
        }
    }
    return result
}

private fun countReachableTargets(array: PrimitiveMultiDimArray<Int>, start: Pair<Int, Int>, to: Int): Int {
    var sum = 0
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val queue: Queue<Pair<Int, Int>> = LinkedList()
    val visited = HashSet<Pair<Int, Int>>()
    val parents: MutableMap<Pair<Int, Int>, Pair<Int, Int>> = HashMap()
    queue.add(start)
    visited.add(start)
    while (queue.isNotEmpty()) {
        val (y, x) = queue.poll()
        val adjacencies = getAdjacentPositions(y, x, height, width)
        for (ad in adjacencies) {
            if (!visited.contains(ad) && array[y, x] + 1 == array[ad.first, ad.second]) {
                queue.add(ad)
                visited.add(ad)
                parents[ad] = Pair(y, x)
            }
        }
    }
    val possibleEndPositions = searchFor(array, to)
    for (pos in possibleEndPositions) {
        if (parents.containsKey(pos)) {
            sum += 1
        }
    }
    return sum
}

private fun part2(array: PrimitiveMultiDimArray<Int>): Int {
    val startPositions = searchFor(array, 0)
    var sum = 0
    for (pos in startPositions) {
        sum += countPossiblePaths(array, pos, 9)
    }
    return sum
}

private fun countPossiblePaths(array: PrimitiveMultiDimArray<Int>, from: Pair<Int, Int>, to: Int): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val startValue = array[from.first, from.second]
    if (startValue == to) {
        return 1
    }
    var result = 0
    val adjacentPositions = getAdjacentPositions(from.first, from.second, height, width)
    for (ad in adjacentPositions) {
        if (array[from.first, from.second] + 1 == array[ad.first, ad.second]) {
            result += countPossiblePaths(array, ad, to)
        }
    }
    return result
}

private fun getAdjacentPositions(y: Int, x: Int, height: Int, width: Int): List<Pair<Int, Int>> {
    return mutableListOf(Pair(y + 1, x), Pair(y - 1, x), Pair(y, x + 1), Pair(y, x - 1)).filter { (y, x) ->
        y in 0..<height && x in 0..<width
    }
}

fun parseInputAsMultiDimIntArray(input: List<String>): PrimitiveMultiDimArray<Int> {
    val dim = input[0].length // I don't care about any exception here ... if empty -> bad luck
    val out: PrimitiveMultiDimArray<Int> = PrimitiveMultiDimArray(dim, dim) { size -> PrimitiveIntArray(size) }
    var j = 0
    input.forEach { line ->
        for (i in line.indices) {
            out[j, i] = line[i].toString().toInt()
        }
        j += 1
    }
    return out
}