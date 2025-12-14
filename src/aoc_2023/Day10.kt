package aoc_2023

import aoc_util.Primitive2DCharArray
import aoc_util.graph.BitSetAdjacencyBasedGraph
import aoc_util.readInput2023
import aoc_util.solve

private const val connectNorth = "|LJ"
private const val connectEast = "-LF"
private const val connectSouth = "|7F"
private const val connectWest = "-J7"

fun main() {
    val testLines = readInput2023("Day10_test")
//    val (testGraph, tsId) = parseAsGraph(testLines)
//    solve("Test result", testGraph) {
//        part1(it, tsId)
//    }
    val testArray = parseAsArray(testLines)
    solve("Test result", testArray, ::solve1)

    val lines = readInput2023("Day10")
//    val (graph, sId) = parseAsGraph(lines)
//    solve("Result", graph) {
//        part1(it, sId)
//    }
    val array = parseAsArray(lines)
    solve("Result", array, ::solve1)

}

private fun solve1(array: Primitive2DCharArray): Int {
    // search the S
    val start = findStart(array)
    val (rowS, colS) = start
    val depth = HashMap<Pair<Int, Int>, Int>()
//    val visited = Array(array.height) { BitSet(array.width) }
    val currentDepth = 0
    depth[start] = currentDepth

    val q = ArrayDeque<Pair<Int, Int>>()

    // check north
    val rowNorth = rowS - 1
    if (rowNorth >= 0 && array[rowNorth, colS] in connectSouth) {
        val north = rowNorth to colS
        depth[north] = 1
        q.addLast(north)
    }
    // check east
    val colEast = colS + 1
    if (colEast < array.width && array[rowS, colEast] in connectWest) {
        val east = rowS to colEast
        depth[east] = 1
        q.addLast(east)
    }
    // check south
    val rowSouth = rowS + 1
    if (rowSouth < array.height && array[rowSouth, colS] in connectNorth) {
        val south = rowSouth to colS
        depth[south] = 1
        q.addLast(south)
    }
    // check west
    val colWest = colS - 1
    if (colWest >= 0 && array[rowS, colWest] in connectEast) {
        val west = rowS to colWest
        depth[west] = 1
        q.addLast(west)
    }

    var maxDepth = 0

    // start search
    while (q.isNotEmpty()) {
        val current = q.removeFirst()
        val currentDepth = depth[current]!!
        maxDepth = maxDepth.coerceAtLeast(currentDepth)
        val (row, col) = current
        val currentChar = array[row, col]
        if (currentChar in connectNorth) {
            val north = (row - 1) to col
            if (depth[north] == null) {
                depth[north] = currentDepth + 1
                q.addLast(north)
            }
        }
        if (currentChar in connectEast) {
            val east = row to col + 1
            if (depth[east] == null) {
                depth[east] = currentDepth + 1
                q.addLast(east)
            }
        }
        if (currentChar in connectSouth) {
            val south = row + 1 to col
            if (depth[south] == null) {
                depth[south] = currentDepth + 1
                q.addLast(south)
            }
        }
        if (currentChar in connectWest) {
            val west = row to col - 1
            if (depth[west] == null) {
                depth[west] = currentDepth + 1
                q.addLast(west)
            }
        }
    }

    return maxDepth
}

private fun findStart(array: Primitive2DCharArray): Pair<Int, Int> {
    for (row in 0..<array.height) {
        for (col in 0..<array.width) {
            if (array[row, col] == 'S') {
                return row to col
            }
        }
    }
    return -1 to -1
}

private fun part1(graph: BitSetAdjacencyBasedGraph<V>, startId: Int): Int {
    graph.run {
        val dv = DepthVisitor()
        val start = get(startId)
        start?.let {
            search(BitSetAdjacencyBasedGraph.SearchType.BFS, dv, it, false)
        }
        return dv.depth.values.max()
    }
}

private fun parseAsArray(lines: List<String>) = Primitive2DCharArray.parseFromLines(lines, '.')

private data class V(val row: Int, val col: Int, val c: Char)

private fun parseAsGraph(lines: List<String>): Pair<BitSetAdjacencyBasedGraph<V>, Int> {
    val width = lines.asSequence().map { it.length }.max()
    val graph = BitSetAdjacencyBasedGraph<V>(directed = false)
    // create vertices and place them in array
    for (row in lines.indices) {
        val line = lines[row]
        for (col in line.indices) {
            graph.run {
                val id = introduceVertex(V(row, col, line[col]))
                assert(id == row * width + col)
            }
        }
    }
    // materialize edges
    var startId: Int = -1
    graph.run {
        var start: V? = null
        forEach { v: V ->
            val edgeCount = countEdges()
            if (connectNorth.contains(v.c)) {
                val nId = (v.row - 1) * width + v.col
                get(nId)?.let { v.connect(it) }
            }
            if (connectEast.contains(v.c)) {
                val eId = v.row * width + v.col + 1
                get(eId)?.let { v.connect(it) }
            }
            if (connectSouth.contains(v.c)) {
                val sId = (v.row + 1) * width + v.col
                get(sId)?.let { v.connect(it) }
            }
            if (connectWest.contains(v.c)) {
                val wId = v.row * width + v.col - 1
                get(wId)?.let { v.connect(it) }
            }
            if (v.c == 'S') {
                start = v
            }
        }
//        start?.let { s ->
//            val nId = (s.row - 1) * width + s.col
//            get(nId)?.let { if (connectSouth.contains(it.c)) s.connect(it) }
//            val eId = s.row * width + s.col + 1
//            get(eId)?.let { if (connectWest.contains(it.c)) s.connect(it) }
//            val sId = (s.row + 1) * width + s.col
//            get(sId)?.let { if (connectNorth.contains(it.c)) s.connect(it) }
//            val wId = s.row * width - 1
//            get(wId)?.let { if (connectEast.contains(it.c)) s.connect(it) }
//            startId = s.getId()
//        }
        startId = start?.getId() ?: -1
    }
    return graph to startId
}
