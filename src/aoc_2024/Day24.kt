package aoc_2024

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import java.util.*

fun main() {
    val testInput = readInput2024("Day24_test")
    val (testGraph, testAssignment) = createDirectedGraph(testInput)
    val testResult = evaluate(testGraph, testAssignment)
    println("Test result: $testResult")
    println()
    val input = readInput2024("Day24")
    val (graph, assignment) = createDirectedGraph(input)
    val result = evaluate(graph, assignment)
    println("Result: $result")
    println(graph.getEdges().size)
}

private fun evaluate(graph: BitSetAdjacencyBasedGraph<Operation>, assignment: Map<String, Int>): Long {

    return graph.run {
        // here we store the results, we start with the assignments
        val results = mutableMapOf<String, Int>()
        results.putAll(assignment)

        // Process in topological order (with the help of my dear friend Kahn-Knuth)
        // get inDegree for all vertices ... unfortunately my structure can only get the outDegree easily ... so we perform a search for that
        val inDegree = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
        val inDegreeVisitor = object : BitSetAdjacencyBasedGraph<Operation>.SearchVisitor() {
            override fun visitEdge(edge: BitSetAdjacencyBasedGraph<Operation>.Edge, from: Operation, to: Operation) {
                val toIndegree = inDegree[to.getId()] ?: 0
                inDegree[to.getId()] = toIndegree + 1
            }
        }
        search(BitSetAdjacencyBasedGraph.SearchType.BFS, inDegreeVisitor)

        val nextCandidates: Queue<Operation> = LinkedList()
        for (v in this) {
            val vInDegree = inDegree.getOrDefault(v.getId(), 0)
            if (vInDegree == 0) {
                nextCandidates.add(v)
            }
        }

        // now the fun begins
        while (nextCandidates.isNotEmpty()) {
            val currentOperation = nextCandidates.poll()

            // perform the operation (usually, this part would be in a visit point)
            val first = results[currentOperation.s1]!!
            val second = results[currentOperation.s2]!!
            val result = currentOperation.eval(first, second)
            results[currentOperation.t] = result

            // now the Kahn-Knuth part continues
            for (nextOperation in currentOperation.adjacencies()) {
                // we reduce the in-degree because we just processed the edge
                val nextOperationInDegree = inDegree[nextOperation.getId()]!! - 1
                inDegree[nextOperation.getId()] = nextOperationInDegree

                // if there are no more relevant incoming edges, we add the vertex to the candidate list
                if (nextOperationInDegree == 0) {
                    nextCandidates.add(nextOperation)
                }
            }

        }

        // evaluate the result
        val finalResultKeys: List<String> = results.keys.asSequence().filter { it.startsWith("z") }.sorted().toList().reversed()
        var result = 0L
        for (key in finalResultKeys) {
            val current = results[key]!!
            result = result shl 1
            result = result xor current.toLong()
        }
        result
    }
}

private fun createDirectedGraph(input: List<String>): Pair<BitSetAdjacencyBasedGraph<Operation>, Map<String, Int>> {
    val targetToSourceMap = LinkedHashMap<String, Operation>()
    val targetToSelfMap = LinkedHashMap<String, MutableSet<Operation>>()
    val inputAssignments = LinkedHashMap<String, Int>()
    val opSet = mutableSetOf<Operation>()

    val inputAssignmentRegex = "([xy]\\d\\d): ([01])".toRegex()
    val operationRegex = "([a-z0-9]{3}) (AND|OR|XOR) ([a-z0-9]{3}) -> ([a-z0-9]{3})".toRegex()

    // read, convert and collect lines
    for (line in input) {
        if (line.isBlank()) {
            continue
        }
        val assignment = inputAssignmentRegex.matchEntire(line)
        if (assignment != null) {
            val groupValues = assignment.groupValues
            inputAssignments[groupValues[1]] = groupValues[2].toInt()
            continue
        }
        val operation = operationRegex.matchEntire(line)
        if (operation != null) {
            val groupValues = operation.groupValues
            val s1 = groupValues[1]
            val operator = Operator.valueOf(groupValues[2])
            val s2 = groupValues[3]
            val target = groupValues[4]
            val operationVertex = Operation(operator, s1, s2, target)
            opSet.add(operationVertex)
            targetToSourceMap[target] = operationVertex
            targetToSelfMap.getOrPut(s1) { LinkedHashSet() }.add(operationVertex)
            targetToSelfMap.getOrPut(s2) { LinkedHashSet() }.add(operationVertex)
            continue
        }
        println("No match for line '$line'")
    }
    val graph = BitSetAdjacencyBasedGraph<Operation>(directed = true)
    graph.run {
        for ((target, operation) in targetToSourceMap) {
            introduceVertex(operation) // this covers operations that directly produce output
            targetToSelfMap[target]?.forEach { targetOperation ->
                operation.connect(targetOperation)
            }
        }
    }
    return graph to inputAssignments
}

private data class Operation(val op: Operator, val s1: String, val s2: String, val t: String) {
    fun eval(first: Int, second: Int): Int {
        val result = when (op) {
            Operator.AND -> first and second
            Operator.OR -> first or second
            Operator.XOR -> first xor second
        }
        return result and 1 // limit result to last bit
    }
}

private enum class Operator {
    AND, OR, XOR
}