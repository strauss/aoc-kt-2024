package aoc_2025

import aoc_util.PrimitiveMultiDimArray
import aoc_util.extractSchlong
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2025
import de.dreamcube.hornet_queen.array.PrimitiveCharArray
import java.math.BigInteger

fun main() {
    val testLines = readInput2025("Day06_test")
    val testInput = parseInput(testLines)
    val testResult = evaluateInput(testInput)
    println("Test result: $testResult")

    val testArray: PrimitiveMultiDimArray<Char> = parseInputAsMultiDimArray(testLines)
    val testTArray = testArray.transpose()
    val testInput2 = testTArray.toLines()
    val testResult2 = evaluateInput2(testInput2)
    println("Test result 2: $testResult2")

    val lines = readInput2025("Day06")
    val input = parseInput(lines)
    val result = evaluateInput(input)
    println("Result: $result")

    val array = parseInputAsMultiDimArray(lines)
//    array.show()
    val tarray = array.transpose()
    tarray.show()
    val input2 = tarray.toLines()
    val result2 = evaluateInput2(input2)
    println("Result 2: $result2")
}

fun evaluateInput2(lines: List<String>): BigInteger {
    var result = BigInteger.ZERO
    var operator: String? = null
    val numbers = ArrayList<Long>()
    fun calculateAndAdd(op: String) {
        val cresult = calculate(op, numbers)
//        println(cresult)
        result += cresult.toBigInteger()
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

fun PrimitiveMultiDimArray<Char>.transpose(): PrimitiveMultiDimArray<Char> {
    val height = this.getDimensionSize(0)
    val width = this.getDimensionSize(1)
    val out: PrimitiveMultiDimArray<Char> = PrimitiveMultiDimArray(width, height) { size -> PrimitiveCharArray(size) }
    for (oldRow in 0..<height) {
        for (oldCol in 0..<width) {
            out[oldCol, oldRow] = this[oldRow, oldCol]
        }
    }
    return out
}

fun PrimitiveMultiDimArray<Char>.show() {
    val height = this.getDimensionSize(0)
    val width = this.getDimensionSize(1)
    for (row in 0..<height) {
        for (col in 0..<width) {
            print(this[row, col])
        }
        println()
    }
}

fun PrimitiveMultiDimArray<Char>.toLines(): List<String> {
    val height = this.getDimensionSize(0)
    val width = this.getDimensionSize(1)
    val result = ArrayList<String>()
    for (row in 0..<height) {
        val charArray = CharArray(width)
        for (col in 0..<width) {
            charArray[col] = this[row, col]
        }
        result.add(String(charArray))
    }
    return result
}

private fun evaluateInput(input: List<Pair<List<Long>, String>>): Long {
    return input.map { evaluateOne(it) }.sum()
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