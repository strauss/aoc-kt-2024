package aoc_2025

import aoc_util.PrimitiveMultiDimArray
import aoc_util.getAdjacentValues
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2025
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

fun main() {
    val testInput: List<String> = readInput2025("Day04_test")
    val testArray: PrimitiveMultiDimArray<Char> = parseInputAsMultiDimArray(testInput)
    val testResult = countFreeRolls(testArray)
    println("Test result: $testResult")
    val testClean = cleanUp(testArray)
    println("Test removed: $testClean")

    val input: List<String> = readInput2025("Day04")
    val array = parseInputAsMultiDimArray(input)
    val result = countFreeRolls(array)
    println("Result: $result")
    val clean = cleanUp(array)
    println("Removed: $clean")
}

private fun cleanUp(array: PrimitiveMultiDimArray<Char>): Int {
    var result = 0

    while (true) {
        val removed = countFreeRolls(array, true)
        if (removed == 0) {
            break
        }
        result += removed
    }

    return result
}

private fun countFreeRolls(array: PrimitiveMultiDimArray<Char>, remove: Boolean = false): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val markRemove = PrimitiveIntSetB()
    var result = 0
    for (row in 0..<height) {
        for (col in 0..<width) {
            val current = array[row, col]
            if (current == '@' && array.countBlockedNeighbors(row, col) < 4) {
                result += 1
                markRemove.add(row * width + col)
            }
        }
    }
    if (remove) {
        for (index in markRemove) {
            val col = index % width
            val row = index / width
            array[row, col] = '.'
        }
    }
    return result
}

private fun <T> PrimitiveMultiDimArray<T>.countBlockedNeighbors(row: Int, col: Int): Int {
    return getAdjacentValues(row, col) { it == '@' }.count()
}

