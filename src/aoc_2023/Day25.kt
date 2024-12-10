package aoc_2023

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.CombinatorialIterator
import aoc_util.readInput2023
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import java.util.*

fun main() {
    val testList = readInput2023("Day25_test")
    val testGraph = parseAdjacencies(testList)
    println("Bruteforcing result for ${testGraph.getEdges().size}")
    val testResult = bruteForce(testGraph)
    println("Test result: ${testResult[0].size * testResult[1].size}")

    val list = readInput2023("Day25")
    val graph = parseAdjacencies(list)
    println("Bruteforcing result for ${graph.getEdges().size}")
    val result = bruteForce(graph)
    println("Result: ${result[0].size * result[1].size}")
}

private fun bruteForce(graph: BitSetAdjacencyBasedGraph<String>): List<List<String>> {
    val edges = graph.getEdges()
    val bfIterator = CombinatorialIterator(edges, 3)
    var result: List<List<String>>? = null
    bfIterator.iterate { currentCandidate: List<Pair<String, String>> ->
        val graphCopy = graph.createCopy()

        graphCopy.run {
            currentCandidate.forEach {
                it.first.disconnect(it.second)
            }
        }
        val weakComponents = weakComponents(graphCopy)
        if (weakComponents.size == 2) {
            result = weakComponents
            return@iterate
        }
    }
    return result ?: emptyList()
}


private fun <V> weakComponents(graph: BitSetAdjacencyBasedGraph<V>): List<List<V>> {
    val resultList = ArrayList<MutableList<V>>()
    // DFS
    graph.run {
        val vertexIterator = vertexIterator()
        if (!vertexIterator.hasNext()) {
            return emptyList()
        }
        val entered = PrimitiveIntSetB()
        var currentInnerList = ArrayList<V>()
        val vertexStack = Stack<V>()
        val root = vertexIterator.next()
        // visit root
        // visit vertex
        entered.add(root.getId())
        currentInnerList.add(root)
        vertexStack.push(root)

        while (vertexStack.isNotEmpty()) {
            val currentVertex = vertexStack.pop()
            currentVertex.adjacencies().forEach { adjacentVertex: V ->
                // visit edge
                if (!entered.contains(adjacentVertex.getId())) {
                    entered.add(adjacentVertex.getId())
                    currentInnerList.add(adjacentVertex)
                    vertexStack.push(adjacentVertex)
                    // visit vertex
                    // visit tree edge
                } else {
                    // visit frond
                }
            }
            if (vertexStack.isEmpty()) {
                // Empty stack = done with current weak component
                resultList.add(currentInnerList)
                while (vertexIterator.hasNext()) {
                    val nextPossibleRoot = vertexIterator.next()
                    if (!entered.contains(nextPossibleRoot.getId())) {
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

