package aoc_2023

import aoc_util.*
import java.util.*

fun main() {
    val testLines = readInput2023("Day12_test")
//    solve("Test complexity", testLines, ::analyseComplexity)
    val testInput = parseInput(testLines)
//    solve("Test result", testInput, ::solve1)
//    solve("Test reSult", testInput, ::solve2)

    val lines = readInput2023("Day12")
//    solve("Complexity", lines, ::analyseComplexity)
    val input = parseInput(lines)
//    solve("Result", input, ::solve1)
//    solve("ReSult", input, ::solve2)
    main2()
}

private val cache: MutableMap<Pair<String, List<Int>>, Long> = HashMap()

private fun solve1(input: List<Pair<String, List<Int>>>): Long {
    var out = 0L
    for ((sample, testAgainst) in input) {
        out += countOne(sample.normalize(), testAgainst)
    }
    return out
}

private fun solve2(input: List<Pair<String, List<Int>>>): Long {
    var out = 0L
    for ((sample, testAgainst) in input) {
        val localResult = AnswerCache.combinations(sample, testAgainst)
//        println("Result for '$sample' and '$testAgainst': $localResult")
        out += localResult
    }
    return out
}

private fun detectErrors(input: List<Pair<String, List<Int>>>) {
    for ((sample, testAgainst) in input) {
        val slowResult = countOne(sample, testAgainst)
        val fastResult = AnswerCache.combinations(sample, testAgainst)
        if (slowResult != fastResult) {
            println("Error detected for '$sample' and '$testAgainst'. Expected result was $slowResult, but we got $fastResult")
        }
    }
}

private fun countOne(sample: String, testAgainst: List<Int>): Long {
    val unknownCount = countUnknown(sample)
    return internalCount(sample, testAgainst, unknownCount)
}

private fun internalCount(sample: String, testAgainst: List<Int>, maxDepth: Int, currentDepth: Int = 0): Long {
    if (maxDepth == currentDepth) {
        return if (sample.isValid(testAgainst)) 1 else 0
    }

    val key = sample to testAgainst
    val cachedValue = cache[key]

    if (cachedValue != null) {
        return cachedValue
    }

    var currentResult = 0L

    currentResult += internalCount(sample.replaceFirst('?', '.'), testAgainst, maxDepth, currentDepth + 1)
    currentResult += internalCount(sample.replaceFirst('?', '#'), testAgainst, maxDepth, currentDepth + 1)

    cache[key] = currentResult
    return currentResult
}

private fun parseInput(lines: List<String>): List<Pair<String, List<Int>>> {
    val out = ArrayList<Pair<String, List<Int>>>()
    for (line in lines) {
        val split = line.split(" ")
        out.add(split[0] to split[1].split(",").map { it.toInt() })
    }
    return out
}

private fun List<Pair<String, List<Int>>>.unfoldInput(): List<Pair<String, List<Int>>> = map { it.unfold() }

private fun Pair<String, List<Int>>.unfold(): Pair<String, List<Int>> = first.unfold() to second.unfold()

private fun String.unfold(): String = "$this?$this?$this?$this?$this"

private fun List<Int>.unfold(): List<Int> = buildList {
    for (i in 1..5) {
        addAll(this@unfold)
    }
}

private fun analyseComplexity(lines: List<String>): Int {
    return lines.asSequence().map { countUnknown(it) }.max()
}

private fun countUnknown(input: String): Int = input.count { c -> c == '?' }

private val splitPattern = "\\.+".toRegex()

private fun String.isValid(withRespectTo: List<Int>): Boolean {
    val aResult = analyze()
    return withRespectTo == aResult
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

private fun String.analyze(): List<Int> {
    return splitPattern.split(this).asSequence()
        .filter { it.isNotEmpty() }
        .map { it.length }
        .toList()
}

private val splitString = "\\.+".toRegex()

private fun String.normalize(): String {
    val splitted = splitString.split(this.trim('.'))
    return splitted.joinToString(".")
}

private object AnswerCache {
    private val validString = "[?.#]*".toRegex()
    private val simpleString = "[#]+".toRegex()
    private val onlyWildcards = "[?]+".toRegex()
    private val wildcardString = "[?#]+".toRegex()


    private val cache: MutableMap<Pair<String, List<Int>>, Long> = HashMap()

    fun combinations(pattern: String, distribution: List<Int>): Long {
//        println("called with '$pattern' and '$distribution'")
        if (!validString.matches(pattern)) {
            return 0
        }
        val realPattern: String = pattern.trim('.') // we get rid of all outside dots because they are meaningless
        if (distribution.isEmpty() || realPattern.isEmpty()) {
            return 0
        }
        if (distribution.size == 1) {
            return handleSingleValueDistribution(realPattern, distribution)
        }

        // here it gets dirty
        // first we normalize the pattern for creating a decent key
        // This operation shrinks all sequences of "......" to a single "."
        val subPatterns: List<String> = splitString.split(realPattern)
        val normalizedPattern = subPatterns.joinToString(".")

        // now we check, if we already encountered this combination of pattern and distribution
        val key = normalizedPattern to distribution
        val value = cache[key]
        if (value != null) {
//            println("Hit cache for '$key'")
            return value
        }

        // if we didn't, we have to do some serious work

        // If we only have one pattern, but multiple distributions, we have to handle it separately

        if (subPatterns.size == 1) {
            assert(distribution.size > 1)
            val result = handleSmallerSize(subPatterns[0], distribution)
            cache[key] = result
            return result
        }

        // If the first pattern can be easily stripped away, we do just that
        val firstSize = distribution[0]
        val firstPattern = subPatterns[0]
        if (firstPattern.contains('#') && firstPattern.length in firstSize..firstSize + 1) {
            var result = handleSingleValueDistribution(firstPattern, listOf(firstSize))
            if (result > 0) {
                val remainingPattern: String = subPatterns.drop(1).joinToString(".")
                result *= combinations(remainingPattern, distribution.drop(1))
            }
            cache[key] = result
            return result
        } else if (firstPattern.length < firstSize) {
            if ('#' in firstPattern) {
                // the whole pattern is impossible
                cache[key] = 0L
                return 0L
            } else {
                val remainingPattern: String = subPatterns.drop(1).joinToString(".")
                val result = combinations(remainingPattern, distribution)
                cache[key] = result
                return result
            }
        }

        // If the last pattern can be easily stripped away, we do just that
        val lastSize = distribution[distribution.lastIndex]
        val lastPattern = subPatterns[subPatterns.lastIndex]
        if (lastPattern.contains('#') && lastPattern.length in lastSize..lastSize + 1) {
            var result = handleSingleValueDistribution(
                lastPattern, listOf(distribution[distribution.lastIndex])
            )
            if (result > 0) {
                val remainingPattern: String = subPatterns.take(subPatterns.size - 1).joinToString(".")
                result *= combinations(remainingPattern, distribution.take(distribution.size - 1))
            }
            cache[key] = result
            return result
        } else if (lastPattern.length < lastSize) {
            if ('#' in lastPattern) {
                // the whole pattern is impossible
                cache[key] = 0L
                return 0L
            } else {
                val remainingPattern: String = subPatterns.take(subPatterns.size - 1).joinToString(".")
                val result = combinations(remainingPattern, distribution)
                cache[key] = result
                return result
            }
        }

        // TODO: here lies one error. Sometimes they do not need to line up and pure wildcard patterns can be removed for still being viable
        // TODO: In that case, the "handleSmallerSize" logic has to be used

        // If the sub patterns and distributions actually do line up, we try to strip match them
        if (subPatterns.size == distribution.size) {
            val (_, wildcard) = analyzeSubPatterns(subPatterns)
            var noReductionPossible = true
            for (depth in 1..wildcard.size) {
                val iterator = CombinatorialIterator(wildcard, depth, distinct = true)
                iterator.iterate { combination ->
                    if (combination.isSorted()) {
                        val reducedSubPatterns = buildList {
                            for (idx in subPatterns.indices) {
                                if (idx !in combination) {
                                    add(subPatterns[idx])
                                }
                            }
                        }
                        val newPattern = reducedSubPatterns.joinToString(".")
                        if (distribution.minPatternSize() <= newPattern.length) {
                            noReductionPossible = false
                            return@iterate // break
                        }
                    }
                }
                if (!noReductionPossible) {
                    break
                }
            }

            if (noReductionPossible) {
                val result = handleEqualSize(subPatterns, distribution)
                cache[key] = result
                return result
            }
            // we are forced to search the hard way
            return handleSmallerSize(subPatterns.joinToString("."), distribution)
        }

        if (subPatterns.size < distribution.size) {
            var result = 0L
            result += handleSmallerSize(subPatterns.joinToString("."), distribution)
            cache[key] = result
            return result
        }

        assert(subPatterns.size > distribution.size) { "${subPatterns.size} is not greater than ${distribution.size} for '$pattern' and $distribution" }
        val result = handleBiggerSize(subPatterns, distribution)
        cache[key] = result
        return result
    }

    private fun handleBiggerSize(subPatterns: List<String>, distribution: List<Int>): Long {
        val (enforced, wildcard) = analyzeSubPatterns(subPatterns)
        if (enforced.cardinality() > distribution.size) {
            // If wee need more patterns than distribution parts, we are done
            return 0
        }

        // This is probably wrong, but maybe not ... I'm not sure
        if (enforced.cardinality() == distribution.size) {
            // If the enforced patterns are our only option, we go with them
            val enforcedSubPatterns = buildList {
                for (idx in subPatterns.indices) {
                    if (enforced[idx]) {
                        add(subPatterns[idx])
                    }
                }
            }
            val newPattern = enforcedSubPatterns.joinToString(".")
            val result = combinations(newPattern, distribution)
            return result
        }

        val newPatterns = LinkedList<String>()

        // This is also not correct ... maybe we can optimize differently by assigning bigger chunks to their respective bigger distributions
//        for (idx in subPatterns.indices) {
//            val currentSubPattern = subPatterns[idx]
//            if ('#' !in currentSubPattern) {
//                val newPattern = subPatterns.drop(idx + 1).joinToString(".")
//                if (distribution.minPatternSize() <= newPattern.length) {
//                    newPatterns.add(newPattern)
//                } else {
//                    break
//                }
//            } else {
//                break
//            }
//        }
//        for (idx in subPatterns.indices) {
//            val rIdx = subPatterns.lastIndex - idx
//            val currentSubPattern = subPatterns[rIdx]
//            if ('#' !in currentSubPattern) {
//                val newPattern = subPatterns.take(rIdx - 1).joinToString(".")
//                if (distribution.minPatternSize() <= newPattern.length) {
//                    newPatterns.add(newPattern)
//                } else {
//                    break
//                }
//            } else {
//                break
//            }
//        }

        // This is not correct: We are not allowed to leave out in the middle just like that
//        for (depth in 1..wildcard.size) {
//            val iterator = CombinatorialIterator(wildcard, depth, distinct = true)
//            iterator.iterate { combination ->
//                if (combination.isSorted()) {
//                    val reducedSubPatterns = buildList {
//                        for (idx in subPatterns.indices) {
//                            if (idx !in combination) {
//                                add(subPatterns[idx])
//                            }
//                        }
//                    }
//                    val newPattern = reducedSubPatterns.joinToString(".")
//                    if (distribution.minPatternSize() <= newPattern.length) {
//                        newSubPatterns.add(newPattern)
//                    }
//                }
//            }
//        }

        if (newPatterns.isNotEmpty()) {
            var result = 0L
            for (newPattern in newPatterns) {
                val currentResult = combinations(newPattern, distribution)
                result += currentResult
            }
            return result
        }
        return handleSmallerSize(subPatterns.joinToString("."), distribution)
    }

    private fun analyzeSubPatterns(subPatterns: List<String>): Pair<BitSet, HashSet<Int>> {
        val enforced = BitSet()
        val wildcard = HashSet<Int>()
        for (idx in subPatterns.indices) {
            if (subPatterns[idx].contains('#')) {
                enforced.set(idx)
            } else {
                wildcard.add(idx)
            }
        }
        return Pair(enforced, wildcard)
    }

    private fun handleEqualSize(subPatterns: List<String>, distribution: List<Int>): Long {
        var result = 1L
        var unmatchablePatterns = ArrayList<String>()
        var unmatchableDistribution = ArrayList<Int>()
        var anyMatch = false
        for (idx in subPatterns.indices) {
            val pattern = subPatterns[idx]
            val distribution = distribution[idx]
            if (pattern.length in distribution..distribution + 1) {
                anyMatch = true
                result *= handleSingleValueDistribution(pattern, listOf(distribution))
                if (result == 0L) {
                    break
                }
                if (unmatchablePatterns.isNotEmpty()) {
                    val newPattern = unmatchablePatterns.joinToString(".")
                    result *= combinations(newPattern, unmatchableDistribution)
                    unmatchablePatterns = ArrayList()
                    unmatchableDistribution = ArrayList()
                    if (result == 0L) {
                        break
                    }
                }
            } else {
                unmatchablePatterns.add(pattern)
                unmatchableDistribution.add(distribution)
            }
        }
        if (anyMatch) {
            if (unmatchablePatterns.isNotEmpty()) {
                val newPattern = unmatchablePatterns.joinToString(".")
                result *= combinations(newPattern, unmatchableDistribution)
            }
            // if we matched, we also try leaving out an outer wildcard only
            if (onlyWildcards.matches(subPatterns[0])) {
                result += combinations(subPatterns.drop(1).joinToString("."), distribution)
            }
            if (onlyWildcards.matches(subPatterns[subPatterns.lastIndex])) {
                result += combinations(subPatterns.take(subPatterns.size - 1).joinToString("."), distribution)
            }
            return result
        }
        // if we didn't match anything, we go on
        return redistribute(subPatterns, distribution)
    }

    private fun redistribute(subPatterns: List<String>, distribution: List<Int>): Long {
        var result = 0L
        val minLength = subPatterns.size.coerceAtMost(distribution.size)

        for (currentSize in 2..minLength) {
            val subPatternDistributions: List<List<List<String>>> = subPatterns.allSeparations(currentSize)
            val subPatterns: List<List<String>> = buildList {
                for (currentDistribution: List<List<String>> in subPatternDistributions) {
                    val innerList = ArrayList<String>()
                    for (currentPattern in currentDistribution) {
                        innerList.add(currentPattern.joinToString("."))
                    }
                    add(innerList)
                }
            }
            val subDistributions: List<List<List<Int>>> = distribution.allSeparations(currentSize)

            for (currentDistributionSeparation: List<List<Int>> in subDistributions) {
                for (currentSubPattern: List<String> in subPatterns) {
                    var localResult = 1L
                    for (idx in currentSubPattern.indices) {
                        val localPattern = currentSubPattern[idx]
                        val localDistribution = currentDistributionSeparation[idx]
                        val singleResult = combinations(localPattern, localDistribution)
                        localResult *= singleResult
                        if (localResult == 0L) {
                            break
                        }
                    }
                    result += localResult
                }
            }
        }
        return result
    }


    private fun List<Int>.minPatternSize(): Int = size - 1 + sum()

    private fun handleSmallerSize(pattern: String, distribution: List<Int>): Long {
        if (simpleString.matches(pattern) || distribution.minPatternSize() > pattern.length) {
            // if we only have # or the distribution cannot be achieved by the pattern, we are done
            return 0
        }

        // TODO: try splitting up in a way that it is guaranteed to have equals sized subPatterns and distributions
        // TODO: maybe it is required to detect blocks

//        var result = 0L

//        val iterator = CombinatorialIterator(pattern.indices, distribution.size - 1, distinct = true)
//        iterator.iterate { positions ->
//            if (positions.isSorted() && positions.all { pattern[it] == '?' }) {
//                val newPattern = pattern.changePositions('.', *positions.toIntArray())
//                if (splitPattern.split(newPattern).size == distribution.size) {
//                    result += combinations(newPattern, distribution)
//                }
//
//            }
//        }

//        for (idx in pattern.indices) {
//            if (pattern[idx] == '?') {
//                val newDotPattern = pattern.changePosition('.', idx)
//                result += combinations(newDotPattern, distribution)
//            }
//        }
//        return result

        // countOne is the fallback to hard searching and should only be the last ressort
        return countOne(pattern, distribution)
    }

    private fun handleSingleValueDistribution(pattern: String, distribution: List<Int>): Long {
        if (!wildcardString.matches(pattern)) {
            // as first step, we split them into their sub patterns
            val subPatterns = splitString.split(pattern)
            assert(subPatterns.size > 1)

            val (wildcardOnlyPatterns, mixedPatterns) =
                subPatterns.partition { onlyWildcards.matches(it) }

            // border case: they are all wildcard only
            // TODO: maybe we want to cache this
            if (mixedPatterns.isEmpty()) {
                var result = 0L
                for (subPattern in wildcardOnlyPatterns) {
                    result += combinations(subPattern, distribution)
                }
                return result
            }
            if (mixedPatterns.size > 1) {
                return 0 // if we have multiple mixed patterns but only one distribution value, we are done
            }
            return combinations(mixedPatterns[0], distribution)
        }
        val onlyDistributionValue = distribution[0]
        // here, our string matches the wildcard string, meaning we only have # and ?
        if (pattern.length < onlyDistributionValue) {
            // if our sample is smaller than the distribution value, we are done
            return 0
        }
        if (simpleString.matches(pattern)) {
            // if our pattern only consists of "#", we just need to match the numbers
            return if (pattern.length == onlyDistributionValue) 1 else 0
        }
        assert(pattern.length >= onlyDistributionValue)
        // now we have to remove the first and the last ? and call this function recursively
        val key = pattern to distribution
        val value = cache[key]
        if (value != null) {
//            println("Hit cache for '$key'")
            return value
        }

        var result = 0L
        val firstIndex = pattern.indexOf('?')
        result += combinations(pattern.changePosition('#', firstIndex), distribution)
        result += combinations(pattern.changePosition('.', firstIndex), distribution)

        cache[key] = result
        return result
    }

}

private fun main2() {
    // Error detected for '?#??.???.?.?.????' and '[4, 1, 1, 3]'. Expected result was 16, but we got 14
    val pattern = "????.######..#####.".unfold()
    val distribution = listOf(1, 6, 5).unfold()
    println(pattern)
    println(distribution)
    val result = AnswerCache.combinations(pattern, distribution)
    println("My result     : $result")
    val correctResult = countOne(pattern, distribution)
    println("Correct result: $correctResult")
}