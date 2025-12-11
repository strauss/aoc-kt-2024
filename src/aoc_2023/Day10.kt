package aoc_2023

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day10_test")
    val (testGraph, tsId) = parse(testLines)
    solve("Test result", testGraph) {
        part1(it, tsId)
    }

    val lines = readInput2023("Day10")
    val (graph, sId) = parse(lines)
    solve("Result", graph) {
        part1(it, sId)
    }

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

private data class V(val row: Int, val col: Int, val c: Char)

private fun parse(lines: List<String>): Pair<BitSetAdjacencyBasedGraph<V>, Int> {
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
    val connectNorth = "|LJ"
    val connectEast = "-LF"
    val connectSouth = "|7F"
    val connectWest = "-J7"
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
