package aoc_2025

import aoc_util.Primitive2DCharArray
import aoc_util.readInput2025
import de.dreamcube.hornet_queen.array.PrimitiveLongArray
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

fun main() {
    val testLines = readInput2025("Day07_test")
    val testArray = Primitive2DCharArray.parseFromLines(testLines)
    val (testSplits, testWorlds) = tachyonQuantumSimulation(testArray)
    println("Test result: $testSplits")
    println("Test2 result: $testWorlds")

    val lines = readInput2025("Day07")
    val array = Primitive2DCharArray.parseFromLines(lines)
    val (splits, worlds) = tachyonQuantumSimulation(array)
    println("Result: $splits")
    println("Result2: $worlds")
}

private fun tachyonQuantumSimulation(array: Primitive2DCharArray): Pair<Int, Long> {
    var splits = 0 // Result for Part 1
    val activeColumns: MutableSet<Int> = PrimitiveIntSetB()
    val intensities = PrimitiveLongArray(array.width) // Result for part 2
    for (row in 0..<array.height) {
        if (row == 0) {
            val startIndex = array.getRow(row).indexOf('S')
            activeColumns.add(startIndex)
            intensities[startIndex] = 1
            continue
        }
        val addColumns: MutableSet<Int> = PrimitiveIntSetB()
        val removeColumns: MutableSet<Int> = PrimitiveIntSetB()
        for (col in activeColumns) {
            val current = array[row, col]
            if (current == '^') {
                removeColumns.add(col)
                addColumns.add(col - 1)
                addColumns.add(col + 1)
                splits += 1
                intensities[col - 1] += intensities[col]
                intensities[col + 1] += intensities[col]
                intensities[col] = 0
            }
        }
        activeColumns.addAll(addColumns)
        activeColumns.removeAll(removeColumns)
    }
    return splits to intensities.sum()
}