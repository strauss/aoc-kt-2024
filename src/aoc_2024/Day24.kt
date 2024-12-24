package aoc_2024

import aoc_util.BitSetAdjacencyBasedGraph
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import java.util.*

fun main() {
//    solve()
    val input = readInput2024("Day24")
    val (graph, assignment) = createDirectedGraph(input)
    playAround(graph, assignment)
}

private fun solve() {
    val testInput = readInput2024("Day24_test")
    val (testGraph, testAssignment) = createDirectedGraph(testInput)
    val testResult = evaluate(testGraph, testAssignment)
    println("Test result: $testResult")
    println()
    val input = readInput2024("Day24")
    val (graph, assignment) = createDirectedGraph(input)
    val result = evaluate(graph, assignment)
    println("Result: $result")
}

private fun playAround(graph: BitSetAdjacencyBasedGraph<Operation>, assignment: Map<String, Int>) {
    val correctPositions = determineCorrectPositions(graph, assignment)
    println("Correct positions: $correctPositions")
    val possibleIncorrectVertices: Set<Operation> = getPossibleIncorrectVertexIds(graph, correctPositions)
    println("Possible vertices causing an incorrect result: ${possibleIncorrectVertices.size}")

    val outputToOperationMap = mutableMapOf<String, Operation>()
    graph.run {
        for (v in this) {
            outputToOperationMap[v.t] = v
        }
    }

    for (position in 0..44) {
        val z = "z${position.toString().padStart(2, '0')}"
        println(getFormulaFor(outputToOperationMap, z))
    }

    // test swap
//    val newGraph = swapOutputConnections(graph, "z12", "z15")
//    val newCorrectPositions = determineCorrectPositions(newGraph, assignment)
//    println("New correct positions: $newCorrectPositions")
//    val newPossibleIncorrectVertices: Set<Operation> = getPossibleIncorrectVertexIds(newGraph, newCorrectPositions)
//    println("New possible vertices causing an incorrect result: ${newPossibleIncorrectVertices.size}")
}

private fun getFormulaFor(operations: Map<String, Operation>, output: String): String {
    val operation = operations[output] ?: return ""
    val inputRegex = "[xy]\\d{2}".toRegex()
    var formula = operation.formula()

    if (!inputRegex.matches(operation.s1)) {
        val s1Formula = getFormulaFor(operations, operation.s1)
        formula = formula.replace(operation.s1, s1Formula)
    }
    if (!inputRegex.matches(operation.s2)) {
        val s2Formula = getFormulaFor(operations, operation.s2)
        formula = formula.replace(operation.s2, s2Formula)
    }
    return "[$output=$formula]"

}

private fun swapOutputConnections(graph: BitSetAdjacencyBasedGraph<Operation>, first: String, second: String): BitSetAdjacencyBasedGraph<Operation> {
    val copyGraph = graph.createCopy()
    // find vertices
    var firstVertex: Operation? = null
    var secondVertex: Operation? = null
    copyGraph.run {
        for (v in this) {
            if (firstVertex != null && secondVertex != null) {
                break
            }
            if (v.t == first) {
                firstVertex = v
            } else if (v.t == second) {
                secondVertex = v
            }
        }
    }

    if (firstVertex != null && secondVertex != null) {
        val oldFirst: Operation = firstVertex!!
        val oldSecond: Operation = secondVertex!!
        val firstIn = mutableListOf<Operation>()
        val firstOut = mutableListOf<Operation>()
        val secondIn = mutableListOf<Operation>()
        val secondOut = mutableListOf<Operation>()
        copyGraph.run {
            for (v in oldFirst.backwardAdjacencies()) {
                firstIn.add(v)
            }
            for (v in oldFirst.adjacencies()) {
                firstOut.add(v)
            }
            for (v in oldSecond.backwardAdjacencies()) {
                secondIn.add(v)
            }
            for (v in oldSecond.adjacencies()) {
                secondOut.add(v)
            }
        }
        println("First in: $firstIn")
        println("First out: $firstOut")
        println("Second in: $secondIn")
        println("Second out: $secondOut")
        val newFirst = Operation(oldFirst.op, oldFirst.s1, oldFirst.s2, second)
        val newSecond = Operation(oldSecond.op, oldSecond.s1, oldSecond.s2, first)
        // reestablish the connections
        copyGraph.run {
            oldFirst.remove()
            oldSecond.remove()
            introduceVertex(newFirst)
            introduceVertex(newSecond)
            // sustain incoming edges
            for (v in firstIn) {
                v.connect(newFirst)
            }
            for (v in secondIn) {
                v.connect(newSecond)
            }
            // swap outgoing edges
            for (v in firstOut) {
                newSecond.connect(v)
            }
            for (v in secondOut) {
                newFirst.connect(v)
            }
        }
    }

    return copyGraph
}

private fun getPossibleIncorrectVertexIds(
    graph: BitSetAdjacencyBasedGraph<Operation>,
    correctPositions: LinkedHashSet<String>
): Set<Operation> {
    val correctOutputVertices: MutableList<Operation> = mutableListOf()
    val possibleIncorrectVertices: MutableSet<Operation> = mutableSetOf()
    val incorrectPositions = mutableListOf<String>()
    graph.run {
        for (v in this) {
            if (v.t in correctPositions) {
                correctOutputVertices.add(v)
            } else {
                possibleIncorrectVertices.add(v)
                if (v.t.startsWith("z")) {
                    incorrectPositions.add(v.t)
                }
            }
        }
        for (w in correctOutputVertices) {
            for (u in w.backwardAdjacencies()) {
                possibleIncorrectVertices.remove(u)
            }
        }
    }
    incorrectPositions.sort()
    println("Incorrect positions: $incorrectPositions")
    println("Graph size: ${graph.size}")
    println("Output vertices with correct result: ${correctOutputVertices.size}")
    return possibleIncorrectVertices
}

private fun determineCorrectPositions(
    graph: BitSetAdjacencyBasedGraph<Operation>,
    assignment: Map<String, Int>
): LinkedHashSet<String> {
    val xKeys: List<String> = assignment.keys.asSequence().filter { it.startsWith("x") }.sorted().toList().reversed()
    val yKeys: List<String> = assignment.keys.asSequence().filter { it.startsWith("y") }.sorted().toList().reversed()
    val x: Long = assembleLongFromBits(xKeys, assignment)
    val y: Long = assembleLongFromBits(yKeys, assignment)
    var correctResult = x + y
    var result: Long = evaluate(graph, assignment)
    println(correctResult)
    println(result)

    var position = 0
    val correctPositions = LinkedHashSet<String>()
    while (correctResult != 0L && result != 0L) {
        val currentPosition = "z${position.toString().padStart(2, '0')}"
        if (correctResult and 1 == result and 1) {
            correctPositions.add(currentPosition)
        }
        correctResult = correctResult ushr 1
        result = result ushr 1
        position += 1
    }
    return correctPositions
}

private fun evaluate(graph: BitSetAdjacencyBasedGraph<Operation>, assignment: Map<String, Int>): Long {

    return graph.run {
        // here we store the results, we start with the assignments
        val results = mutableMapOf<String, Int>()
        results.putAll(assignment)

        // Process in topological order (with the help of my dear friend Kahn-Knuth)
        val inDegree = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
        for (v in this) {
            val vInDegree = v.getInDegree()
            if (vInDegree > 0) {
                inDegree[v.getId()] = vInDegree
            }
        }

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
        assembleLongFromBits(finalResultKeys, results)
    }
}

private fun assembleLongFromBits(
    keys: List<String>,
    assignments: Map<String, Int>
): Long {
    var result = 0L
    for (key in keys) {
        val current = assignments[key]!!
        result = result shl 1
        result = result xor current.toLong()
    }
    return result
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

    fun formula(): String = "($s1 $op $s2)"
}

private enum class Operator {
    AND, OR, XOR
}