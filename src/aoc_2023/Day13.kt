package aoc_2023

import aoc_util.Primitive2DCharArray
import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day13_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::solveIt)
    solve("Test2 result", testInput, ::solveIt2)

    val lines = readInput2023("Day13")
    val input = parseInput(lines)
    solve("Result", input, ::solveIt)
    solve("Result 2", input, ::solveIt2)

}

private fun solveIt2(input: List<Primitive2DCharArray>): Int {
    var out = 0
    for (array in input) {
        val regular = getReflectionAxes(array)
        val alternative = getAlternativeReflectionAxes(array)
        val (xAxis, yAxis) = alternative
        val result = 100 * yAxis + xAxis
        out += result
    }
    return out
}

private fun solveIt(input: List<Primitive2DCharArray>): Int {
    var out = 0
    for (array in input) {
        val (xAxis, yAxis) = getReflectionAxes(array)
        val result = 100 * yAxis + xAxis
        out += result
    }
    return out
}

private val noResult = ReflectionAxes(0, 0)

private fun getAlternativeReflectionAxes(array: Primitive2DCharArray): ReflectionAxes {
    val regularResult = getReflectionAxes(array)
    val (iCol, iRow) = regularResult
    val cArray = array.copy()
    val newResults = HashSet<ReflectionAxes>()
    for (row in 0..<cArray.height) {
        for (col in 0..<cArray.width) {
            cArray.flip(row, col)
            val newResult = getReflectionAxes(cArray, iCol, iRow)
            newResults.add(newResult)
            if (noResult != newResult && regularResult != newResult) {
                if (newResult.xAxis == 0 || newResult.yAxis == 0) {
                    return newResult
                }
                return if (regularResult.xAxis == 0) {
                    ReflectionAxes(newResult.xAxis, 0)
                } else {
                    ReflectionAxes(0, newResult.yAxis)
                }
            }
            cArray.flip(row, col)
        }
    }
    return noResult
}

private fun Primitive2DCharArray.flip(row: Int, col: Int) {
    val value = this[row, col]
    this[row, col] = when (value) {
        '.' -> '#'
        '#' -> '.'
        else -> value
    }
}

private fun getReflectionAxes(array: Primitive2DCharArray, ignoreColumn: Int = 0, ignoreRow: Int = 0): ReflectionAxes {
    // probe for column
    val col = probeForReflectionColumn(array, ignoreColumn)
    // probe for row
    val row = probeForReflectionColumn(array.transpose(), ignoreRow)
    return ReflectionAxes(col, row)
}

private fun probeForReflectionColumn(array: Primitive2DCharArray, ignore: Int = 0): Int {
    var highestMatches = 0
    var highestCol = 0
    probe@ for (col in 1..array.width) {
        if (col == ignore) {
            continue@probe
        }
        var delta = 0
        inner@ while (true) {
            val leftIndex = col - 1 - delta
            val rightIndex = col + delta
            if (leftIndex !in 0..<array.width || rightIndex !in 0..<array.width) {
                if (delta > highestMatches) {
                    highestMatches = delta
                    highestCol = col
                    continue@probe
                } else {
                    break@probe
                }
            }
            val leftColumn = array.getColumn(leftIndex)
            val rightColumn = array.getColumn(rightIndex)
            if (leftColumn != rightColumn) {
                break@inner
            }
            delta += 1
        }
    }
    return highestCol
}

private data class ReflectionAxes(val xAxis: Int, val yAxis: Int)

private fun parseInput(lines: List<String>): List<Primitive2DCharArray> {
    val out: MutableList<Primitive2DCharArray> = ArrayList()
    val workList: MutableList<String> = ArrayList()
    for (line in lines) {
        if (line.isBlank()) {
            out.add(Primitive2DCharArray.parseFromLines(workList, '.'))
            workList.clear()
            continue
        }
        workList.add(line)
    }
    if (workList.isNotEmpty()) {
        out.add(Primitive2DCharArray.parseFromLines(workList, '.'))
        workList.clear()
    }
    return out
}