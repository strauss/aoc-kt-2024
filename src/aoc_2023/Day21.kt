package aoc_2023

import aoc_util.Coordinate
import aoc_util.Primitive2DCharArray
import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day21_test")
    val testInput = parseInput(testLines)
    val testSteps = 6
    solve("Test result", testInput) {
        countCoordinates(it, testSteps)
    }
    solve("Faster test result", testInput) {
        val (evens, odds) = countCoordinatesFaster(it, testSteps)
        if (testSteps % 2 == 0) evens.size else odds.size
    }
//    analyze(testInput)
//    solve("Test 2 result", testInput) {
//        countReachableStates(it, 999)
//    }

    val lines = readInput2023("Day21")
    val input = parseInput(lines)
    val steps = 64
    solve("Faster result", input) {
        val (evens, odds) = countCoordinatesFaster(it, steps)
        if (steps % 2 == 0) evens.size else odds.size
    }
    solve("Result", input) { countCoordinates(it, steps) }

    val part2Steps = 26501365
    solve("Result 2", input) { determinePart2States(it, part2Steps) }

}

/**
 * This is a very promising approach. There are probably several bugs concerning even and odd. Therefore, it is
 * advisable to come up with a simple example. The AoC example is not suitable.
 */
private fun determinePart2States(input: InputData, steps: Int): Long {
    val (even, odd) = analyze(input)
    val (first, second) = if (steps % 2 == 0) even to odd else odd to even // if steps are odd, we start with odd
    val maxReach = steps / input.width
    var result = 0L

    for (metaStep in 0..<maxReach) { // leave out the last one
        val maps: Long = maxOf(1L, metaStep.toLong() * 4L)
        val delta: Long = (if (metaStep % 2 == 0) first else second) * maps
        result += delta
    }

    val remainder = steps % input.width
    // This is probably incorrect
//    val remainderMaps = maxReach.toLong() * 4L
//    val remainderCount = countCoordinatesFaster(input, remainder)
//    val remainderDelta: Long = remainderMaps * remainderCount
//    result += remainderDelta

    val innerEdgeStepSize = input.width - 1 // one step is implicit
    val takeFirst = maxReach % 2 == 0
    val takeEven = takeFirst && even == first

    // at north we start south
    val south = Coordinate(input.height - 1, input.width / 2)
    val northResult = countCoordinatesFaster(input, innerEdgeStepSize, south)
    val northSet = if (takeEven) northResult.first else northResult.second
    result += northSet.size
    // at east we start west
    val west = Coordinate(input.height / 2, 0)
    val eastResult = countCoordinatesFaster(input, innerEdgeStepSize, west)
    val eastSet = if (takeEven) eastResult.first else northResult.second
    result += eastSet.size
    // at south we start north
    val north = Coordinate(0, input.width / 2)
    val southResult = countCoordinatesFaster(input, innerEdgeStepSize, north)
    val southSet = if (takeEven) southResult.first else southResult.second
    result += southSet.size
    // at west we start east
    val east = Coordinate(input.height / 2, input.width - 1)
    val westResult = countCoordinatesFaster(input, innerEdgeStepSize, east)
    val westSet = if (takeEven) westResult.first else westResult.second
    result += westSet.size

    // now we tackle the remainder of the inner edge
    val innerEdgeMapCountPerSide = maxReach - 1 // we have exactly one less on each side

    // north-east
    val northEastSet = northSet + eastSet // set union
    result += northEastSet.size * innerEdgeMapCountPerSide

    // south-east
    val southEastSet = southSet + eastSet // set union
    result += southEastSet.size * innerEdgeMapCountPerSide

    // south-west
    val southWestSet = southSet + westSet // set union
    result += southWestSet.size * innerEdgeMapCountPerSide

    // north-west
    val northWestSet = northSet + westSet // set union
    result += northWestSet.size * innerEdgeMapCountPerSide

    // now we add the outer edges
    // We have exactly as many maps on the outer edge per side, then we have maxReach
    // The idea is starting in one of the corners
    // we take exactly the opposite result set than for the inner edge (if it was even, we now take odd)
    val outerEdgeMapCountPerSide = maxReach
    val outerEdgeStepSize = remainder - 1 // one step is implicit

    // for north-east, we start south-west
    val southWest = Coordinate(input.height - 1, 0)
    val northEastResult = countCoordinatesFaster(input, outerEdgeStepSize, southWest)
    result += outerEdgeMapCountPerSide * if (takeEven) northEastResult.second.size else northEastResult.first.size

    // for south-east we start north-west
    val northWest = Coordinate(0, 0)
    val southEastResult = countCoordinatesFaster(input, outerEdgeStepSize, northWest)
    result += outerEdgeMapCountPerSide * if (takeEven) southEastResult.second.size else southEastResult.first.size

    // for south-west we start north-east
    val northEast = Coordinate(0, input.width - 1)
    val southWestResult = countCoordinatesFaster(input, outerEdgeStepSize, northEast)
    result += outerEdgeMapCountPerSide * if (takeEven) southWestResult.second.size else southWestResult.first.size

    // for north-west we start south-east
    val southEast = Coordinate(input.height - 1, input.width - 1)
    val northWestResult = countCoordinatesFaster(input, outerEdgeStepSize, southEast)
    result += outerEdgeMapCountPerSide * if (takeEven) northWestResult.second.size else northWestResult.first.size

    return result
}

private fun countReachableStates(input: InputData, limit: Int): Long {
    val (grid, start, height, width) = input
    val visited = VisitedMarker(grid.size)
    val startPosition = CompoundCoordinate(Coordinate(0, 0), start)
    visited.markVisited(startPosition)
    val workBuffer = ArrayDeque<LocalSearchState>()
    workBuffer.addFirst(LocalSearchState(startPosition, 0))

    while (workBuffer.isNotEmpty()) {
        val (compoundPosition, step) = workBuffer.removeFirst()
        val nextStep = step + 1
        if (nextStep > limit) {
            continue
        }
        val neighbors = compoundPosition.getNeighbors(width, height)
        for (nextCompoundPosition in neighbors) {
            if (visited.isVisited(nextCompoundPosition)) {
                continue
            }
            val (_, nextPosition) = nextCompoundPosition
            if (nextPosition in grid) {
                visited.markVisited(nextCompoundPosition)
                workBuffer.add(LocalSearchState(nextCompoundPosition, nextStep))
            }
        }
    }

    return visited.size
}

private class VisitedMarker(private val singleMapSize: Int) {
    private val visitedPositions: MutableMap<Coordinate, MutableSet<Coordinate>> = LinkedHashMap()
    private val visitedMaps: MutableSet<Coordinate> = LinkedHashSet()

    val size: Long
        get() = visitedMaps.size.toLong() * singleMapSize.toLong() +
                visitedPositions.values.sumOf { position -> position.size.toLong() }

    fun isVisited(compoundPosition: CompoundCoordinate): Boolean {
        val (map, position) = compoundPosition
        if (map in visitedMaps) {
            return true
        }
        return visitedPositions[map]?.contains(position) ?: false
    }

    fun markVisited(compoundPosition: CompoundCoordinate) {
        val (map, position) = compoundPosition
        if (map in visitedMaps) {
            return
        }
        val positions = visitedPositions.computeIfAbsent(map) { LinkedHashSet() }
        positions.add(position)
        if (positions.size == singleMapSize) {
            visitedMaps.add(map)
            visitedPositions.remove(map)
        }
    }

}

private data class LocalSearchState(val compoundPosition: CompoundCoordinate, val step: Int)

/**
 * The [map] [Coordinate] is the map we are in right now. The [position] refers to the position in the [map].
 */
private data class CompoundCoordinate(val map: Coordinate, val position: Coordinate) {
    fun getNorth(height: Int): CompoundCoordinate {
        val northPosition = position.getNorth()
        val nextPosition = if (northPosition.row >= 0) northPosition else Coordinate(height - 1, northPosition.col)
        val nextMap = if (northPosition == nextPosition) map else map.getNorth()
        return CompoundCoordinate(nextMap, nextPosition)
    }

    fun getEast(width: Int): CompoundCoordinate {
        val eastPosition = position.getEast()
        val nextPosition = if (eastPosition.col < width) eastPosition else Coordinate(eastPosition.row, 0)
        val nextMap = if (eastPosition == nextPosition) map else map.getEast()
        return CompoundCoordinate(nextMap, nextPosition)
    }

    fun getSouth(height: Int): CompoundCoordinate {
        val southPosition = position.getSouth()
        val nextPosition = if (southPosition.row < height) southPosition else Coordinate(0, southPosition.col)
        val nextMap = if (southPosition == nextPosition) map else map.getSouth()
        return CompoundCoordinate(nextMap, nextPosition)
    }

    fun getWest(width: Int): CompoundCoordinate {
        val westPosition = position.getWest()
        val nextPosition = if (westPosition.col >= 0) westPosition else Coordinate(westPosition.row, width - 1)
        val nextMap = if (westPosition == nextPosition) map else map.getWest()
        return CompoundCoordinate(nextMap, nextPosition)
    }

    fun getNeighbors(width: Int, height: Int): Set<CompoundCoordinate> =
        setOf(getNorth(height), getEast(width), getSouth(height), getWest(width))
}

private fun solveTorus(input: InputData, steps: Int): Long {
    val (grid, start, height, width) = input
    val solver = TorusSolver(grid, height, width)
    val result = solver.countTorusCoordinates(start, steps)
    return result
}

private class TorusSolver(private val grid: Set<Coordinate>, private val height: Int, private val width: Int) {

    fun countTorusCoordinates(start: Coordinate, steps: Int): Long {
        val workList = ArrayList<Coordinate>()
        var result = 0L
        workList.add(start)
        val followUpSet: MutableSet<Pair<Coordinate, Int>> = LinkedHashSet()
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
                    if (torusCoordinate in grid) {
                        val newSteps = steps - i
                        followUpSet.add(torusCoordinate to newSteps)
                    }
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
        result += workList.size.toLong()

        // TODO: handle follow-up set
        /*
         * Further ideas:
         * - introduce "higher level coordinates" for the map repetitions. The start map is at (0,0)
         * - store the reachable positions in a BitSet (2D -> 1D with local coordinates)
         * - for each hl-coordinate store the BitSet "so far"
         * - as soon as the whole local map is part of the solution, don't search in that part anymore (BitSet for comparison should be calculated beforehand)
         */

        return result
    }
}

private fun analyze(input: InputData): Pair<Int, Int> {
    println(input.grid.size)
    val evenSet = HashSet<Coordinate>()
    //internalCountCoordinates(input, evenSize).toSet()
    val oddSet = HashSet<Coordinate>()
    //internalCountCoordinates(input, oddSize).toSet()

    data class SearchState(val position: Coordinate, val even: Boolean)

    val workList = ArrayDeque<SearchState>()
    evenSet.add(input.start)
    workList.addLast(SearchState(input.start, true))

    while (workList.isNotEmpty()) {
        val (current, even) = workList.removeFirst()
        for (neighbor in current.getNeighbors()) {
            if (neighbor !in input.grid || neighbor in evenSet || neighbor in oddSet) {
                continue
            }
            val nextEven = !even
            if (nextEven) {
                evenSet.add(neighbor)
            } else {
                oddSet.add(neighbor)
            }
            workList.addLast(SearchState(neighbor, nextEven))
        }
    }

    println("Even: ${evenSet.size} + Odd: ${oddSet.size} = ${evenSet.size + oddSet.size}")

//    printDebugMap(input, evenSet, oddSet)

//    val north = countCoordinatesFaster(input, 64, Coordinate(0, input.width / 2))
//    val east = countCoordinatesFaster(input, 64, Coordinate(input.height / 2, input.width - 1))
//    val south = countCoordinatesFaster(input, 64, Coordinate(input.height - 1, input.width / 2))
//    val west = countCoordinatesFaster(input, 64, Coordinate(input.height / 2, 0))

    return evenSet.size to oddSet.size
}

private fun printDebugMap(
    input: InputData,
    evenSet: HashSet<Coordinate>,
    oddSet: HashSet<Coordinate>
) {
    val debugOut = Primitive2DCharArray(input.height, input.width)
    for (row in 0..<input.height) {
        for (col in 0..<input.width) {
            val current = Coordinate(row, col)
            val char = when (current) {
                in evenSet -> 'x'
                in oddSet -> 'O'
                else -> ' '
            }
            debugOut[row, col] = char
        }
    }

    println(debugOut.toString())
}

private fun countCoordinatesFaster(
    input: InputData, steps: Int = 64, alternativeStart: Coordinate? = null
): Pair<Set<Coordinate>, Set<Coordinate>> {
    val (grid, configuredStart, _, _) = input
    data class SearchState(val position: Coordinate, val step: Int)

    val start = alternativeStart ?: configuredStart

    val workList = ArrayDeque<SearchState>()
    val evenSet = HashSet<Coordinate>()
    val oddSet = HashSet<Coordinate>()
    evenSet.add(start)
    workList.addLast(SearchState(start, 0))
    while (workList.isNotEmpty()) {
        val (current, currentStep) = workList.removeFirst()
        for (neighbor in current.getNeighbors()) {
            if (currentStep >= steps || neighbor !in grid || neighbor in evenSet || neighbor in oddSet) {
                continue
            }
            val nextSteps = currentStep + 1
            if (nextSteps % 2 == 0) {
                evenSet.add(neighbor)
            } else {
                oddSet.add(neighbor)
            }
            workList.add(SearchState(neighbor, nextSteps))
        }
    }
    return evenSet to oddSet
}

private fun countCoordinates(input: InputData, steps: Int = 64): Int {
    val workList = internalCountCoordinates(input, steps)
    return workList.size
}

private fun internalCountCoordinates(
    input: InputData,
    steps: Int
): ArrayList<Coordinate> {
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
    return workList
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