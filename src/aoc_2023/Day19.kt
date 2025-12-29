package aoc_2023

import aoc_util.graph.incidence.*
import aoc_util.readInput2023
import aoc_util.solve
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder

fun main() {
    val testLines = readInput2023("Day19_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::simulateWorkflows)
    solve("Test result", testInput, ::simulateWithGraph)
    solve("Test 2 result", testInput.first, ::simulateWorkflows)

    val lines = readInput2023("Day19")
    val input = parseInput(lines)
    solve("Result", input, ::simulateWorkflows)
    solve("Result", input, ::simulateWithGraph)
    solve("Result 2", input.first, ::simulateWorkflows)

}

private fun rangePermutations(workflows: List<Workflow>): Long {
    val graph = createGraph(workflows)
    val startVertex = graph.vertexSequence().first { it.value.id == "in" }
    val visitor = RangeVisitor()
    val bfs = BreadthFirstSearch(graph, visitor)
    bfs.execute(startVertex)
    val accepted = visitor.acceptedRanges
    val rejected = visitor.rejectedRanges

    val sumOfAcceptedPermutations = accepted.sumOf { it.permutations() }
    val acceptIntersections = calculateIntersections(accepted)
    val sumOfAcceptIntersections = acceptIntersections.sumOf { it.permutations() }

    val sumOfRejectedPermutations = rejected.sumOf { it.permutations() }
    val rejectedIntersections = calculateIntersections(rejected)
    val sumOfRejectedIntersections = rejectedIntersections.sumOf { it.permutations() }

    val result = sumOfAcceptedPermutations - sumOfAcceptIntersections
    val rejectedWouldBeResult = sumOfRejectedPermutations - sumOfRejectedIntersections

    println("Sum of accepted: $sumOfAcceptedPermutations")
    println("Sum of rejected: $sumOfRejectedPermutations")
    println("Total: ${sumOfAcceptedPermutations + sumOfRejectedPermutations} ")

    println("Result for accepted: $result")
    println("Result for rejected: $rejectedWouldBeResult")
    println("Total: ${result + rejectedWouldBeResult}")

    val maximum = XmasRanges.initialRanges.permutations()
    println("Reference maximum: $maximum")


    return result
}

private fun calculateIntersections(result: MutableList<XmasRanges>): ArrayList<XmasRanges> {
    val intersections = ArrayList<XmasRanges>()
    for (idx in result.indices) {
        val currentPartialResult: XmasRanges = result[idx]
        for (otherIdx in idx + 1..result.lastIndex) {
            val otherPartialResult: XmasRanges = result[otherIdx]
            val intersection = currentPartialResult.intersect(otherPartialResult)
            if (intersection.permutations() != 0L) {
                intersections.add(intersection)
            }
        }
    }
    return intersections
}

private class RangeVisitor : SearchVisitor<WorkflowState, Rule>() {
    private val rangesAtVertex: MutableMap<Int, XmasRanges> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<XmasRanges>().create()
    val acceptedRanges: MutableList<XmasRanges> = ArrayList()
    val rejectedRanges: MutableList<XmasRanges> = ArrayList()

    override fun visitRoot(root: Graph<WorkflowState, Rule>.Vertex) {
        rangesAtVertex[root.id] = XmasRanges.initialRanges
    }

    override fun visitEdge(edge: Graph<WorkflowState, Rule>.Edge) {
        val alpha = edge.alpha
        val alphaRanges = rangesAtVertex[alpha.id]!!
        val rule = edge.value
        val operation = rule.operation
        val omegaRanges = alphaRanges.getReducedRangesByOperation(operation)
//        println("$alpha -> $omegaRanges ($rule)")
        val omega = edge.omega
        when (omega.value.id) {
            "A" -> acceptedRanges.add(omegaRanges)
            "R" -> rejectedRanges.add(omegaRanges)

            else -> {
                assert(rangesAtVertex[omega.id] == null)
                rangesAtVertex[omega.id] = omegaRanges // regular case
            }
        }
    }
}

private data class XmasRanges(val xr: IntRange, val mr: IntRange, val ar: IntRange, val sr: IntRange) {

    companion object {
        private val emptyRange = 1..0

        val initialRanges = XmasRanges(1..4000, 1..4000, 1..4000, 1..4000)

        fun IntRange.reduce(op: Operator, number: Int): IntRange {
            val lower = this.start
            val upper = this.endInclusive

            val newLower: Int
            val newUpper: Int

            when (op) {
                Operator.LT -> {
                    newLower = lower
                    newUpper = upper.coerceAtMost(number - 1)
                }

                Operator.LET -> {
                    newLower = lower
                    newUpper = upper.coerceAtMost(number)
                }

                Operator.GT -> {
                    newLower = lower.coerceAtLeast(number + 1)
                    newUpper = upper
                }

                Operator.GET -> {
                    newLower = lower.coerceAtLeast(number)
                    newUpper = upper
                }

                Operator.NOP -> {
                    newLower = lower
                    newUpper = upper
                }
            }

            if (newUpper < newLower) {
                return emptyRange
            }

            return newLower..newUpper
        }

        /**
         * Splits at [border]. The right side includes [border] at its lower bound. If [border] is out of bounds,
         * one of the resulting ranges will be empty and the other one a copy of [this].
         */
        fun IntRange.split(border: Int, borderLeft: Boolean = false): Pair<IntRange, IntRange> {
            if ((borderLeft && border < start) || (!borderLeft && border <= start)) {
                return emptyRange to start..endInclusive
            }
            if ((borderLeft && border >= endInclusive) || (!borderLeft && border > endInclusive)) {
                // if border is exactly the upper bound, the right side will contain exactly one number
                return start..endInclusive to emptyRange
            }
            return if (borderLeft) start..border to (border + 1)..endInclusive else start..<border to border..endInclusive
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val range = 100..300
            println(range.split(100, true))
        }

        fun IntRange.intersect(other: IntRange): IntRange {
            if (this.size() == 0 || other.size() == 0) {
                return emptyRange
            }
            val smaller = if (this.start < other.start) this else other
            val bigger = if (this == smaller) other else this
            if (smaller.endInclusive < bigger.start) {
                return emptyRange
            }

            val start = bigger.start
            val endInclusive = if (bigger.endInclusive in smaller) bigger.endInclusive else smaller.endInclusive

            return start..endInclusive
        }

        fun IntRange.size(): Int = 0.coerceAtLeast(endInclusive - start + 1)
    }

    fun getSplitRanges(variable: Char, border: Int, borderLeft: Boolean): Pair<XmasRanges, XmasRanges> {
        val relevantRange: IntRange = when (variable) {
            'x' -> xr
            'm' -> mr
            'a' -> ar
            's' -> sr
            else -> throw IllegalStateException()
        }

        val (left, right) = relevantRange.split(border, borderLeft)


        val nxr = if (xr === relevantRange) left to right else xr to xr
        val nmr = if (mr === relevantRange) left to right else mr to mr
        val nar = if (ar === relevantRange) left to right else ar to ar
        val nsr = if (sr === relevantRange) left to right else sr to sr

        return Pair(
            XmasRanges(
                nxr.first,
                nmr.first,
                nar.first,
                nsr.first
            ),
            XmasRanges(
                nxr.second,
                nmr.second,
                nar.second,
                nsr.second
            )
        )
    }

    fun getReducedRangesByOperation(operation: ComparisonOperation): XmasRanges {
        val variable: Char = operation.variable
        val relevantRange: IntRange = when (variable) {
            'x' -> xr
            'm' -> mr
            'a' -> ar
            's' -> sr
            else -> throw IllegalStateException()
        }

        val reducedRange = relevantRange.reduce(operation.op, operation.number)

        val nxr = if (xr === relevantRange) reducedRange else xr
        val nmr = if (mr === relevantRange) reducedRange else mr
        val nar = if (ar === relevantRange) reducedRange else ar
        val nsr = if (sr === relevantRange) reducedRange else sr

        return XmasRanges(nxr, nmr, nar, nsr)
    }

    fun permutations(): Long = xr.size().toLong() * mr.size().toLong() * ar.size().toLong() * sr.size().toLong()

    fun intersect(other: XmasRanges): XmasRanges = XmasRanges(
        xr.intersect(other.xr),
        mr.intersect(other.mr),
        ar.intersect(other.ar),
        sr.intersect(other.sr)
    )
}

/*
 * - Create graph
 * - DFS in this graph
 *     - SearchState:
 *         - Allowed Range for all four numbers
 *     - Only store results that end up in "accept"
 * - Calculate all permutations for all stored ranges
 * - Somehow™ handle overlapping ranges (try subtracting all overlaps once)
 */

private fun analyseGraph(graph: Graph<WorkflowState, Rule>, root: Graph<WorkflowState, Rule>.Vertex) {
    val visitor = AnalyseEdgesVisitor<WorkflowState, Rule>()
    val dfs = DepthFirstSearch(graph, visitor)
    dfs.execute(root)
    println(visitor)
}

private fun simulateWithGraph(input: Pair<List<Workflow>, List<XmasPart>>): Long {
    val (workflows, parts) = input
    val graph = createGraph(workflows)
    val startVertex = graph.vertexSequence().first { it.value.id == "in" }
    analyseGraph(graph, startVertex)
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

private fun simulateWorkflows(workflows: List<Workflow>): Long {
    val workflowMap = createWorkflowMap(workflows)
    val accepted = ArrayList<XmasRanges>()
    val rejected = ArrayList<XmasRanges>()
    val workQueue = ArrayDeque<SimulationState>()
    val startState = SimulationState("in", XmasRanges.initialRanges)
    workQueue.addLast(startState)

    while (workQueue.isNotEmpty()) {
        val currentState = workQueue.removeFirst()
        val currentWorkflow = workflowMap[currentState.nextWorkflowId] ?: throw IllegalStateException()
        var currentRanges = currentState.ranges
        for (rule in currentWorkflow.rules) {
            val (result: Result, operation: ComparisonOperation) = rule
            val (variable: Char, op: Operator, number: Int) = operation
            val (resultType: ResultType, target: String?) = result
            if (op == Operator.NOP) { // last rule, just take the leftover currentRanges
                when (resultType) {
                    ResultType.ACCEPT -> accepted.add(currentRanges)
                    ResultType.REJECT -> rejected.add(currentRanges)
                    ResultType.REDIRECT -> workQueue.addLast(SimulationState(target!!, currentRanges))
                    ResultType.CONTINUE -> throw IllegalStateException()
                }
                continue
            }

            // normal case: take the positive result as it is (potentially creating a new SimulationState)
            // the negative result will be the leftover for the next rule in the workflow
            val borderLeft: Boolean = when (op) {
                Operator.GT, Operator.LET -> true
                Operator.LT, Operator.GET -> false
                Operator.NOP -> throw IllegalStateException()
            }
            val (nextLeftRangesRanges, nextRightRanges) = currentRanges.getSplitRanges(variable, number, borderLeft)
            val positiveResultRanges: XmasRanges
            val negativeResultRanges: XmasRanges
            if (op == Operator.LT || op == Operator.LET) {
                positiveResultRanges = nextLeftRangesRanges
                negativeResultRanges = nextRightRanges
            } else { // GT || GET
                positiveResultRanges = nextRightRanges
                negativeResultRanges = nextLeftRangesRanges
            }
            when (resultType) {
                ResultType.ACCEPT -> accepted.add(positiveResultRanges)
                ResultType.REJECT -> rejected.add(positiveResultRanges)
                ResultType.REDIRECT -> workQueue.addLast(SimulationState(target!!, positiveResultRanges))
                ResultType.CONTINUE -> throw IllegalStateException()
            }
            currentRanges = negativeResultRanges
        }
    }

    val rejectedResult: Long = rejected.sumOf { it.permutations() }
    val acceptedResult: Long = accepted.sumOf { it.permutations() }
    val totalPermutations: Long = XmasRanges.initialRanges.permutations()

    println("Rejected: $rejectedResult")
    println("Accepted: $acceptedResult")
    println("Sum of  : ${acceptedResult + rejectedResult}")
    println("Perms   : $totalPermutations")

    return acceptedResult
}

private data class SimulationState(val nextWorkflowId: String, val ranges: XmasRanges)

private fun createWorkflowMap(workflows: List<Workflow>): Map<String, Workflow> {
    val workflowMap = buildMap {
        for (workflow in workflows) {
            put(workflow.name, workflow)
        }
    }
    return workflowMap
}

private fun simplifyWorkflows(workflows: List<Workflow>): List<Workflow> {
    val replacementMap: MutableMap<String, Result> = HashMap()
    for (workflow in workflows) {
        val rules = workflow.rules
        val lastRule = rules[rules.lastIndex]
        val beforeLastRule = rules[rules.lastIndex - 1]
        if (lastRule.onMatch == beforeLastRule.onMatch) {
            rules.removeAt(rules.lastIndex - 1)
        }
        if (rules.size == 1) {
            replacementMap[workflow.name] = lastRule.onMatch
        }
    }
    val newWorkflows: MutableList<Workflow> = ArrayList()
    for (workflow in workflows) {
        if (workflow.name in replacementMap.keys) {
            continue
        }
        val newRules: MutableList<Rule> = ArrayList()
        for (rule in workflow.rules) {
            if (rule.onMatch.type == ResultType.REDIRECT) {
                val oldTarget = rule.onMatch.to!!
                val replacementMatch: Result = replacementMap[oldTarget] ?: rule.onMatch
                val newRule = rule.copy(onMatch = replacementMatch)
                newRules.add(newRule)
            } else {
                newRules.add(rule)
            }
        }
        workflow.rules.clear()
        workflow.rules.addAll(newRules)
        newWorkflows.add(workflow)
    }

    return newWorkflows
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

//    var actualWorkflows: List<Workflow> = workflows

//    var round = 0
//    while (true) {
//        round += 1
//    val simplifiedWorkflows = simplifyWorkflows(actualWorkflows)
//        if (simplifiedWorkflows == actualWorkflows) {
//            break
//        }
//    actualWorkflows = simplifiedWorkflows
//    }

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