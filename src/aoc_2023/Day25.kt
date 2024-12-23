package aoc_2023

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.CombinatorialIterator
import aoc_util.readInput2023
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import java.util.*

fun main() {
    val testList = readInput2023("Day25_test")
    val testGraph = parseAdjacencies(testList)
    val testResult = shatter(testGraph)
    println("Test result: $testResult")

    val list = readInput2023("Day25")
    val graph = parseAdjacencies(list)
    val result = shatter(graph)
    println("Result: $result")
}

private fun shatter(graph: BitSetAdjacencyBasedGraph<String>): Int {
    val edges = ArrayList(graph.getEdges())
    val treeEdgeCount: MutableMap<BitSetAdjacencyBasedGraph<String>.Edge, Int> = HashMap()

    graph.run {
        val treeEdgeCounter =
            object : BitSetAdjacencyBasedGraph<String>.SearchVisitor() {
                override fun visitTreeEdge(edge: BitSetAdjacencyBasedGraph<String>.Edge, from: String, to: String) {
                    treeEdgeCount[edge] = (treeEdgeCount[edge] ?: 0) + 1
                }
            }

        vertexIterator().forEach { vertex ->
            search(BitSetAdjacencyBasedGraph.SearchType.BFS, treeEdgeCounter, vertex, false)
        }
    }

    val treeEdges = edges.sortedWith { e1: BitSetAdjacencyBasedGraph<String>.Edge, e2: BitSetAdjacencyBasedGraph<String>.Edge ->
        (treeEdgeCount[e2] ?: 0).compareTo(treeEdgeCount[e1] ?: 0)
    }

    val firstCount = treeEdgeCount[treeEdges.first()] ?: 0

    var result: List<List<String>> = emptyList()

    for (i in 0..firstCount) {
        val candidateList = ArrayList<BitSetAdjacencyBasedGraph<String>.Edge>()
        for (edge in treeEdges) {
            if ((treeEdgeCount[edge] ?: 0) >= firstCount - i) {
                candidateList.add(edge)
            } else {
                break
            }
        }

//        for (edge in candidateList) {
//            println("$edge: ${treeEdgeCount[edge] ?: 0}")
//        }

        if (candidateList.size > 2) {
            println("Tolerance: $i")
            result = bruteForce(graph, candidateList)
            if (result.size == 2) {
                break
            }
        } else {
            println("Not enough edges left at tolerance: $i.")
        }
    }
    return if (result.size == 2) result[0].size * result[1].size else 0
}

private fun bruteForce(graph: BitSetAdjacencyBasedGraph<String>, edges: List<BitSetAdjacencyBasedGraph<String>.Edge>): List<List<String>> {
    println("Bruteforcing result for ${edges.size}")
    val bfIterator = CombinatorialIterator(edges, 3, true)
    var result: List<List<String>>? = null
    bfIterator.iterate { currentCandidate: List<BitSetAdjacencyBasedGraph<String>.Edge> ->
        val graphCopy = graph.createCopy()

        graphCopy.run {
            currentCandidate.forEach { it.alpha.disconnect(it.omega) }
        }
        val weakComponents = weakComponents(graphCopy)
        if (weakComponents.size == 2) {
            result = weakComponents
            println(currentCandidate)
            bfIterator.stop()
        }
    }
    return result ?: emptyList()
}


fun <V> weakComponents(graph: BitSetAdjacencyBasedGraph<V>): List<List<V>> {
    val visitor = graph.WeakComponentVisitor()
    graph.run {
        search(BitSetAdjacencyBasedGraph.SearchType.DFS, visitor, null)
    }
    return visitor.result
}

private fun <V> weakComponents2(graph: BitSetAdjacencyBasedGraph<V>): List<List<V>> {

    val resultList = ArrayList<MutableList<V>>()
    // DFS
    graph.run {
        val vertexIterator = vertexIterator()
        if (!vertexIterator.hasNext()) {
            return emptyList()
        }
        val entered = PrimitiveIntSetB()
        val vertexStack = Stack<V>()
        val root = vertexIterator.next()

        // visit root
        // visit vertex
        var currentInnerList = ArrayList<V>()
        currentInnerList.add(root)

        entered.add(root.getId())
        vertexStack.push(root)

        while (vertexStack.isNotEmpty()) {
            val currentVertex = vertexStack.pop()
            currentVertex.adjacencies().forEach { adjacentVertex: V ->
                // visit edge
                if (!entered.contains(adjacentVertex.getId())) {
                    entered.add(adjacentVertex.getId())
                    vertexStack.push(adjacentVertex)

                    // visit vertex
                    currentInnerList.add(adjacentVertex)

                    // visit tree edge
                } else {
                    // visit frond
                }
            }
            if (vertexStack.isEmpty()) {
                // Empty stack = done with current weak component
                // leaveroot
                resultList.add(currentInnerList)
                while (vertexIterator.hasNext()) {
                    val nextPossibleRoot = vertexIterator.next()
                    if (!entered.contains(nextPossibleRoot.getId())) {

                        // visit root
                        currentInnerList = ArrayList<V>()
                        currentInnerList.add(nextPossibleRoot)

                        entered.add(nextPossibleRoot.getId())
                        vertexStack.push(nextPossibleRoot)
                        break
                    }
                }
            }
        }
    }

    return resultList
}

private fun parseAdjacencies(input: List<String>): BitSetAdjacencyBasedGraph<String> {
    val graph = BitSetAdjacencyBasedGraph<String>()

    graph.run {
        input.forEach { line: String ->
            val nodeToNodes: List<String> = line.split(':')
            val node: String = nodeToNodes[0]
            this.introduceVertex(node)
            val nodes: List<String> = nodeToNodes[1].trim().split(' ')
            nodes.forEach { vertex: String ->
                this.introduceVertex(vertex)
                node.connect(vertex)
            }
        }
    }

    return graph
}

