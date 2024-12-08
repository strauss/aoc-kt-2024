package aoc_2024

import aoc_util.CombinatorialIterator
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList

fun main() {
    val operators1 = listOf('+', '*')
    val operators2 = listOf('+', '*', '|')
    val testList = readInput2024("Day07_test")
    val testInput = parseInput(testList)
    val testResult = solve(testInput, operators1)
    println("Test result 1: $testResult")
    val testResult2 = solve(testInput, operators2)
    println("Test result 2: $testResult2")

    val realList = readInput2024("Day07")
    val input = parseInput(realList)
    val result = solve(input, operators1)
    println("Real result: $result")
    val result2 = solve(input, operators2)
    println("Real result2: $result2")

}

private fun solve(input: List<Pair<Long, List<Long>>>, operators: List<Char>): Long {
    var sum = 0L
    input.forEach { (result, input) ->
        val times = input.size - 1
        val currentOperatorCombinations = getOperatorCombinations(operators, times)
        for (currentCombination in currentOperatorCombinations) {
            val currentResult = getResult(input, currentCombination)
            if (currentResult == result) {
                sum += result
                break
            }
        }
    }
    return sum
}

private fun getResult(input: List<Long>, operators: List<Char>): Long {
    var result = input[0]
    for (i in operators.indices) {
        val j = i + 1
        val currentOperator = operators[i]
        if (currentOperator == '+') {
            result += input[j]
        }
        if (currentOperator == '*') {
            result *= input[j]
        }
        if (currentOperator == '|') {
            result = "$result${input[j]}".toLong()
        }
    }
    return result
}

private fun getOperatorCombinations(operators: List<Char>, times: Int): List<List<Char>> {
    val out: MutableList<List<Char>> = ArrayList()
    CombinatorialIterator(operators, times).iterate { out.add(it) }
    return out
}

private fun parseInput(input: List<String>): List<Pair<Long, List<Long>>> {
    val outList: MutableList<Pair<Long, List<Long>>> = ArrayList()
    input.forEach { line ->
        val split = line.split(':')
        val result = split[0].toLong()
        val list = PrimitiveLongArrayList()
        val numbers = split[1].trim().split(' ')
        numbers.forEach { list.add(it.toLong()) }
        outList.add(Pair(result, list))
    }
    return outList
}
