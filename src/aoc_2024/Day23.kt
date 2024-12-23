package aoc_2024

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.readInput2024

fun main() {
    val testInput = readInput2024("Day23_test")
    val testGraph = parseInput(testInput)
    val testResultSet = getCliquesOfThree(testGraph) { it.startsWith("t") }
    println("Test result: ${testResultSet.size}")
    val testResult2 = findBiggestSingleClique(testGraph)
    println("Test result 2: ${renderResult(testResult2)}")

    val input = readInput2024("Day23")
    val graph = parseInput(input)
    val resultSet = getCliquesOfThree(graph) { it.startsWith("t") }
    println("Result: ${resultSet.size}")
    val result2 = findBiggestSingleClique(graph)
    println("Result 2: ${renderResult(result2)}")
}

private fun renderResult(result: Set<String>): String {
    val resultAsList = ArrayList(result)
    resultAsList.sort()
    return resultAsList.joinToString(",")
}

private fun findBiggestSingleClique(graph: BitSetAdjacencyBasedGraph<String>): Set<String> {
    val cliquesOfThree = getCliquesOfThree(graph)
    var lastCliques = cliquesOfThree
    var cliqueSize = 3
    while (lastCliques.size > 1) {
        cliqueSize += 1
        lastCliques = getCliquesOfNextSize(graph, lastCliques)
        println("Cliques of size $cliqueSize: ${lastCliques.size}")
    }
    return if (lastCliques.size == 1) lastCliques.first() else emptySet()
}

private fun testSomethingElse(graph: BitSetAdjacencyBasedGraph<String>) {
    val cliquesOfThree = getCliquesOfThree(graph)
    println("Total vertices: ${graph.countVertices()}")
    println("Cliques of 3: ${cliquesOfThree.size}")
    val allCliqueVertices = cliquesOfThree.asSequence().flatMap { it.asSequence() }.toSet()
    println("All clique 3 vertices: ${allCliqueVertices.size}")
    val cliquesOfFour = getCliquesOfNextSize(graph, cliquesOfThree)
    println("Cliques of 4: ${cliquesOfFour.size}")
    val allCliqueFourVertices = cliquesOfFour.asSequence().flatMap { it.asSequence() }.toSet()
    println("All cliques 4 vertices: ${allCliqueFourVertices.size}")
    val cliquesOfFive = getCliquesOfNextSize(graph, cliquesOfFour)
    println("Cliques of 5: ${cliquesOfFive.size}")
    val allCliqueFiveVertices = cliquesOfFive.asSequence().flatMap { it.asSequence() }.toSet()
    println("All cliques 5 vertices: ${allCliqueFiveVertices.size}")
}

private fun getCliquesOfNextSize(graph: BitSetAdjacencyBasedGraph<String>, cliques: Set<Set<String>>): Set<Set<String>> {
    val out: MutableSet<Set<String>> = mutableSetOf()
    graph.run {
        for (clique: Set<String> in cliques) {
            for (vertex: String in vertexIterator()) {
                if (vertex in clique) {
                    continue
                }
                // I'm not sure about this one...
                if (out.any { vertex in it }) {
                    continue
                }
                if (vertex.isConnectedWithAll(clique)) {
                    val newClique: Set<String> = clique + vertex
                    out.add(newClique)
                }
            }
        }
    }
    return out
}

private fun getCliquesOfThree(graph: BitSetAdjacencyBasedGraph<String>, predicate: (String) -> Boolean = { true }): Set<Set<String>> {
    val result = mutableSetOf<Set<String>>()
    graph.run {
        for (v: String in vertexIterator()) {
            if (!predicate(v)) {
                continue
            }
            for (w in v.adjacencies()) {
                for (x in w.adjacencies()) {
                    if (x.isConnectedWith(v)) {
                        result.add(setOf(v, w, x))
                    }
                }
            }
        }
    }
    return result
}

private fun parseInput(input: List<String>): BitSetAdjacencyBasedGraph<String> {
    val graph = BitSetAdjacencyBasedGraph<String>()
    graph.run {
        for (line in input) {
            val lineSplit = line.split('-')
            lineSplit[0].trim().connect(lineSplit[1].trim())
        }
    }
    return graph
}