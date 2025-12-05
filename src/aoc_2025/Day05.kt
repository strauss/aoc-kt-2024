package aoc_2025

import aoc_util.readInput2025
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList

fun main() {
    val testInput: List<String> = readInput2025("Day05_test2")
    val (testRanges, testCandidates) = parseInput(testInput)
    val testResult = countFresh(testRanges, testCandidates)
    println("Test result: $testResult")
    val test2Result = countFreshTwo(testRanges)
    println("Test2Result: $test2Result")

    val input: List<String> = readInput2025("Day05")
    val (ranges, candidates) = parseInput(input)
    val result = countFresh(ranges, candidates)
    println("Result: $result")
    val result2 = countFreshTwo(ranges)
    println("Result2: $result2")
}

private fun countFresh(ranges: List<LongRange>, candidates: List<Long>): Int {
    return candidates.asSequence()
        .filter { candidate -> ranges.any { range: LongRange -> range.contains(candidate) } }
        .count()
}

private fun countFreshTwo(ranges: List<LongRange>): Long {
    val mlr = MultiLongRange()
    ranges.forEach { mlr.add(it) }
    return mlr.size()
}

private class MultiLongRange() {
    private val ranges: MutableList<LongRange> = ArrayList()

    fun add(range: LongRange) {
        val ri = ranges.iterator()
        var added = false
        while (ri.hasNext()) {
            val cr = ri.next()
            val (low, high) = range.countOverlapSize(cr)
            if (low > 0L || high > 0L) {
                ri.remove()
                fuseAndAdd(cr, range, low, high)
                added = true
                break
            }
        }
        if (!added) {
            ranges.add(range)
        }
    }

    private fun fuseAndAdd(currentRange: LongRange, newRange: LongRange, low: Long, high: Long) {
        val fusedRange = if (low > 0 && high > 0) {
            if (currentRange.size() > newRange.size()) currentRange else newRange
        } else if (low > 0) {
            currentRange.first..newRange.last
        } else {
            assert((high > 0))
            newRange.first..currentRange.last
        }
        add(fusedRange)
    }

    fun size(): Long = ranges.asSequence().map { it.size() }.sum()

    fun contains(element: Long) = ranges.asSequence().any { it.contains(element) }

}

private fun LongRange.size(): Long = this.last - this.first + 1

private fun LongRange.countOverlapSize(other: LongRange): Pair<Long, Long> {
    val smaller: LongRange
    val bigger: LongRange
    if (this.first < other.first) {
        smaller = this
        bigger = other
    } else {
        smaller = other
        bigger = this
    }
    if (bigger.first == smaller.first && bigger.last == smaller.last) {
        return smaller.size() to bigger.size()
    }
    if (bigger.first in smaller && bigger.last in smaller) {
        return bigger.first - smaller.first + 1 to smaller.last - bigger.last + 1
    }
    if (bigger.first !in smaller) {
        return 0L to 0L
    }
    val overlap = smaller.last - bigger.first + 1
    return if (this == smaller) 0L to overlap else overlap to 0L
}

private fun parseInput(input: List<String>): Pair<List<LongRange>, List<Long>> {
    val ranges = ArrayList<LongRange>()
    val candidates: MutableList<Long> = PrimitiveLongArrayList()

    for (line in input) {
        if (line.isBlank()) {
            continue
        }
        if (line.contains('-')) {
            val bounds = line.split('-')
            ranges.add(bounds[0].toLong()..bounds[1].toLong())
        } else {
            candidates.add(line.toLong())
        }
    }

    return Pair(ranges, candidates)
}