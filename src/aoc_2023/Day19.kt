package aoc_2023

import aoc_util.graph.incidence.Graph
import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day19_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::simulateWorkflows)
    solve("Test result", testInput, ::simulateWithGraph)

    val lines = readInput2023("Day19")
    val input = parseInput(lines)
    solve("Result", input, ::simulateWorkflows)
    solve("Result", input, ::simulateWithGraph)

}

/*
 * - Create graph
 *     - Vertex: Pair<String, Int> (workflow name to index in rule list
 *     - Edge: The rule (hopefully that works)
 * - DFS in this graph
 *     - SearchState:
 *         - Allowed Range for all four numbers
 *     - Only store results that end up in "accept"
 * - Calculate all permutations for all stored ranges
 * - Somehow™ handle overlapping ranges (try subtracting all overlaps once)
 */

private fun simulateWithGraph(input: Pair<List<Workflow>, List<XmasPart>>): Long {
    val (workflows, parts) = input
    val graph = createGraph(workflows)
    val startVertex = graph.vertexSequence().first { it.value.id == "in" }
    var result = 0L
    for (part in parts) {
        var currentVertex = startVertex
        while (true) {
            val follow = currentVertex.incidenceOutSequence().first { edge -> part.evaluateWith(edge.value.operation) }
            val nextVertex = follow.omega
            if (nextVertex.value.id == "A") {
                result += part.sum()
                break
            }
            if (nextVertex.value.id == "R") {
                break
            }
            currentVertex = nextVertex
        }
    }

    return result
}

private data class WorkflowState(val id: String, val ruleIndex: Int)

private fun createGraph(workflows: List<Workflow>): Graph<WorkflowState, Rule> {
    val workflowMap = createWorkflowMap(workflows)
    val graph = Graph<WorkflowState, Rule>()
    val workflowIdToFirstVertexMap: MutableMap<String, Graph<WorkflowState, Rule>.Vertex> = HashMap()
    val acceptVertex = graph.createVertex(WorkflowState("A", 0))
    val rejectVertex = graph.createVertex(WorkflowState("R", 0))
    val startVertex = graph.createVertex(WorkflowState("in", 0))
    workflowIdToFirstVertexMap["in"] = startVertex

    val vertexBuffer = ArrayDeque<Graph<WorkflowState, Rule>.Vertex>()
    vertexBuffer.addLast(startVertex)
    while (vertexBuffer.isNotEmpty()) {
        val currentVertex = vertexBuffer.removeFirst()
        val currentId = currentVertex.value.id
        val workflow: Workflow = workflowMap[currentId] ?: throw IllegalStateException()
        val rules = workflow.rules
        var currentRule = rules[0]
        var innerVertex = currentVertex

        fun handleRedirect(nextId: String, rule: Rule) {
            val nextVertex = workflowIdToFirstVertexMap[nextId]
            val omegaVertex: Graph<WorkflowState, Rule>.Vertex
            if (nextVertex == null) {
                omegaVertex = graph.createVertex(WorkflowState(nextId, 0))
                workflowIdToFirstVertexMap[nextId] = omegaVertex
            } else {
                omegaVertex = nextVertex
            }
            graph.createEdge(innerVertex, omegaVertex, rule)
            if (nextVertex == null) {
                vertexBuffer.addLast(omegaVertex)
            } else {
                println("Cycle detected!")
            }
        }

        for (rIdx in 1..rules.lastIndex) {
            val currentMatch = currentRule.onMatch
            val inverseOperation = currentRule.operation.inverseOperation()
            when (currentMatch.type) {
                ResultType.ACCEPT -> graph.createEdge(innerVertex, acceptVertex, currentRule)
                ResultType.REJECT -> graph.createEdge(innerVertex, rejectVertex, currentRule)
                ResultType.REDIRECT -> {
                    val nextId = currentMatch.to!!
                    handleRedirect(nextId, currentRule)

                    if (rIdx != rules.lastIndex) {
                        val inverseRule = Rule(Result(ResultType.CONTINUE), inverseOperation)
                        val nextInnerVertex = graph.createVertex(WorkflowState(currentId, rIdx))
                        graph.createEdge(
                            innerVertex,
                            nextInnerVertex,
                            inverseRule
                        )
                        innerVertex = nextInnerVertex
                    }

                }

                ResultType.CONTINUE -> throw IllegalStateException()
            }
            if (rIdx == rules.lastIndex) {
                // enforced redirect
                val finalRule = rules[rules.lastIndex]
                val finalMatch = finalRule.onMatch
                when (finalMatch.type) {
                    ResultType.ACCEPT -> graph.createEdge(
                        innerVertex,
                        acceptVertex,
                        Rule(acceptResult, inverseOperation)
                    )

                    ResultType.REJECT -> graph.createEdge(
                        innerVertex,
                        rejectVertex,
                        Rule(rejectResult, inverseOperation)
                    )

                    ResultType.REDIRECT -> {
                        val finalId = finalMatch.to!!
                        handleRedirect(
                            finalId,
                            Rule(Result(ResultType.REDIRECT, finalMatch.to), inverseOperation)
                        )
                    }

                    ResultType.CONTINUE -> throw IllegalStateException()
                }


            }
            currentRule = rules[rIdx]
        }

    }
    return graph
}

private fun simulateWorkflows(input: Pair<List<Workflow>, List<XmasPart>>): Long {
    val (workflows, parts) = input
    val workflowMap = createWorkflowMap(workflows)
    var result = 0L

    for (part in parts) {
        var currentWorkflow = workflowMap["in"]!!
        simulation@ while (true) {
            rules@ for (rule in currentWorkflow.rules) {
                val match = part.evaluateWith(rule.operation)
                if (match) {
                    val r = rule.onMatch
                    when (r.type) {
                        ResultType.ACCEPT -> {
                            result += part.sum()
                            break@rules
                        }

                        ResultType.REJECT -> {
                            break@rules
                        }

                        ResultType.REDIRECT -> {
                            currentWorkflow = workflowMap[r.to!!]!!
                            continue@simulation
                        }

                        ResultType.CONTINUE -> {
                            continue@simulation
                        }
                    }
                }
            }
            break@simulation
        }
    }

    return result
}

private fun createWorkflowMap(workflows: List<Workflow>): Map<String, Workflow> {
    val workflowMap = buildMap {
        for (workflow in workflows) {
            put(workflow.name, workflow)
        }
    }
    return workflowMap
}

private fun parseInput(lines: List<String>): Pair<List<Workflow>, List<XmasPart>> {
    val workflows = ArrayList<Workflow>()
    val parts = ArrayList<XmasPart>()
    val commaSplit = ",".toRegex()
    val colonSplit = ":".toRegex()
    val equalsSplit = "=".toRegex()
    var parseWorkflows = true
    for (line in lines) {
        if (line.isBlank()) {
            parseWorkflows = false
            continue
        }
        if (parseWorkflows) {
            val until = line.indexOf('{')
            val wfName = line.substring(0..<until)
            val newWorkflow = Workflow(wfName)
            val rulesString = line.substring(until + 1..<line.lastIndex)
            val ruleStrings = commaSplit.split(rulesString)
            for (ruleString in ruleStrings) {
                val ruleSegments = colonSplit.split(ruleString)
                if (ruleSegments.size >= 2) {
                    val conditionString = ruleSegments[0]
                    val resultString = ruleSegments[1]
                    val letter = conditionString[0]
                    val number = conditionString.substring(2..conditionString.lastIndex).toInt()
                    val operator = when (conditionString[1]) {
                        '<' -> Operator.LT
                        '>' -> Operator.GT
                        else -> Operator.NOP
                    }
                    val operation = ComparisonOperation(letter, operator, number)
                    val onMatch = when (resultString) {
                        "A" -> acceptResult
                        "R" -> rejectResult
                        else -> Result(ResultType.REDIRECT, resultString)
                    }
                    val rule = Rule(onMatch, operation)
                    newWorkflow.addRule(rule)
                } else {
                    val resultString = ruleSegments[0]
                    val onMatch = when (resultString) {
                        "A" -> acceptResult
                        "R" -> rejectResult
                        else -> Result(ResultType.REDIRECT, resultString)
                    }
                    val rule = Rule(onMatch, ComparisonOperation('.', Operator.NOP, -1))
                    newWorkflow.addRule(rule)
                }
            }
            workflows.add(newWorkflow)
        } else {
            val relevantString = line.substring(1..<line.lastIndex)
            val assignments = commaSplit.split(relevantString)
            var x = Integer.MIN_VALUE
            var m = Integer.MIN_VALUE
            var a = Integer.MIN_VALUE
            var s = Integer.MIN_VALUE
            for (assignment in assignments) {
                val splitAssignment = equalsSplit.split(assignment)
                val value = splitAssignment[1].toInt()
                when (splitAssignment[0]) {
                    "x" -> x = value
                    "m" -> m = value
                    "a" -> a = value
                    "s" -> s = value
                }
            }
            val part = XmasPart(x, m, a, s)
            parts.add(part)
        }
    }

    return workflows to parts
}

private class Workflow(val name: String) {
    val rules: MutableList<Rule> = ArrayList()
    fun addRule(rule: Rule) {
        rules.add(rule)
    }
}

private enum class Operator {
    LT, LET, GT, GET, NOP;

    val inverse: Operator
        get() = when (this) {
            LT -> GET
            LET -> GT
            GT -> LET
            GET -> LT
            NOP -> NOP
        }
}

private data class ComparisonOperation(val variable: Char, val op: Operator, val number: Int) {
    fun inverseOperation(): ComparisonOperation = copy(op = op.inverse)
    fun evaluate(assignedValue: Int): Boolean = when (op) {
        Operator.LT -> assignedValue < number
        Operator.LET -> assignedValue <= number
        Operator.GT -> assignedValue > number
        Operator.GET -> assignedValue >= number
        Operator.NOP -> true
    }
}

private data class Rule(val onMatch: Result, val operation: ComparisonOperation)

private data class Result(val type: ResultType, val to: String? = null)

//private val continueResult = Result(ResultType.CONTINUE)
private val acceptResult = Result(ResultType.ACCEPT)
private val rejectResult = Result(ResultType.REJECT)

private enum class ResultType {
    ACCEPT, REJECT, REDIRECT, CONTINUE
}

private data class XmasPart(val x: Int, val m: Int, val a: Int, val s: Int) {
    fun getByChar(char: Char): Int = when (char) {
        'x', 'X' -> x
        'm', 'M' -> m
        'a', 'A' -> a
        's', 'S' -> s
        else -> -1
    }

    fun evaluateWith(op: ComparisonOperation) = op.evaluate(getByChar(op.variable))

    fun sum() = x + m + a + s
}