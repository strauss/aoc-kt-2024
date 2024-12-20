package aoc_2024

import aoc_util.*
import kotlin.math.abs

fun main() {
    val testInput = readInput2024("Day20_test")
    val testMaze = parseInputAsMultiDimArray(testInput)
    val testResult = efficientSolution(testMaze, 2, 1.0)
    println("Test result: $testResult")
    val testResultRound2 = efficientSolution(testMaze, 20, 50.0)
    println("Test result 2: $testResultRound2")

    println()

    val input = readInput2024("Day20")
    val maze = parseInputAsMultiDimArray(input)
    val result = efficientSolution(maze, 2, 100.0)
    println("Result: $result")
    val resultRound2 = efficientSolution(maze, 20, 100.0)
    println("Result 2: $resultRound2")
}

private fun efficientSolution(maze: PrimitiveMultiDimArray<Char>, cheatTime: Int, feasibleBound: Double): Int {
    val rowRange = 0..<maze.getDimensionSize(0)
    val colRange = 0..<maze.getDimensionSize(1)
    val (start, end) = searchStartAndEnd(maze)
    val graph = assembleGraph(maze)
    var out = 0
    graph.run {
        val dijkstra = Dijkstra(graph)
        // We search backwards to fully exploit the true power of Dijkstra.
        // The parent function directly leads to the end on the shortest "regular" path.
        // The distance function gives us the cost from every vertex to the end.
        // This information can be used for directly determining the cost efficiency of a "cheat path" without the need to brute-force anything.
        dijkstra.execute(root = end)
        val parent = dijkstra.parent
        val distance = dijkstra.distance
        var currentPosition = start
        while (currentPosition != end) {
            for (row in currentPosition.row - cheatTime..currentPosition.row + cheatTime) {
                for (col in currentPosition.col - cheatTime..currentPosition.col + cheatTime) {
                    // if too far away or not in bounds
                    if (row !in rowRange || col !in colRange) {
                        continue
                    }
                    // no walls ... we could also check if the vertex is contained in the graph ... but well, at least it saves creating the Coordinate object too early
                    if (maze[row, col] == '#') {
                        continue
                    }
                    // check if in manhattan distance
                    val rowDistance = abs(row - currentPosition.row)
                    val colDistance = abs(col - currentPosition.col)
                    val mDistance = rowDistance + colDistance
                    if (mDistance > cheatTime) {
                        continue
                    }
                    val cheatPosition = Coordinate(row, col)
                    val currentDistance = distance[currentPosition.getId()] ?: Double.POSITIVE_INFINITY
                    val cheatDistance = (distance[cheatPosition.getId()] ?: Double.NEGATIVE_INFINITY) + mDistance - 1
                    val timeSave = currentDistance - cheatDistance
                    if (timeSave > feasibleBound) {
                        out += 1
                    }
                }
            }
            val parentId: Int = parent[currentPosition.getId()] ?: -1
            currentPosition = parentId.getVertex() ?: end // a bit of a hack ... at least no NPE and no endless loop
        }
    }
    return out
}

private fun searchStartAndEnd(maze: PrimitiveMultiDimArray<Char>): Pair<Coordinate, Coordinate> {
    val (srow, scol) = searchFor(maze, 'S')
    val (erow, ecol) = searchFor(maze, 'E')
    return Pair(Coordinate(srow, scol), Coordinate(erow, ecol))
}

private fun assembleGraph(maze: PrimitiveMultiDimArray<Char>): WeightedGraph<Coordinate> {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    val graph = WeightedGraph<Coordinate>()
    graph.run {
        for (row in 0..<height) {
            for (col in 0..<width) {
                if (maze[row, col] != '#') {
                    val current = Coordinate(row, col)
                    introduceVertex(current)
                    val north = current.getNorth()
                    if (row > 0 && maze[north.row, north.col] != '#') {
                        current.connect(north)
                    }
                    val west = current.getWest()
                    if (col > 0 && maze[west.row, west.col] != '#') {
                        current.connect(west)
                    }
                }
            }
        }
    }
    return graph
}

