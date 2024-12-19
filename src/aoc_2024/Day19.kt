package aoc_2024

import aoc_util.readInput2024
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

fun main() {
    val testInput = readInput2024("Day19_test")
    val (testTokens, testSamples) = parseInput(testInput)
    val testTokenPool = TokenPool(testTokens)
    val possibleTestSamples = countPossibleSamples(testSamples, testTokenPool)
    println("Test result: $possibleTestSamples")
    val testCombinations = countPossibleCombinations(testSamples, testTokenPool)
    println("Test combinations: $testCombinations")

    val input = readInput2024("Day19")
    val (tokens, samples) = parseInput(input)
    val tokenPool = TokenPool(tokens)
    val possibleSamples = countPossibleSamples(samples, tokenPool)
    println("Result: $possibleSamples")
    val combinations = countPossibleCombinations(samples, tokenPool)
    println("Combinations: $combinations")
}

private fun countPossibleCombinations(samples: List<String>, tokenPool: TokenPool): Long {
    var out = 0L
    val tokensAsSet = tokenPool.availableTokens.toSet()
    val alreadyCountCombinations = mutableMapOf<String, Long>()
    for (sample in samples) {
        if (samplePossibleRegex(sample, tokenPool)) {
            val countCombinations = countCombinations(sample, tokensAsSet, tokenPool, alreadyCountCombinations)
            out += countCombinations
        }
    }
    return out
}

private fun countCombinations(
    sample: String,
    remainingTokens: Set<String>,
    tokenPool: TokenPool,
    alreadyCounted: MutableMap<String, Long>
): Long {
    if (sample.isBlank()) {
        return 1
    }
    val newRemainingTokens: MutableSet<String> = remainingTokens.asSequence().filter { sample.contains(it) }.toMutableSet()
    if (newRemainingTokens.isEmpty()) {
        return 0
    }

    var combinations = 0L
    for (token in newRemainingTokens) {
        if (sample.startsWith(token)) {
            val subSample = sample.substring(token.length..<sample.length)
            if (samplePossibleRegex(subSample, tokenPool)) {
                combinations += alreadyCounted.getOrPut(subSample) { countCombinations(subSample, newRemainingTokens, tokenPool, alreadyCounted) }
            }
        }
    }
    return combinations
}

private fun countPossibleSamples(samples: List<String>, tokenPool: TokenPool): Int {
    var out = 0
    for (sample in samples) {
        val samplePossible = samplePossibleRegex(sample, tokenPool)
        if (samplePossible) {
            out += 1
        }
    }
    return out
}

private fun samplePossibleRegex(
    sample: String,
    tokenPool: TokenPool
): Boolean {
    val tokens = tokenPool.availableTokens.filter { sample.contains(it) }
    val regex = "(?:${tokens.joinToString(")*|(?:", "(?:", ")*")})*".toRegex()

    val result = regex.matchEntire(sample)
    return result != null
}

private fun samplePossibleFast(
    sample: String,
    tokenPool: TokenPool,
    remainingTokens: Set<String> = tokenPool.availableTokens.toSet(),
    maxKeySize: Int = 4
): Boolean {
    if (sample.isBlank()) {
        return true
    }
    val newRemainingTokens: MutableSet<String> = remainingTokens.asSequence().filter { sample.contains(it) }.toMutableSet()
    if (newRemainingTokens.isEmpty()) {
        return false
    }

    val keyList = determineKeyList(sample, maxKeySize)

    for (key in keyList) {
        val possibleTokens = tokenPool.tokenMap[key] ?: emptyList()
        for (token in possibleTokens) {
            if (!newRemainingTokens.contains(token)) {
                continue
            }

            // split the string at the current token and search recursively all remaining substrings
            val remainingFragments: List<String> = sample.split(token.toRegex())
            val possibleFragments: MutableSet<Int> = PrimitiveIntSetB()
            for (i in remainingFragments.indices) {
                val currentFragment = remainingFragments[i]
                val fragmentPossible = samplePossibleFast(currentFragment, tokenPool, newRemainingTokens, maxKeySize)
                if (fragmentPossible) {
                    possibleFragments.add(i)
                }
            }

            // if all fragments are possible, we win
            if (possibleFragments.containsAll(remainingFragments.indices.toList())) {
                return true
            }

            // TODO: if fragments are impossible, we need to fuse some of them with the current token and check the bigger one. If that is impossible
            // TODO: the whole thing is impossible. The selection for the fusion is the tricky part
            // TODO: we need to remove the currently processed token from the newRemainingTokens before we call recursively

        }
    }
    return false

}

private fun samplePossible(
    sample: String,
    tokenPool: TokenPool,
    remainingTokens: Set<String> = tokenPool.availableTokens.toSet(),
    maxKeySize: Int = 4
): Boolean {
    if (sample.isBlank()) {
        return true
    }
    val newRemainingTokens: MutableSet<String> = remainingTokens.asSequence().filter { sample.contains(it) }.toMutableSet()
    if (newRemainingTokens.isEmpty()) {
        return false
    }

    val keyList = determineKeyList(sample, maxKeySize)

    for (key in keyList) {
        val possibleTokens = tokenPool.tokenMap[key] ?: emptyList()
        for (token in possibleTokens) {
            if (!newRemainingTokens.contains(token)) {
                continue
            }
            if (sample.startsWith(token)) {
                val restPossible = samplePossible(sample.substring(token.length..<sample.length), tokenPool, newRemainingTokens, maxKeySize)
                if (!restPossible) {
                    continue
                }
                return true
            }
        }
    }
    return false
}

private fun determineKeyList(sample: String, maxKeySize: Int): MutableList<String> {
    val keyList = mutableListOf<String>()
    var currentKeySize = maxKeySize
    while (currentKeySize > 0) {
        if (sample.length >= currentKeySize) {
            keyList.add(sample.substring(0..<currentKeySize))
        }
        currentKeySize -= 1
    }
    return keyList
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