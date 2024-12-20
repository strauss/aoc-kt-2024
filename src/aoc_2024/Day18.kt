package aoc_2024

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.Coordinate
import aoc_util.readInput2024

fun main() {
    val testSize = 7
    val size = 71
    val testInput = readInput2024("Day18_test")
    val testCoordinates = parseCoordinates(testInput)
    val testGraph = createUndirectedNeighborGraph(testSize, testSize)
    val testResult = searchPathAfter(testGraph, testCoordinates, 12, Coordinate(0, 0), Coordinate(6, 6))
    println("Test result: $testResult")
    val testBlockadeAfter = probeForBlockade(testGraph, testCoordinates, 12, Coordinate(0, 0), Coordinate(6, 6))
    println("Test block at ${testCoordinates[testBlockadeAfter - 1]}")

    val input = readInput2024("Day18")
    val coordinates = parseCoordinates(input)
    val graph = createUndirectedNeighborGraph(size, size)
    val result = searchPathAfter(graph, coordinates, 1024, Coordinate(0, 0), Coordinate(70, 70))
    println("Result: $result")
    val blockadeAfter = probeForBlockade(graph, coordinates, 1024, Coordinate(0, 0), Coordinate(70, 70))
    println("Block at ${coordinates[blockadeAfter - 1]}")
}

private fun probeForBlockade(
    graph: BitSetAdjacencyBasedGraph<Coordinate>,
    coordinates: List<Coordinate>, startByte: Int, start: Coordinate,
    target: Coordinate
): Int {
    for (i in startByte..coordinates.lastIndex) {
        val currentResult = searchPathAfter(graph, coordinates, i, start, target)
        if (currentResult == 0) {
            return i
        }
    }
    return -1
}

private fun searchPathAfter(
    graph: BitSetAdjacencyBasedGraph<Coordinate>,
    coordinates: List<Coordinate>,
    after: Int,
    start: Coordinate,
    target: Coordinate
): Int {
    val searchGraph = graph.createCopy()
    var pathLength = 0
    searchGraph.run {
        for (i in 0..<after) {
            val coordinate = coordinates[i]
            coordinate.isolate()
        }
        val parent: MutableMap<Coordinate, Coordinate> = HashMap()
        val countVertices = object : BitSetAdjacencyBasedGraph<Coordinate>.SearchVisitor() {
            override fun visitTreeEdge(edge: BitSetAdjacencyBasedGraph<Coordinate>.Edge, from: Coordinate, to: Coordinate) {
                parent[to] = from
            }
        }
        searchGraph.search(BitSetAdjacencyBasedGraph.SearchType.BFS, countVertices, start, false)
        val path = getPath(parent, target)
        pathLength = path.size
    }
    return pathLength
}

private fun getPath(parent: Map<Coordinate, Coordinate>, from: Coordinate): List<Coordinate> {
    val out: MutableList<Coordinate> = mutableListOf()
    var currentParent: Coordinate? = parent[from]
    while (currentParent != null) {
        out.add(currentParent)
        currentParent = parent[currentParent]
    }
    return out.reversed()
}

private fun parseCoordinates(input: List<String>): List<Coordinate> {
    val out = mutableListOf<Coordinate>()
    for (line in input) {
        val splitLine = line.split(',')
        val row = splitLine[1].trim().toInt()
        val col = splitLine[0].trim().toInt()
        out.add(Coordinate(row, col))
    }
    return out
}

private fun createUndirectedNeighborGraph(height: Int, width: Int): BitSetAdjacencyBasedGraph<Coordinate> {
    val graph = BitSetAdjacencyBasedGraph<Coordinate>()
    graph.run {
        for (row in 0..<height) {
            for (col in 0..<width) {
                val current = Coordinate(row, col)
                introduceVertex(current)
                if (row > 0) {
                    current.connect(current.getNorth())
                }
                if (col > 0) {
                    current.connect(current.getWest())
                }
            }
        }
    }
    return graph
}
