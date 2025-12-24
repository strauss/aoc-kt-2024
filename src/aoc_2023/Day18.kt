package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day18_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::calculateAreaByFilling)

    val lines = readInput2023("Day18")
    val input = parseInput(lines)
    solve("Result", input, ::calculateAreaByFilling)
}

private val deltaRight = 0 to 1
private val deltaLeft = 0 to -1
private val deltaUp = -1 to 0
private val deltaDown = 1 to 0
private val deltaInvalid = 0 to 0

private fun Char.getDelta(): Pair<Int, Int> = when (this) {
    'R' -> deltaRight
    'L' -> deltaLeft
    'U' -> deltaUp
    'D' -> deltaDown
    else -> deltaInvalid
}

private fun calculateAreaByFilling(instructions: List<Entry>): Int {
    // create perimeter
    val perimeter = LinkedHashSet<Pair<Int, Int>>()
    var row = 0
    var col = 0
    val start = row to col
    perimeter.add(start)
    var minRow = row
    var minCol = col
    var maxRow = row
    var maxCol = col
    for (instruction in instructions) {
        val (char, steps, _) = instruction
        val (dRow, dCol) = char.getDelta()
        for (i in 1..steps) {
            row += dRow
            col += dCol
            perimeter.add(row to col)
            minRow = minRow.coerceAtMost(row)
            minCol = minCol.coerceAtMost(col)
            maxRow = maxRow.coerceAtLeast(row)
            maxCol = maxCol.coerceAtLeast(col)
        }
    }

    // determine inside value
    val startRow = (maxRow + minRow) / 2
    var currentColumn = minCol

    // search until perimeter is hit
    while (startRow to currentColumn !in perimeter) {
        currentColumn += 1
    }

    // search until "not perimeter" is hit
    while (startRow to currentColumn in perimeter) {
        currentColumn += 1
    }

    val fillStart = startRow to currentColumn
    // no we fill it up
    val filled = HashSet<Pair<Int, Int>>().also { it.addAll(perimeter) }
    val buffer = ArrayDeque<Pair<Int, Int>>()
    buffer.addLast(fillStart)

    fun handleNext(up: Pair<Int, Int>) {
        if (up !in filled) {
            buffer.addLast(up)
        }
    }

    while (buffer.isNotEmpty()) {
        val current = buffer.removeLast()
        filled.add(current)
        handleNext(current.first - 1 to current.second) // up
        handleNext(current.first + 1 to current.second) // down
        handleNext(current.first to current.second - 1) // left
        handleNext(current.first to current.second + 1) // right
    }

    return filled.size
}

private fun parseInput(lines: List<String>): List<Entry> {
    val result = ArrayList<Entry>(lines.size)
    val spaceSplit = " ".toRegex()
    for (line in lines) {
        val split = spaceSplit.split(line)
        val char = split[0][0]
        val steps = split[1].toInt()
        val remainder = split[2]
        result.add(Entry(char, steps, remainder))
    }
    return result
}

private data class Entry(val char: Char, val steps: Int, val remainder: String)