package aoc_2025

import aoc_util.readInput2025
import java.util.*

fun main() {
    val testInput: List<String> = readInput2025("Day03_test")
    val testResult = solve1(testInput)
    println("Test result: $testResult")
    val testResult2 = solve2(testInput)
    println("Test result 2: $testResult2")

    val input = readInput2025("Day03")
    val result = solve1(input)
    println("Result: $result")
    val result2 = solve2(input)
    println("Result 2: $result2")
}

private fun solve1(numbers: List<String>): Int {
    var result = 0
    for (number in numbers) {
        result += biggestNumber(number)
    }
    return result
}

private fun solve2(numbers: List<String>): Long {
    var result = 0L
    for (number in numbers) {
        val n = biggestBigNumber(number, 12)
        result += n
    }
    return result
}

private fun biggestBigNumber(numberAsString: String, length: Int): Long {
    var b = IntArray(length)
    for (i in 0..<length) {
        b[i] = numberAsString[i].digitToInt()
    }

    for (j in length..<numberAsString.length) {
        val currentReplacement = numberAsString[j].digitToInt()
        val candidates: MutableList<IntArray> = LinkedList()
        for (i in 0..<length) {
            val bb = IntArray(length)
            System.arraycopy(b, 0, bb, 0, length)
            val oldNumber = convertToNumber(bb).toLong()
            val newNumber = convertToNumber(bb, i, currentReplacement).toLong()
            if (newNumber > oldNumber) {
                shiftLeft(bb, i)
                bb[bb.size - 1] = currentReplacement
                candidates.add(bb)
            }
        }
        if (candidates.isNotEmpty()) {
            var maxValue = convertToNumber(b)
            for (bb in candidates) {
                val bbValue = convertToNumber(bb)
                if (bbValue > maxValue) {
                    maxValue = bbValue
                    b = bb
                }
            }
        }
    }

    return convertToNumber(b).toLong()
}

private fun shiftLeft(currentDigits: IntArray, towards: Int) {
    for (i in towards..<currentDigits.size - 1) {
        currentDigits[i] = currentDigits[i + 1]
    }
}

private fun convertToNumber(currentDigits: IntArray, exceptIndex: Int = -1, suffix: Int = -1): String {
    return buildString {
        for (i in 0..<currentDigits.size) {
            if (i != exceptIndex) {
                append(currentDigits[i])
            }
        }
        if (suffix >= 0) {
            append(suffix)
        }
    }
}

private fun biggestNumber(numberAsString: String): Int {
    val b = IntArray(2)

    for (c in numberAsString) {
        val cur = c.digitToInt()
        if ("${b[1]}$cur".toInt() > "${b[0]}${b[1]}".toInt()) {
            b[0] = b[1]
            b[1] = cur
        } else if ("${b[0]}$cur".toInt() > "${b[0]}${b[1]}".toInt()) {
            b[1] = cur
        }
    }
    return "${b[0]}${b[1]}".toInt()
}