package aoc_2024

import aoc_util.CombinatorialIterator
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveByteArrayList
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import kotlin.math.abs

fun main() {
    val testInput = readInput2024("Day22_test")
    val testSeeds = testInput.map { it.toLong() }
    val testResult = evaluateMonkeyNumbers(testSeeds, 2000)
    println("Test result: $testResult")

    println()

    val input = readInput2024("Day22")
    val seeds = input.map { it.toLong() }
    val result = evaluateMonkeyNumbers(seeds)
    println("Result: $result")

    val start = System.currentTimeMillis()
//    println(countBananaOutcome(listOf(1, 2, 3, 2024)))
    println(countBananaOutcome(seeds))
    val duration = System.currentTimeMillis() - start
    println("Duration: ${duration.toDouble() / 1000.0} s")
}

private val possibleSequenceElements: List<Byte> = createPossibleSequenceElements()

private fun createPossibleSequenceElements(): List<Byte> {
    val out = PrimitiveByteArrayList()
    for (i in -9..9) {
        out.add(i.toByte())
    }
    return out
}

private fun countBananaOutcome(seeds: List<Long>, bound: Int = 2000): Pair<List<Byte>, Int> {
    var out = 0
    var winningSequence: List<Byte> = emptyList()
    val biddingLists: MutableList<List<Int>> = mutableListOf()
    val deltaLists: MutableList<List<Byte>> = mutableListOf()
    seeds.forEach {
        val (biddings, deltas) = getDeltaList(it, bound)
        biddingLists.add(biddings)
        deltaLists.add(deltas)
    }

    val sequenceCombinationIterator = CombinatorialIterator(possibleSequenceElements, 4)
//    sequenceCombinationIterator.iterate { sequence ->
    for (i in -9..9) {
        for (j in -9..9) {
            for (k in -9..9) {
                for (m in -9..9) {
                    if (abs(i + j + k + m) > 9 || abs(i + j + k) > 9 || abs(j + k + m) > 9 || abs(i + j) > 9 || abs(j + k) > 9 || abs(k + m) > 9) {
                        // filter out implausible ones
                        continue
                    }
                    val sequence = listOf(i.toByte(), j.toByte(), k.toByte(), m.toByte())
                    var bananas = 0
                    for (n in deltaLists.indices) {
                        val biddings = biddingLists[n]
                        val deltas = deltaLists[n]

                        val currentBananas = countBananasForSequence(biddings, deltas, sequence)
                        bananas += currentBananas
                    }
                    if (bananas > out) {
                        out = bananas
                        winningSequence = sequence
                    }
                }
            }
        }
    }

//    }
    return winningSequence to out
}

private fun countBananasForSequence(biddings: List<Int>, deltas: List<Byte>, sequence: List<Byte>): Int {
    outer@ for (i in deltas.indices) {
        for (j in sequence.indices) {
            if (i + j > deltas.lastIndex) {
                return 0
            }
            val current = deltas[i + j]
            if (current != sequence[j]) {
                continue@outer
            }
        }
        return biddings[i + 3]
    }
    return 0
}

private fun evaluateMonkeyNumbers(seeds: List<Long>, bound: Int = 2000): Long {
    var sum = 0L
    for (seed in seeds) {
        val mng = MonkeyNumberGenerator(seed)
        var number = 0L
        for (i in 1..bound) {
            number = mng.nextNumber()
        }
        sum += number
    }
    return sum
}

private fun getDeltaList(seed: Long, bound: Int = 2000): Pair<List<Int>, List<Byte>> {
    val biddings = PrimitiveIntArrayList()
    val deltas = PrimitiveByteArrayList()
    val mng = MonkeyNumberGenerator(seed)
    var lastNumber = seed % 10
    for (i in 1..bound) {
        val nextNumber = mng.nextNumber() % 10
        val delta = nextNumber - lastNumber
        biddings.add(nextNumber.toInt())
        deltas.add(delta.toByte())
        lastNumber = nextNumber
    }
    return biddings to deltas
}

private class MonkeyNumberGenerator(val seed: Long) {
    private val firstMult = 64L
    private val firstDivide = 32L
    private val finalMult = 2048L
    private val prune = 16777216

    private var currentNumber = seed

    fun nextNumber(): Long {
        currentNumber = currentNumber xor (currentNumber * firstMult)
        currentNumber %= prune
        currentNumber = currentNumber xor (currentNumber / firstDivide)
        currentNumber %= prune
        currentNumber = currentNumber xor (currentNumber * finalMult)
        currentNumber %= prune
        return currentNumber
    }
}