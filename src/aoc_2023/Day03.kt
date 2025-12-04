package aoc_2023

import aoc_util.*
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder

private const val gear = '*'

fun main() {
    val testInput = readInput2023("Day03_test")
    val (testArray, testNumbers) = parseInput(testInput)
    val testResult = partNumbers(testArray)
    println("Test result: $testResult")
    val testGears = gears(testArray, testNumbers)
    println("Test gears: $testGears")

    val input = readInput2023("Day03")
    val (array, numbers) = parseInput(input)
    val result = partNumbers(array)
    println("Result: $result")
    val gears = gears(array, numbers)
    // 71774808 was wrong
    println("Gears: $gears")
}

private fun gears(array: PrimitiveMultiDimArray<Char>, numbersByRow: Map<Int, List<IntAndLocation>>): Int {
    val gearLocations = findGearLocations(array)
    var sum = 0
    for (location in gearLocations) {
        val (row, col) = location
        val adjacentNumbers: Set<IntAndLocation> = determineAdjacentNumbers(array, numbersByRow, row, col)
        if (adjacentNumbers.size == 2) {
            var product = 1
            for ((adjacentNumber, _, _) in adjacentNumbers) {
                product *= adjacentNumber
            }
            sum += product
        }
    }
    return sum
}

private fun determineAdjacentNumbers(
    array: PrimitiveMultiDimArray<Char>,
    numbersByRow: Map<Int, List<IntAndLocation>>,
    row: Int,
    col: Int
): Set<IntAndLocation> {
    val adjacentValues: List<Pair<Char, Pair<Int, Int>>> = array.getAdjacentValues(row, col) { it.isLatinDigit() }
    val adjacentNumbers = HashSet<IntAndLocation>()
    for ((_, location) in adjacentValues) {
        val numberCandidates: List<IntAndLocation> = numbersByRow[location.first] ?: emptyList()
        for (number in numberCandidates) {
            if (location.second in number.range) {
                adjacentNumbers.add(number)
            }
        }
    }
    return adjacentNumbers
}


private fun determineAdjacentNumbersWrong(
    array: PrimitiveMultiDimArray<Char>,
    numbersByRow: Map<Int, List<IntAndLocation>>,
    row: Int,
    col: Int
): Set<IntAndLocation> {
    val numberCandidates = (numbersByRow[row - 1] ?: emptyList()) + (numbersByRow[row + 1] ?: emptyList())
    val adjacentValues: List<Pair<Char, Pair<Int, Int>>> = array.getAdjacentValues(row, col) { it.isLatinDigit() }
    val adjacentNumbers = HashSet<IntAndLocation>()
    for (candidate in numberCandidates) {
        val (num, nr, range) = candidate
        for ((_, al) in adjacentValues) {
            val (ar, ac) = al
            if (ar == nr && ac in range) {
                adjacentNumbers.add(candidate)
            }
        }
    }
    return adjacentNumbers
}

private fun findGearLocations(array: PrimitiveMultiDimArray<Char>): List<Pair<Int, Int>> {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    val result: MutableList<Pair<Int, Int>> = mutableListOf()
    for (row in 0..<height) {
        for (col in 0..<width) {
            if (array[row, col] == gear) {
                result.add(Pair(row, col))
            }
        }
    }
    return result
}

private fun partNumbers(array: PrimitiveMultiDimArray<Char>): Int {
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
                val adjacentValues = getAdjacentValuesExceptEastWest(array, row, col).filter { it.first != '.' }
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

fun getAdjacentValuesExceptEastWest(
    array: PrimitiveMultiDimArray<Char>,
    row: Int,
    col: Int
): List<Pair<Char, Pair<Int, Int>>> {
    val result: MutableList<Pair<Char, Pair<Int, Int>>> = ArrayList()

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

private fun parseInput(input: List<String>): Pair<PrimitiveMultiDimArray<Char>, Map<Int, List<IntAndLocation>>> {
    val array = parseInputAsMultiDimArray(input)
    val result: MutableMap<Int, List<IntAndLocation>> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<List<IntAndLocation>>().create()
    var row = 0
    for (line in input) {
        val elements: List<IntAndLocation> = line.extractInts(row)
        result[row] = elements
        row += 1
    }
    return Pair(array, result)
}
