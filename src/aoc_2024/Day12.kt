package aoc_2024

import aoc_util.PrimitiveMultiDimArray
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2024
import java.util.*

fun main() {
    val testList = readInput2024("Day12_test_s")
    val list = readInput2024("Day12")
    val testArray = parseInputAsMultiDimArray(testList)
    val array = parseInputAsMultiDimArray(list)
    val (testResult1, testResult2) = getTotalFenceCost(testArray)
    val (result1, result2) = getTotalFenceCost(array)
    println("Test Result: $testResult1 ")
    println("Result: $result1")

    println("Test Result 2: $testResult2")
    println("Result 2: $result2")

}

fun getTotalFenceCost(array: PrimitiveMultiDimArray<Char>): Pair<Int, Int> {
    var result1 = 0
    val areas = determineAreas(array)
    var result2 = 0
    for (area in areas) {
        result1 += area.area * area.perimeter
        result2 += area.area * area.sides
    }
    return Pair(result1, result2)
}

private fun countCorners(points: List<Pair<Int, Int>>): Int {
    val ps = points.toSet()
    var count = 0
    for (point in points) {
        val (y, x) = point
        // convex corners
        // . . . . . . . . . . . . .
        // .[X]? . ?[X]. ? ? . ? ? .
        // . ? ? . ? ? .[X]? . ?[X].
        // . . . . . . . . . . . . .
        val north = Pair(y - 1, x)
        val northEast = Pair(y - 1, x + 1)
        val east = Pair(y, x + 1)
        val southEast = Pair(y + 1, x + 1)
        val south = Pair(y + 1, x)
        val southWest = Pair(y + 1, x - 1)
        val west = Pair(y, x - 1)
        val northWest = Pair(y - 1, x - 1)
        if (!ps.contains(north) && !ps.contains(west)) {
            count += 1
        }
        if (!ps.contains(north) && !ps.contains(east)) {
            count += 1
        }
        if (!ps.contains(south) && !ps.contains(west)) {
            count += 1
        }
        if (!ps.contains(south) && !ps.contains(east)) {
            count += 1
        }
        // concave corners
        // . . . . . . . . . . . . . . . .
        // . ? ? . . . ? ? . . . . . . . .
        // . ?[X]X . X[X]? . X . . . X . .
        // . . X . . . X . ?[X]X . X[X]? .
        // . . . . . . . . ? ? . . . ? ? .

        if (ps.contains(south) && ps.contains(east) && !ps.contains(southEast)) {
            count += 1
        }
        if (ps.contains(south) && ps.contains(west) && !ps.contains(southWest)) {
            count += 1
        }
        if (ps.contains(north) && ps.contains(east) && !ps.contains(northEast)) {
            count += 1
        }
        if (ps.contains(north) && ps.contains(west) && !ps.contains(northWest)) {
            count += 1
        }
    }
    return count
}

private fun determineAreas(array: PrimitiveMultiDimArray<Char>): List<Area> {
    val output = ArrayList<Area>()
    var currentPositions: MutableList<Pair<Int, Int>>
    var currentAreaSize: Int
    var currentPerimeter: Int
    val visitedPositions = mutableSetOf<Pair<Int, Int>>()
    val buffer = LinkedList<Pair<Int, Int>>()
    for (j in 0 until array.getDimensionSize(0)) {
        for (i in 0 until array.getDimensionSize(1)) {
            val currentRootPosition = Pair(j, i)
            if (visitedPositions.contains(currentRootPosition)) {
                continue
            }
            val currentChar = array[j, i]
            currentPositions = ArrayList()
            currentPositions.add(currentRootPosition)
            currentAreaSize = 1
            currentPerimeter = calcPerimeterPart(array, currentRootPosition, currentChar)
            buffer.add(currentRootPosition)
            visitedPositions.add(currentRootPosition)

            // BFS in Area
            while (buffer.isNotEmpty()) {
                val currentSearchPosition = buffer.removeFirst()
                // we only search the space that belongs to the current area
                if (currentChar == array[currentSearchPosition.first, currentSearchPosition.second]) {
                    val neighbors = getNeighbors(array, currentSearchPosition)
                    for (neighbor in neighbors) {
                        // we only consider neighbors that actually belong to our area
                        if (!visitedPositions.contains(neighbor) && currentChar == array[neighbor.first, neighbor.second]) {
                            visitedPositions.add(neighbor)
                            buffer.add(neighbor)
                            currentPositions.add(neighbor)
                            currentAreaSize += 1
                            val calcPerimeterPart = calcPerimeterPart(array, neighbor, currentChar)
                            currentPerimeter += calcPerimeterPart
                        }
                    }
                }
            }
            val sides = countCorners(currentPositions)
            output.add(Area(currentChar, currentAreaSize, currentPerimeter, sides, currentPositions))
        }
    }
    return output
}

private fun getNeighbors(array: PrimitiveMultiDimArray<Char>, position: Pair<Int, Int>): List<Pair<Int, Int>> {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val out = mutableListOf<Pair<Int, Int>>()
    val (y, x) = position
    var neighbor = Pair(y - 1, x)
    if (inBounds(neighbor, height, width)) {
        out.add(neighbor)
    }
    neighbor = Pair(y + 1, x)
    if (inBounds(neighbor, height, width)) {
        out.add(neighbor)
    }
    neighbor = Pair(y, x - 1)
    if (inBounds(neighbor, height, width)) {
        out.add(neighbor)
    }
    neighbor = Pair(y, x + 1)
    if (inBounds(neighbor, height, width)) {
        out.add(neighbor)
    }
    return out
}

fun calcPerimeterPart(array: PrimitiveMultiDimArray<Char>, position: Pair<Int, Int>, char: Char): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val (y, x) = position
    var sum = 0
    sum += perimeter(y - 1, x, height, width, array, char)
    sum += perimeter(y + 1, x, height, width, array, char)
    sum += perimeter(y, x - 1, height, width, array, char)
    sum += perimeter(y, x + 1, height, width, array, char)
    return sum
}

private fun perimeter(
    y: Int,
    x: Int,
    height: Int,
    width: Int,
    array: PrimitiveMultiDimArray<Char>,
    char: Char
): Int {
    if (!inBounds(y, x, height, width) || array[y, x] != char) {
        return 1
    }
    return 0
}

private fun inBounds(position: Pair<Int, Int>, height: Int, width: Int): Boolean {
    return inBounds(position.first, position.second, height, width)
}

private fun inBounds(y: Int, x: Int, height: Int, width: Int): Boolean {
    return y in 0..<height && x in 0..<width
}


private data class Area(val char: Char, val area: Int, val perimeter: Int, val sides: Int, val positions: List<Pair<Int, Int>>)