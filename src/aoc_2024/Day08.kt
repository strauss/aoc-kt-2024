package aoc_2024

import aoc_util.CombinatorialIterator
import aoc_util.PrimitiveMultiDimArray
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder

fun main() {
    val testList = readInput2024("Day08_test")
    val testArray = parseInputAsMultiDimArray(testList)
    val testResult = countAntinodes(testArray)
    val testResult2 = countAntinodes(testArray, true)
    println("Test result 1: $testResult")
    println("Test result 2: $testResult2")

    val realList = readInput2024("Day08")
    val realArray = parseInputAsMultiDimArray(realList)
    val realResult = countAntinodes(realArray)
    val realResult2 = countAntinodes(realArray, true)
    println("Real result 1: $realResult")
    println("Real result 2: $realResult2")

}

private fun countAntinodes(array: PrimitiveMultiDimArray<Char>, harmonics: Boolean = false): Int {
    val antennaMap: MutableMap<Char, MutableList<Pair<Int, Int>>> = buildAntennaMap(array)

    val antinodes = mutableSetOf<Pair<Int, Int>>()

    for (value in antennaMap.values) {
        val iterator = CombinatorialIterator(value, 2, true)
        iterator.iterate { innerList ->
            val a = innerList[0]
            val b = innerList[1]
            val distanceFirst = a.first - b.first
            val distanceSecond = a.second - b.second
            val anA = Pair(a.first + distanceFirst, a.second + distanceSecond)
            if (inbounds(anA, array)) {
                antinodes.add(anA)
            }
            val anB = Pair(b.first - distanceFirst, b.second - distanceSecond)
            if (inbounds(anB, array)) {
                antinodes.add(anB)
            }
            if (harmonics) {
                var current = a
                while (inbounds(current, array)) {
                    antinodes.add(current)
                    current = Pair(current.first + distanceFirst, current.second + distanceSecond)
                }
                current = a
                while (inbounds(current, array)) {
                    antinodes.add(current)
                    current = Pair(current.first - distanceFirst, current.second - distanceSecond)
                }

            }
        }
    }

    return antinodes.size
}

private fun buildAntennaMap(array: PrimitiveMultiDimArray<Char>): MutableMap<Char, MutableList<Pair<Int, Int>>> {
    val antennaMap: MutableMap<Char, MutableList<Pair<Int, Int>>> =
        HashTableBasedMapBuilder.useCharKey().useArbitraryTypeValue<MutableList<Pair<Int, Int>>>().create()
    for (j in 0 until array.getDimensionSize(0)) {
        for (i in 0 until array.getDimensionSize(1)) {
            val currentChar = array[j, i]
            if (currentChar != '.') {
                val list: MutableList<Pair<Int, Int>> = antennaMap.getOrPut(currentChar) { ArrayList() }
                list.add(Pair(i, j))
            }
        }
    }
    return antennaMap
}

private fun inbounds(position: Pair<Int, Int>, array: PrimitiveMultiDimArray<Char>) =
    position.first in 0..<array.getDimensionSize(0) && position.second in 0..<array.getDimensionSize(1)

