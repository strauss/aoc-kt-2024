package aoc_2024

import aoc_util.readInput2024

fun main() {
    val testInput = readInput2024("Day25_test")
    val (testLocks, testKeys) = getLocksAndKeys(testInput)
    val testResult = countMatches(testLocks, testKeys)
    println("Test result: $testResult")

    val input = readInput2024("Day25")
    val (locks, keys) = getLocksAndKeys(input)
    val result = countMatches(locks, keys)
    println("Result: $result")
}

private fun countMatches(locks: Set<List<Int>>, keys: Set<List<Int>>): Int {
    var matches = 0
    println(locks.size * keys.size)
    for (lock in locks) {
        for (key in keys) {
            if (fit(lock, key)) {
                matches += 1
            }
        }
    }
    return matches
}

private fun fit(lock: List<Int>, key: List<Int>): Boolean {
    for (i in lock.indices) {
        if (lock[i] + key[i] > 5) {
            return false
        }
    }
    return true
}

private fun getLocksAndKeys(input: List<String>): Pair<Set<List<Int>>, Set<List<Int>>> {
    val locks = mutableSetOf<List<Int>>()
    val keys = mutableSetOf<List<Int>>()

    var parseState = ParseState.NOTHING
    var currentEntry = mutableListOf(0, 0, 0, 0, 0)
    for (line in input) {
        if (line.isBlank()) {
            when (parseState) {
                ParseState.LOCK -> {
                    locks.add(currentEntry)
                }

                ParseState.KEY -> {
                    for (i in currentEntry.indices) {
                        currentEntry[i] -= 1
                    }
                    keys.add(currentEntry)
                }

                else -> {
                    // nothing
                }
            }
            parseState = ParseState.NOTHING
            currentEntry = mutableListOf(0, 0, 0, 0, 0)
            continue
        }
        if (parseState == ParseState.NOTHING) {
            parseState = if (line.startsWith(".")) {
                ParseState.KEY
            } else {
                ParseState.LOCK
            }
            continue
        }

        for (i in line.indices) {
            if (line[i] == '#') {
                currentEntry[i] += 1
            }
        }

    }
    when (parseState) {
        ParseState.LOCK -> {
            locks.add(currentEntry)
        }

        ParseState.KEY -> {
            for (i in currentEntry.indices) {
                currentEntry[i] -= 1
            }
            keys.add(currentEntry)
        }

        else -> {
            // nothing
        }
    }


    return Pair(locks, keys)
}

private enum class ParseState {
    NOTHING, LOCK, KEY
}