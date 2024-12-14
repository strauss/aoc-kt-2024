package aoc_2023

import aoc_util.*
import de.dreamcube.hornet_queen.list.PrimitiveCharArrayList
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList

fun main() {
    val testInput = readInput2023("Day03_test")
    val (testArray, testNumbers) = parseInput(testInput)
    val testResult = partNumbers(testArray, testNumbers)
    println("Test result: $testResult")
    val input = readInput2023("Day03")
    val (array, numbers) = parseInput(input)
    val result = partNumbers(array, numbers)
    println("Result: $result")
}

private fun partNumbers(array: PrimitiveMultiDimArray<Char>, numbers: List<Int>): Int {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    var sum = 0
    for (row in 0..<height) {
        var touchedSymbol = false
        val currentNumberReading = StringBuilder()
        for (col in 0..<width) {
            val char = array[row, col]
            if (char.isLatinDigit()) {
                currentNumberReading.append(char)
                val adjacentValues = getAdjacentValues(array, row, col).filter { it != '.' }
                if (adjacentValues.isNotEmpty()) {
                    touchedSymbol = true
                }
            } else {
                if (touchedSymbol && currentNumberReading.isNotEmpty()) {
                    val currentNumber = currentNumberReading.toString().toInt()
                    sum += currentNumber
                }
                currentNumberReading.clear()
                touchedSymbol = false
            }
        }
        if (touchedSymbol && currentNumberReading.isNotEmpty()) {
            val currentNumber = currentNumberReading.toString().toInt()
            sum += currentNumber
        }
    }
    return sum
}

private fun Char.isLatinDigit() = this in '0'..'9'

fun getAdjacentValues(array: PrimitiveMultiDimArray<Char>, row: Int, col: Int): List<Char> {
    val result = PrimitiveCharArrayList()

    result.addIfInBounds(array, row - 1, col) // north
    result.addIfInBounds(array, row - 1, col + 1) // north-east
    result.addIfInBounds(array, row, col + 1) { !it.isLatinDigit() } // east
    result.addIfInBounds(array, row + 1, col + 1) // south-east
    result.addIfInBounds(array, row + 1, col) // south
    result.addIfInBounds(array, row + 1, col - 1) // south-west
    result.addIfInBounds(array, row, col - 1) { !it.isLatinDigit() } // west
    result.addIfInBounds(array, row - 1, col - 1) // north-west

    return result
}

private fun parseInput(input: List<String>): Pair<PrimitiveMultiDimArray<Char>, List<Int>> {
    val array = parseInputAsMultiDimArray(input)
    val intList = PrimitiveIntArrayList()
    for (line in input) {
        intList.addAll(line.extractInts())
    }
    return Pair(array, intList)
}