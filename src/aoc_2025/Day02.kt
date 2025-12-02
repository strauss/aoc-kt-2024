package aoc_2025

import aoc_util.readInput2025

fun main() {
    val testInput: List<String> = readInput2025("Day02_test").asSequence()
        .flatMap { it.split(',') }
        .toList()
    val testResult = checkInput(testInput, ::checkId1)
    println("Test result: $testResult")
    val testResult2 = checkInput(testInput, ::checkId2)
    println("Test result 2: $testResult2")

    val input = readInput2025("Day02").asSequence()
        .flatMap { it.split(',') }
        .toList()
    val result = checkInput(input, ::checkId1)
    println("Result: $result")
    val result2 = checkInput(input, ::checkId2)
    println("Result 2: $result2")

//    println(checkRange(11L..22L, ::checkId2))
//    println(checkRange(95L..115L, ::checkId2))
//    println(checkRange(998L..1012L, ::checkId2))
//    println(checkRange(1188511880L..1188511890, ::checkId2))
//    println(checkRange(998L..1012L, ::checkId2))
//    println(checkRange(2121212118L..2121212124L, ::checkId2))
}

fun checkId2(id: String): Boolean {
    val maxSubLength = id.length / 2
    for (currentSubLengh in 1..maxSubLength) {
        val splitted = buildList {
            for (currentStartSplit in 0..id.length step currentSubLengh) {
                val currentEndSplit = (currentStartSplit + currentSubLengh - 1).coerceAtMost(id.length - 1)
                val substring = id.substring(currentStartSplit..currentEndSplit)
                if (substring.isNotEmpty()) {
                    add(substring)
                }
            }
        }
        if (splitted.allEquals()) {
            return true
        }
    }
    return false
}

fun List<Any>.allEquals(): Boolean {
    if (size <= 1) {
        return true
    }
    val compareWith = this[0]
    for (any in this) {
        if (any != compareWith) {
            return false
        }
    }
    return true
}

fun checkId1(id: String): Boolean {
    val left = id.substring(0..(id.length - 1) / 2)
    val right = id.substring(id.length - id.length / 2..id.length - 1)
    return left == right
}

fun checkRange(range: LongRange, checkId: (String) -> Boolean): Long {
    var result: Long = 0L
    for (id in range) {
        if (checkId(id.toString())) {
            result += id
        }
    }
    return result
}

fun checkInput(input: List<String>, checkId: (String) -> Boolean): Long {
    var result = 0L
    for (currentStringRange in input) {
        val splittedRange = currentStringRange.split('-')
        val left = splittedRange[0].toLong()
        val right = splittedRange[1].toLong()
        result += checkRange(left..right, checkId)
    }
    return result
}
