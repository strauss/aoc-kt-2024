package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day19_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::simulateWorkflows)

    val lines = readInput2023("Day19")
    val input = parseInput(lines)
    solve("Result", input, ::simulateWorkflows)

}

private fun simulateWorkflows(input: Pair<List<Workflow>, List<XmasPart>>): Long {
    val (workflows, parts) = input
    val workflowMap = buildMap {
        for (workflow in workflows) {
            put(workflow.name, workflow)
        }
    }
    var result = 0L

    for (part in parts) {
        var currentWorkflow = workflowMap["in"]!!
        simulation@ while (true) {
            rules@ for (rule in currentWorkflow.rules) {
                val match = rule.condition(part)
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
                    val operator = conditionString[1]
                    val number = conditionString.substring(2..conditionString.lastIndex).toInt()
                    val condition = when (operator) {
                        '<' -> { part: XmasPart -> part.getByChar(letter) < number }
                        '>' -> { part: XmasPart -> part.getByChar(letter) > number }
                        else -> { _ -> false }
                    }
                    val onMatch = when (resultString) {
                        "A" -> acceptResult
                        "R" -> rejectResult
                        else -> Result(ResultType.REDIRECT, resultString)
                    }
                    val rule = Rule(onMatch, condition)
                    newWorkflow.addRule(rule)
                } else {
                    val resultString = ruleSegments[0]
                    val onMatch = when (resultString) {
                        "A" -> acceptResult
                        "R" -> rejectResult
                        else -> Result(ResultType.REDIRECT, resultString)
                    }
                    val rule = Rule(onMatch) { _ -> true }
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

private class Rule(val onMatch: Result, val condition: (XmasPart) -> Boolean) {
    fun apply(part: XmasPart): Result = if (condition(part)) onMatch else continueResult
}

private data class Result(val type: ResultType, val to: String? = null)

private val continueResult = Result(ResultType.CONTINUE)
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
        else -> Integer.MIN_VALUE
    }

    fun sum() = x + m + a + s
}