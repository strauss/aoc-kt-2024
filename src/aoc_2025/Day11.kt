package aoc_2025

import aoc_util.graph.BitSetAdjacencyBasedGraph
import aoc_util.graph.ExhaustiveDfs
import aoc_util.graph.RecursiveDfs
import aoc_util.graph.Warshall
import aoc_util.readInput2025
import aoc_util.solve
import java.util.*
import kotlin.time.measureTime

fun main() {
    val testLines1 = readInput2025("Day11_test")
    val testGraph1 = parseInput(testLines1)
    solve("Test result", testGraph1, ::countPathsSingle)

    val testLines2 = readInput2025("Day11_test2")
    val testGraph2 = parseInput(testLines2)
    solve("Test 2 result", testGraph2, ::countPathsMultiple)

    val lines = readInput2025("Day11")
    val graph = parseInput(lines)
    solve("Result", graph, ::countPathsSingle)
    solve("Result2", graph, ::countPathsMultiple)
}

private fun countPathsExhaustively(graph: BitSetAdjacencyBasedGraph<String>): Long {
    // TODO make this solution also work
    val dfs = ExhaustiveDfs(graph)
    dfs.execute("svr", "out", setOf()) //setOf("dac", "out"))
    val part1 = dfs.getResult("svr").size
    println(part1)

    return -1
}

private fun countPathsMultiple(graph: BitSetAdjacencyBasedGraph<String>): Long {
    // well ... that takes some time, but is not too bad
    val warshall = Warshall(graph)
    val duration = measureTime { warshall.execute() }
    println("Warshall done (Duration: $duration)")
    val part1 = countPaths(graph, "svr", "fft", warshall)
    val part2 = countPaths(graph, "fft", "dac", warshall)
    val part3 = countPaths(graph, "dac", "out", warshall)
    val result = part1 * part2 * part3

    return result
}

// that one was interesting for the analysis, but is not that useful, maybe I should move it to the graph part
private fun getBackwardEdges(graph: BitSetAdjacencyBasedGraph<String>, start: String): HashSet<Pair<String, String>> {
    val dfs = RecursiveDfs(graph)
    val treeEdges = HashSet<Pair<String, String>>()
    val forwardArcs = HashSet<Pair<String, String>>()
    val backwardArcs = HashSet<Pair<String, String>>()
    val crossLinks = HashSet<Pair<String, String>>()

    val detector = object : RecursiveDfs.DfsVisitor<String>(dfs) {
        override fun visitTreeEdge(from: String, to: String) {
            treeEdges.add(from to to)
        }

        override fun visitForwardArc(from: String, to: String) {
            forwardArcs.add(from to to)
        }

        override fun visitBackwardArc(from: String, to: String) {
            backwardArcs.add(from to to)
        }

        override fun visitCrossLink(from: String, to: String) {
            crossLinks.add(from to to)
        }
    }
    dfs.execute(start, detector)
    return backwardArcs
}

private fun countPathsSingle(graph: BitSetAdjacencyBasedGraph<String>): Long {
    return countPaths(graph, "you", "out", null)
}

private fun countPaths(
    graph: BitSetAdjacencyBasedGraph<String>,
    start: String,
    goal: String,
    reachability: Warshall<String>?
): Long {
    var result = 0L
    graph.run {
        val q = ArrayDeque<String>()
        q.addFirst(start)
        while (q.isNotEmpty()) {
            val current = q.removeFirst()
            for (next in current.adjacencies()) {
                if (next == goal) {
                    result += 1
                } else if (next != start && reachability?.isReachable(current, goal) ?: true) {
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