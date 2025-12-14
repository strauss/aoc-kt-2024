package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve
import java.util.*
import kotlin.math.abs

fun main() {
    val testLines = readInput2023("Day11_test")
    val testUniverse = parseUniverse(testLines)
    solve("Test result", testUniverse) { solveUniverse(it, 2) }
    solve("Test result", testUniverse) { solveUniverse(it, 10) }
    solve("Test result", testUniverse) { solveUniverse(it, 100) }

    val lines = readInput2023("Day11")
    val universe = parseUniverse(lines)
    solve("Result", universe) { solveUniverse(it, 2) }
    solve("Result", universe) { solveUniverse(it, 1_000_000) }
}

private fun solveUniverse(state: UniverseState, factor: Int = 2): Long {
    var result = 0L
    val exp = state.expand(factor)

    val galaxies = exp.galaxies.toList()

    for (i in galaxies.indices) {
        val g1 = galaxies[i]
        for (j in i + 1..<galaxies.size) {
            val g2 = galaxies[j]
            result += g1.distanceTo(g2)
        }
    }

    return result
}

private fun Pair<Int, Int>.distanceTo(other: Pair<Int, Int>): Int {
    val d1 = abs(first - other.first)
    val d2 = abs(second - other.second)
    return d1 + d2
}

private fun parseUniverse(lines: List<String>): UniverseState {
//    val height = lines.size
//    val width = lines.asSequence().map { it.length }.max()
    val result = UniverseState()
    for (row in lines.indices) {
        val line = lines[row]
        var g = 0
        for (col in line.indices) {
            val c = line[col]
            if (c == '#') {
                result.galaxies.add(row to col)
                result.filledColumns[col] = true
                result.filledRows[row] = true
                g += 1
            }
        }
    }
    return result
}

private class UniverseState() {
    val galaxies = HashSet<Pair<Int, Int>>()
    val filledRows = BitSet()
    val filledColumns = BitSet()
    fun expand(factor: Int = 2): UniverseState {
        val result = UniverseState()
        for (galaxy in galaxies) {
            val (row, col) = galaxy
            val eRow = row + countEmptyRowsUntil(row) * (factor - 1)
            val eCol = col + countEmptyColumnsUntil(col) * (factor - 1)
            result.galaxies.add(eRow to eCol)
            result.filledRows[eRow] = true
            result.filledColumns[eCol] = true
        }
        return result
    }

    private fun countEmptyRowsUntil(row: Int): Int {
        var result = 0
        for (i in 0..<row) {
            result += if (filledRows[i]) 0 else 1
        }
        return result
    }

    private fun countEmptyColumnsUntil(col: Int): Int {
        var result = 0
        for (i in 0..<col) {
            result += if (filledColumns[i]) 0 else 1
        }
        return result
    }
}