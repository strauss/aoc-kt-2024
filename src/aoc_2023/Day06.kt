package aoc_2023

import aoc_util.extractSchlong
import aoc_util.readInput2023

fun main() {
    val testLines: List<String> = readInput2023("Day06_test")
    val testInput = extractTimeDistancePairs(testLines[0], testLines[1])
    val testResult = aggregateResult1(testInput)
    println("Test result: $testResult")
    val test2Result = aggregateResult2(testInput)
    println("Test2 result: $test2Result")

    val lines = readInput2023("Day06")
    val input = extractTimeDistancePairs(lines[0], lines[1])
    val result = aggregateResult1(input)
    println("Result: $result")
    val result2 = aggregateResult2(input)
    println("Result 2: $result2")

}

private fun extractTimeDistancePairs(times: String, distances: String): List<Pair<Long, Long>> {
    val timeList = times.extractSchlong()
    val distanceList = distances.extractSchlong()
    return timeList.zip(distanceList)
}

private fun Long.allSplits(): Sequence<Pair<Long, Long>> {
    val range = if (this > 0L) 0L..this else this..0L
    return sequence {
        for (ll in range) {
            yield(ll to this@allSplits - ll)
        }
    }
}

private fun Pair<Long, Long>.countWins(): Long {
    return this.first.allSplits()
        .filter { (left, right) -> left * right > this@countWins.second }
        .count()
        .toLong()
}

private fun aggregateResult1(input: List<Pair<Long, Long>>): Long {
    var result = 1L
    input.asSequence().map { it.countWins() }.forEach { result *= it }
    return result
}

private fun aggregateResult2(input: List<Pair<Long, Long>>): Long {
    var time = ""
    var distance = ""
    for ((tt, dd) in input) {
        time += tt
        distance += dd
    }
    return (time.toLong() to distance.toLong()).countWins()
}