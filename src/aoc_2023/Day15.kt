package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLine = readInput2023("Day15_test")[0]
    val testInput = parseInput(testLine)
    solve("Test result", testInput, ::hashAll)
    solve("Test 2 result", testInput, ::lenseFlare)

    val line = readInput2023("Day15")[0]
    val input = parseInput(line)
    solve("Result", input, ::hashAll)
    solve("Result2", input, ::lenseFlare)
}

private fun lenseFlare(input: List<String>): Long {
    val buckets = Array<LinkedHashMap<String, Int>>(256) { LinkedHashMap() }

    for (command in input) {
        if (command.contains('-')) { // remove operation
            val ignoreFrom = command.indexOf('-')
            val label = command.substring(0..<ignoreFrom)
            val bucketIndex = label.HASH()
            buckets[bucketIndex].remove(label)
        } else { // insert or update operation
            assert(command.contains('='))
            val separatorIndex = command.indexOf('=')
            val label = command.substring(0..<separatorIndex)
            val power = command.substring(separatorIndex + 1..<command.length).toInt()
            val bucketIndex = label.HASH()
            buckets[bucketIndex][label] = power
        }
    }

    var result = 0L
    var box = 0
    for (map in buckets) {
        if (map.isNotEmpty()) {
            val mapAsList = map.entries.toList()
            for (idx in mapAsList.indices) {
                val (_, power) = mapAsList[idx]
                result += (box + 1) * (idx + 1) * power
            }
        }
        box += 1
    }

    return result
}

private fun hashAll(input: List<String>): Long {
    var out = 0L
    for (string in input) {
        out += string.HASH()
    }
    return out
}

private val splitPattern = ",".toRegex()

private fun parseInput(line: String): List<String> = splitPattern.split(line)

private fun String.HASH(): Int {
    var value = 0
    for (c in this) {
        value += c.code
        value *= 17
        value %= 256
    }
    return value
}