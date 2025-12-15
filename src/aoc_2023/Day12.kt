package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day12_test")
    solve("Test complexity", testLines, ::analyseComplexity)
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::solve1)

    val lines = readInput2023("Day12")
    solve("Complexity", lines, ::analyseComplexity)
    val input = parseInput(lines)
    solve("Result", input, ::solve1)
}

private fun solve1(input: List<Pair<String, IntArray>>): Long {
    var out = 0L
    for ((sample, testAgainst) in input) {
        out += countOne(sample, testAgainst)
    }
    return out
}

private fun countOne(sample: String, testAgainst: IntArray): Int {
    val unknownCount = countUnknown(sample)
    return internalCount(sample, testAgainst, unknownCount)
}

private fun internalCount(sample: String, testAgainst: IntArray, maxDepth: Int, currentDepth: Int = 0): Int {
    if (maxDepth == currentDepth) {
        return if (sample.isValid(testAgainst)) 1 else 0
    }
    var currentResult = 0
//    if (sample.stillPossible(testAgainst)) { // prune impossible
    currentResult += internalCount(sample.replaceFirst('?', '.'), testAgainst, maxDepth, currentDepth + 1)
    currentResult += internalCount(sample.replaceFirst('?', '#'), testAgainst, maxDepth, currentDepth + 1)
//    }
    return currentResult
}

private fun parseInput(lines: List<String>): List<Pair<String, IntArray>> {
    val out = ArrayList<Pair<String, IntArray>>()
    for (line in lines) {
        val split = line.split(" ")
        out.add(split[0] to split[1].split(",").map { it.toInt() }.toIntArray())
    }
    return out
}

private fun analyseComplexity(lines: List<String>): Int {
    return lines.asSequence().map { countUnknown(it) }.max()
}

private fun countUnknown(input: String): Int = input.count { c -> c == '?' }

private val splitPattern = "\\.+".toRegex()

private fun String.isValid(withRespectTo: IntArray): Boolean {
    val aResult = analyze()
    return withRespectTo.contentEquals(aResult)
}

private fun String.stillPossible(withRespectTo: IntArray): Boolean {
    val partialSegments = splitPattern.split(this).filter { it.isNotEmpty() }


    // analyze from left (more important)
    for (sIdx in partialSegments.indices) {
        if (sIdx >= withRespectTo.size) {
            break
        }
        val segment = partialSegments[sIdx]
        if (segment.length < withRespectTo[sIdx]) {
            return false
        }
        if (segment.length == withRespectTo[sIdx]) {
            continue
        }
        break
    }

    // analyze from right (because why not?)
    /*
    var sIdx = partialSegments.size - 1
    var wIdx = withRespectTo.size - 1
    while (sIdx >= 0 && wIdx >= 0) {
        val segment = partialSegments[sIdx]
        if (segment.length < withRespectTo[wIdx]) {
            return false
        }
        if (segment.length == withRespectTo[wIdx]) {
            sIdx -= 1
            wIdx -= 1
            continue
        }
        break
    }
    */

    // TODO: there might be more
    return true
}

private fun String.analyze(): IntArray {
    return splitPattern.split(this).asSequence()
        .filter { it.isNotEmpty() }
        .map { it.length }
        .toList().toIntArray()
}