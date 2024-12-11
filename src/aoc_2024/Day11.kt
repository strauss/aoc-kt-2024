package aoc_2024

import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import java.util.*
import kotlin.math.min

fun main() {
    val testList: List<String> = readInput2024("Day11_test")
    val testArray = parseInput(testList)
    val testBlinks = 25
    val testResult1 = blink(testArray, 0, testBlinks, 1)
    println("Test result 1: ${testResult1.size}")
//    println(testResult1)
    println("Test result 1 faster: ${blinkFaster(testArray, testBlinks)}")

    val list: List<String> = readInput2024("Day11")
    val array = parseInput(list)
    val result1 = blink(array, 0, 25, 1)
    println("Result 1: ${result1.size}")
    val start = System.currentTimeMillis()
    val result2 = blinkFaster(array, 75)
    println("Result 2: $result2")
    val end = System.currentTimeMillis()
    println("Time: ${(end - start).toDouble() / 1000.0} seconds")
}

private fun blinkFaster(input: List<Long>, blinks: Int): Long {
    val laMap: MutableMap<Long, Long> = HashTableBasedMapBuilder.useLongKey().useLongValue().create()
    for (long in input) {
        laMap[long] = (laMap[long] ?: 0) + 1
    }
    val result: Map<Long, Long> = internalBlinkFaster(laMap, 0, blinks)
    return result.values.sum()
}

private tailrec fun internalBlinkFaster(input: Map<Long, Long>, currentBlink: Int, blinks: Int): Map<Long, Long> {
    if (currentBlink == blinks) {
        return input
    }
    val out: MutableMap<Long, Long> = HashTableBasedMapBuilder.useLongKey().useLongValue().create()
    for ((key: Long, currentKeyCount: Long) in input.entries) {
        val local: MutableMap<Long, Long> = HashTableBasedMapBuilder.useLongKey().useLongValue().create()

        // results applied to one number of the input. Each number is computed exactly once.
        val localResult: List<Long> = applyRules(key)

        // count numbers in result
        for (lr: Long in localResult) {
            local[lr] = (local[lr] ?: 0) + 1
        }

        for (lr in local.entries) {
            val currentEntryOutValue = out[lr.key] ?: 0
            // Magic formula :-)
            val newEntryOutValue = currentEntryOutValue + lr.value * currentKeyCount
            out[lr.key] = newEntryOutValue
        }
    }
    return internalBlinkFaster(out, currentBlink + 1, blinks)
}

private fun applyRules(current: Long): List<Long> {
    // rule 1
    if (current == 0L) {
        return listOf(1L)
    }

    // rule 2
    val digits = splitDigits(current)
    if (digits.isNotEmpty() && digits.size % 2 == 0) {
        val out = PrimitiveLongArrayList(2)
        out.add(fuseDigits(digits.subList(0, digits.size / 2)))
        out.add(fuseDigits(digits.subList(digits.size / 2, digits.size)))
        return out
    }

    // rule 3
    return listOf(current * 2024L)
}

private tailrec fun blink(input: List<Long>, currentBlink: Int, blinks: Int, threads: Int = 1): List<Long> {
    if (currentBlink == blinks) {
        return input
    }
    val out: MutableList<Long> = LinkedList()

    val actualThreadSize: Int
    actualThreadSize = if (input.size < 100) 1 else min(threads, 12)

    val threadList = ArrayList<PartialSolutionRunner>()
    val chunkSize = (input.size / actualThreadSize)

    var limit = chunkSize
    var start = 0
    for (i in 1..actualThreadSize) {
        val actualLimit = if (i < actualThreadSize) limit else input.size
        val partialSolutionRunner = PartialSolutionRunner(input.subList(start, actualLimit))
//        println("Adding worker [$start, $actualLimit[")
        threadList.add(partialSolutionRunner)
        start = limit
        limit += chunkSize
    }

    for (partialSolutionRunner in threadList) {
        partialSolutionRunner.start()
    }
    for (partialSolutionRunner in threadList) {
        partialSolutionRunner.join()
        out.addAll(partialSolutionRunner.partialOut)
    }

//    println(out)
    return blink(out, currentBlink + 1, blinks)
}

private class PartialSolutionRunner(val partialIn: List<Long>) : Thread() {
    val partialOut = PrimitiveLongArrayList()
    override fun run() {
        for (current in partialIn) {
            // rule 1
            if (current == 0L) {
                partialOut.add(1)
                continue
            }

            // rule 2
            val digits = splitDigits(current)
            if (digits.isNotEmpty() && digits.size % 2 == 0) {
                partialOut.add(fuseDigits(digits.subList(0, digits.size / 2)))
                partialOut.add(fuseDigits(digits.subList(digits.size / 2, digits.size)))
                continue
            }

            // rule 3
            partialOut.add(current * 2024L)
        }
    }
}

private fun splitDigits(number: Long): List<Long> {
    var count = PrimitiveLongArrayList()
    var work = number
    while (work != 0L) {
        count.add(work % 10L)
        work /= 10
    }
    return count.reversed()
}

private fun fuseDigits(digits: List<Long>): Long {
    var out = 0L
    for (i in digits) {
        out *= 10L
        out += i
    }
    return out
}

private fun parseInput(input: List<String>): List<Long> {
    val outList: MutableList<Long> = PrimitiveLongArrayList()
    input.forEach { line ->
        val split = line.split(' ')
        split.forEach { outList.add(it.trim().toLong()) }
    }
    return outList
}
