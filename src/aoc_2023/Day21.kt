package aoc_2023

import aoc_util.Coordinate
import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day21_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::countCoordinates)
    solve("Test 2 result", testInput) {
        solveTorus(testInput, 10)
    }

    val lines = readInput2023("Day21")
    val input = parseInput(lines)
    solve("Result", input, ::countCoordinates)
}

private fun solveTorus(input: InputData, steps: Int): Long {
    val (grid, start, height, width) = input
    val solver = TorusSolver(grid, height, width)
    val result = solver.countTorusCoordinates(start, steps)
    return result
}

private class TorusSolver(private val grid: Set<Coordinate>, private val height: Int, private val width: Int) {

//    private val resultCache: MutableMap<Pair<Coordinate, Int>, Long> = HashMap()

    private val alreadyCounted: MutableSet<Pair<Coordinate, Int>> = HashSet()

    fun countTorusCoordinates(start: Coordinate, steps: Int): Long {
//        val cachedResult = resultCache[start to steps]
//        if (cachedResult != null) {
//            return cachedResult
//        }
        val workList = ArrayList<Coordinate>()
        var result = 0L
        workList.add(start)
        for (i in 1..steps) {
            val tmpSet = HashSet<Coordinate>()
            fun handleNeighbor(neighbor: Coordinate) {
                if (neighbor.row in 0..<height && neighbor.col in 0..<width) {
                    if (neighbor in grid) {
                        tmpSet.add(neighbor)
                    }
                } else {
                    val (nRow, nCol) = neighbor
                    val tRow = if (nRow > 0) nRow % height else height - nRow
                    val tCol = if (nCol > 0) nCol % width else width - nCol
                    val torusCoordinate = Coordinate(tRow, tCol)
                    val newSteps = steps - i
                    if (torusCoordinate in grid && (torusCoordinate to newSteps) !in alreadyCounted) {
                        val subResult = countTorusCoordinates(torusCoordinate, newSteps)
                        result += subResult
                    }
                }
            }
            for (coordinate in workList) {
                val north = coordinate.getNorth()
                handleNeighbor(north)
                val east = coordinate.getEast()
                handleNeighbor(east)
                val south = coordinate.getSouth()
                handleNeighbor(south)
                val west = coordinate.getWest()
                handleNeighbor(west)
            }
            workList.clear()
            workList.addAll(tmpSet)
        }
        result += workList.size.toLong()
//        resultCache[start to steps] = result
        alreadyCounted.add(start to steps)
        return result
    }
}

private fun countCoordinates(input: InputData, steps: Int = 64): Int {
    val (grid, start, _, _) = input
    val workList = ArrayList<Coordinate>()
    workList.add(start)
    for (i in 1..steps) {
        val tmpSet = HashSet<Coordinate>()
        fun handleNeighbor(neighbor: Coordinate) {
            if (neighbor in grid) {
                tmpSet.add(neighbor)
            }
        }
        for (coordinate in workList) {
            handleNeighbor(coordinate.getNorth())
            handleNeighbor(coordinate.getEast())
            handleNeighbor(coordinate.getSouth())
            handleNeighbor(coordinate.getWest())
        }
        workList.clear()
        workList.addAll(tmpSet)
    }
    return workList.size
}

private fun parseInput(lines: List<String>): InputData {
    var start = Coordinate(-1, -1)
    val grid: MutableSet<Coordinate> = LinkedHashSet()
    var width = 0

    for (row in lines.indices) {
        val line = lines[row]
        width = width.coerceAtLeast(line.length)
        for (col in line.indices) {
            val char = line[col]
            if (char == '.' || char == 'S') {
                val coordinate = Coordinate(row, col)
                grid.add(coordinate)
                if (char == 'S') {
                    start = coordinate
                }
            }
        }
    }

    return InputData(grid, start, lines.size, width)
}

private data class InputData(val grid: Set<Coordinate>, val start: Coordinate, val height: Int, val width: Int)