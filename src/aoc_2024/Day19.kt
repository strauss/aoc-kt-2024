package aoc_2024

import aoc_util.readInput2024

fun main() {
    val testInput = readInput2024("Day19_test")
    val (testTokens, testSamples) = parseInput(testInput)
    val testTokenPool = TokenPool(testTokens)
    val possibleTestSamples = countPossibleSamples(testSamples, testTokenPool)
    println("Test result: $possibleTestSamples")

    val input = readInput2024("Day19")
    val (tokens, samples) = parseInput(input)
    val tokenPool = TokenPool(tokens)
    val possibleSamples = countPossibleSamples(samples, tokenPool)
    println("Result: $possibleSamples")
}

private fun countPossibleSamples(samples: List<String>, tokenPool: TokenPool): Int {
    var out = 0
    for (sample in samples) {
        val samplePossible = samplePossible(sample, tokenPool)
        if (samplePossible) {
            out += 1
        }
//        println("$sample: $samplePossible")
    }
    return out
}

private fun samplePossible(sample: String, tokenPool: TokenPool, maxKeySize: Int = 4): Boolean {
    if (sample.isBlank()) {
        return true
    }
    val keyList = mutableListOf<String>()
    var currentKeySize = maxKeySize
    while (currentKeySize > 0) {
        if (sample.length >= currentKeySize) {
            keyList.add(sample.substring(0..<currentKeySize))
        }
        currentKeySize -= 1
    }

    for (key in keyList) {
        val possibleTokens = tokenPool.tokenMap[key] ?: emptyList()
        for (token in possibleTokens) {
            if (sample.startsWith(token)) {
                val restPossible = samplePossible(sample.substring(token.length..<sample.length), tokenPool, maxKeySize)
                if (!restPossible) {
                    continue
                }
                return true
            }
        }
    }
    return false
}

private data class TokenPool(val availableTokens: List<String>, val maxKeySize: Int = 4) {
    private val internalTokenMap: MutableMap<String, MutableList<String>> = HashMap()
    val tokenMap: Map<String, List<String>>
        get() = internalTokenMap

    init {
        for (availableToken in availableTokens) {
            if (availableToken.isBlank()) {
                continue
            }
            var currentKeySize = maxKeySize
            while (currentKeySize > 0) {
                if (availableToken.length >= currentKeySize) {
                    val key = availableToken.substring(0..<currentKeySize)
                    internalTokenMap.getOrPut(key) { mutableListOf() }.add(availableToken)
                    break
                }
                currentKeySize -= 1
            }
        }
    }
}

private fun parseInput(input: List<String>): Pair<List<String>, List<String>> {
    val iterator = input.iterator()
    if (!iterator.hasNext()) {
        return Pair(emptyList(), emptyList())
    }
    val line1 = iterator.next()
    val list1: List<String> = line1.split(',').map { it.trim() }
    val list2 = ArrayList<String>()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (next.isBlank()) {
            continue
        }
        list2.add(next.trim())
    }
    return Pair(list1, list2)
}