package aoc_2024

import aoc_util.PrimitiveMultiDimArray
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2024

fun main() {
    val testInputList = readInput2024("Day06_test")
    val testInput = parseInputAsMultiDimArray(testInputList)
    println(part1(testInput))
    println(part2(testInput))


    val inputList = readInput2024("Day06")
    val input = parseInputAsMultiDimArray(inputList)
    println(part1(input))
    println(part2(input))
}

private const val north = 0
private const val east = 1
private const val south = 2
private const val west = 3

private fun part1(array: PrimitiveMultiDimArray<Char>): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)

    var direction = north
    var sum = 0
    var (y, x) = searchPosition(array, '^')
    val visited: MutableSet<Pair<Int, Int>> = HashSet()
    while (y in 0..<height && x in 0..<width) {
        val (j, i) = nextAdjacency(x, y, direction)
        if (j < 0 || j >= height || i < 0 || i >= width) {
            break
        }
        if (array[j, i] == '#') {
            direction = (direction + 1) % 4
            continue
        }
        y = j
        x = i
        val Position = Pair(y, x)
        if (!visited.contains(Position)) {
            sum += 1
            visited.add(Position)
        }
    }
    return sum
}

private fun part2(array: PrimitiveMultiDimArray<Char>): Int {
    var sum = 0
    for (j in 0 until array.getDimensionSize(0)) {
        for (i in 0 until array.getDimensionSize(1)) {
            val currentChar = array[j, i]
            if (currentChar == '.') {
                array[j, i] = '#'
                sum += checkPart2(array)
                array[j, i] = '.'
            }
        }
    }
    return sum
}

private fun checkPart2(array: PrimitiveMultiDimArray<Char>): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)

    var direction = north
    var (y, x) = searchPosition(array, '^')
    val visited: MutableSet<Pair<Int, Int>> = HashSet()
    val directionAtPosition: MutableMap<Pair<Int, Int>, Int> = HashMap()
    while (y in 0..<height && x in 0..<width) {
        val (j, i) = nextAdjacency(x, y, direction)
        if (j < 0 || j >= height || i < 0 || i >= width) {
            break
        }
        if (array[j, i] == '#') {
            direction = (direction + 1) % 4
            continue
        }
        y = j
        x = i
        val position = Pair(y, x)
        if (!visited.contains(position)) {
            visited.add(position)
            directionAtPosition[position] = direction
        } else {
            if (directionAtPosition[position] == direction) {
                return 1
            }
        }
    }
    return 0
}

private fun nextAdjacency(x: Int, y: Int, direction: Int): Pair<Int, Int> {
    return when (direction) {
        north -> Pair(y - 1, x)
        east -> Pair(y, x + 1)
        south -> Pair(y + 1, x)
        west -> Pair(y, x - 1)
        else -> Pair(y, x)
    }
}

private fun searchPosition(array: PrimitiveMultiDimArray<Char>, what: Char): Pair<Int, Int> {
    for (j in 0 until array.getDimensionSize(0)) {
        for (i in 0 until array.getDimensionSize(1)) {
            val currentChar = array[j, i]
            if (currentChar == what) {
                return Pair(j, i)
            }
        }
    }
    return Pair(-1, -1)
}
