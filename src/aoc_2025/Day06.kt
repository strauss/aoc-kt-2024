package aoc_2025

import aoc_util.Primitive2DCharArray
import aoc_util.extractSchlong
import aoc_util.readInput2025

fun main() {
    val testLines = readInput2025("Day06_test")
    val testInput = parseInput(testLines)
    val testResult = evaluateInput(testInput)
    println("Test result: $testResult")

    val testArray: Primitive2DCharArray = Primitive2DCharArray.parseFromLines(testLines)
    val testTArray = testArray.transpose()
    val testInput2 = testTArray.rows()
    val testResult2 = evaluateInput2(testInput2)
    println("Test result 2: $testResult2")

    val lines = readInput2025("Day06")
    val input = parseInput(lines)
    val result = evaluateInput(input)
    println("Result: $result")

    val array = Primitive2DCharArray.parseFromLines(lines)
    val tarray = array.transpose()
    val input2 = tarray.rows()
    val result2 = evaluateInput2(input2)
    println("Result 2: $result2")
}

fun evaluateInput2(lines: List<String>): Long {
    var result = 0L
    var operator: String? = null
    val numbers = ArrayList<Long>()
    fun calculateAndAdd(op: String) {
        val cresult = calculate(op, numbers)
        result += cresult
    }

    for (line in lines) {
        if (line.isBlank()) {
            continue
        }

        val newOperator = when {
            line.contains('+') -> "+"
            line.contains('*') -> "*"
            else -> null
        }
        if (newOperator != null) {
            if (operator != null) {
                calculateAndAdd(operator)
            }
            operator = newOperator
            numbers.clear()
        }

        numbers.add(line.extractSchlong()[0])
    }
    if (operator != null) {
        calculateAndAdd(operator)
    }
    return result
}

private fun evaluateInput(input: List<Pair<List<Long>, String>>): Long {
    return input.sumOf { evaluateOne(it) }
}

private fun evaluateOne(one: Pair<List<Long>, String>): Long {
    val (numbers, operator) = one
    return calculate(operator, numbers)
}

private fun calculate(operator: String, numbers: List<Long>): Long {
    return when (operator) {
        "+" -> numbers.sum()
        "*" -> {
            var result = 1L
            numbers.forEach { result *= it }
            result
        }

        else -> 0L
    }
}

private fun parseInput(lines: List<String>): List<Pair<List<Long>, String>> {
    val inputLines: MutableList<List<Long>> = ArrayList()
    var operators: List<String> = ArrayList()
    for (i in lines.indices) {
        if (i < lines.size - 1) {
            inputLines.add(lines[i].extractSchlong())
        } else {
            operators = lines[i].trim().split("\\s+".toRegex())
        }
    }
    val columns = ArrayList<List<Long>>()
    for (col in operators.indices) {
        val column = ArrayList<Long>()
        for (row in lines.indices) {
            if (row < lines.size - 1) {
                column.add(inputLines[row][col])
            }
        }
        columns.add(column)
    }
    return columns.zip(operators)
}