package aoc_2025

import aoc_util.*
import java.util.*

fun main() {
    val testLines = readInput2025("Day11_test2")
    val testGraph = parseInput(testLines)
    solve("Test result", testGraph, ::countPathsSingle)
//    solve("Test 2 result", testGraph, ::countPathsMultiple)

    val lines = readInput2025("Day11")
    val graph = parseInput(lines)
    solve("Result", graph, ::countPathsSingle)
    solve("Result2", graph, ::countPathsMultiple)
}

private fun countPathsMultiple(graph: BitSetAdjacencyBasedGraph<String>): Long {
    var backwardEdges: Set<Pair<String, String>> = setOf() //getBackwardEdges(graph, "svr")
    val dfs = ExhaustiveDfs(graph)
    dfs.execute("svr", "out", setOf())
    val upperBound = dfs.getResult().size
//        countPaths(graph, "svr", "out", backwardEdges)
    println("Upper bound: $upperBound")
    val part1 = countPaths(graph, "svr", "fft", backwardEdges)//, setOf("dac", "out"))
//    backwardEdges = getBackwardEdges(graph, "fft")
    val part2 = countPaths(graph, "fft", "dac", backwardEdges)//, setOf("svr", "out"))
//    backwardEdges = getBackwardEdges(graph, "dac")
    val part3 = countPaths(graph, "dac", "out", backwardEdges)//, setOf("svr", "dac"))
    val result = part1 * part2 * part3

    return result
}

private fun getBackwardEdges(graph: BitSetAdjacencyBasedGraph<String>, start: String): HashSet<Pair<String, String>> {
    val dfs = RecursiveDfs(graph)
    val backwardEdges = HashSet<Pair<String, String>>()
    val detector = object : RecursiveDfs.DfsVisitor<String>(dfs) {
        override fun visitBackwardArc(from: String, to: String) {
            backwardEdges.add(from to to)
        }

        override fun visitCrossLink(from: String, to: String) {
            backwardEdges.add(from to to)
        }
    }
    dfs.execute(start, detector)
    println("Backward edges svr: ${backwardEdges.size}")
    return backwardEdges
}

private fun countPathsSingle(graph: BitSetAdjacencyBasedGraph<String>): Long {
    return countPaths(graph, "you", "out")
}

private fun countPaths(
    graph: BitSetAdjacencyBasedGraph<String>,
    start: String,
    goal: String,
    backwardEdges: Set<Pair<String, String>> = setOf(),
    blacklist: Set<String> = setOf()
): Long {
    var result = 0L
    graph.run {
        val q = ArrayDeque<String>()
        val visitedCrossLinks = HashSet<Pair<String, String>>()
        val visitedEdges = HashMap<Pair<String, String>, Int>()

        q.addFirst(start)
        while (q.isNotEmpty()) {
            val current = q.removeFirst()
            for (next in current.adjacencies()) {
                val edge = current to next
                if (backwardEdges.contains(edge)) {
                    if (visitedCrossLinks.contains(edge)) {
                        continue
                    }
                    visitedCrossLinks.add(edge)
                }
//                val visitedTimes = visitedEdges[edge] ?: 0
//                if (visitedTimes > countVertices() * 5) {
//                    continue
//                }
//                visitedEdges[edge] = visitedTimes + 1
                if (next == goal) {
                    result += 1
                } else if (next != start && !blacklist.contains(next)) {
                    q.offer(next)
                }
            }
        }
    }
    return result
}

private fun parseInput(lines: List<String>): BitSetAdjacencyBasedGraph<String> {
    val graph = BitSetAdjacencyBasedGraph<String>(directed = true)
    val connections = LinkedHashMap<String, List<String>>()
    for (line in lines) {
        val s = line.split(":")
        graph.introduceVertex(s[0].trim())
        val cons: List<String> = s[1].trim().split(" ")
        connections[s[0]] = cons
    }
    graph.run {
        for ((vertex, con) in connections.entries) {
            for (c: String in con) {
                vertex.connect(c)
            }
        }
    }
    return graph
}