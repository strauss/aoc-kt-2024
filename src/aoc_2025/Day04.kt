package aoc_2025

import aoc_util.PrimitiveMultiDimArray
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
    for (y in 0..<height) {
        for (x in 0..<width) {
            val current = array[x, y]
            if (current == '@' && array.countBlockedNeigbors(x, y) < 4) {
                result += 1
                markRemove.add(y * width + x)
            }
        }
    }
    if (remove) {
        for (index in markRemove) {
            val x = index % width
            val y = index / width
            array[x, y] = '.'
        }
    }
    return result
}

private fun <T> PrimitiveMultiDimArray<T>.countBlockedNeigbors(x: Int, y: Int): Int {
    return getNeighbors(x, y).asSequence()
        .filter { it == '@' }
        .count()
}

private fun <T> PrimitiveMultiDimArray<T>.height(): Int = getDimensionSize(0)
private fun <T> PrimitiveMultiDimArray<T>.width(): Int = getDimensionSize(1)

private fun <T> PrimitiveMultiDimArray<T>.getNeighbors(x: Int, y: Int): List<T> {
    val result = mutableListOf<T>()
    if (x in 0..<width() && y in 0..<height()) {
        if (y > 0) {
            result.add(this[x, y - 1]) // north
        }
        if (x < width() - 1 && y > 0) {
            result.add(this[x + 1, y - 1]) // northeast
        }
        if (x < width() - 1) {
            result.add(this[x + 1, y]) // east
        }
        if (x < width() - 1 && y < height() - 1) {
            result.add(this[x + 1, y + 1]) // southeast
        }
        if (y < height() - 1) {
            result.add(this[x, y + 1]) // south
        }
        if (x > 0 && y < height() - 1) {
            result.add(this[x - 1, y + 1]) // southwest
        }
        if (x > 0) {
            result.add(this[x - 1, y]) // west
        }
        if (x > 0 && y > 0) {
            result.add(this[x - 1, y - 1]) // northwest
        }
    }
    return result
}