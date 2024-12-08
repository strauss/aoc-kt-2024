package aoc_2024

import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import java.util.*

fun main() {
    val testList = readInput2024("Day05_test")
    val (testRules, testSamples) = parseInput(testList)
    val testResult = part1(testRules, testSamples)
    println("Test result: $testResult")

    val realList = readInput2024("Day05")
    val (rules, samples) = parseInput(realList)
    val result = part1(rules, samples)
    println("Real result: $result")

}

private fun part1(rules: List<Pair<Int, Int>>, samples: List<List<Int>>): Pair<Int, Int> {
    val allowed: MutableMap<Int, MutableSet<Int>> = HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<MutableSet<Int>>().create()
    val forbidden: MutableMap<Int, MutableSet<Int>> = HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<MutableSet<Int>>().create()
    rules.forEach {
        if (!allowed.containsKey(it.first)) {
            allowed[it.first] = PrimitiveIntSetB()
        }
        if (!forbidden.containsKey(it.second)) {
            forbidden[it.second] = PrimitiveIntSetB()
        }
        allowed[it.first]?.add(it.second)
        forbidden[it.second]?.add(it.first)
    }
    var result = 0
    val invalid: MutableList<List<Int>> = ArrayList()
    samples.forEach { sample ->
        if (valid(sample, forbidden)) {
            val middle = sample[sample.size / 2]
            result += middle
        } else {
            invalid.add(sample)
        }
    }
    val invalidResult = handleInvalid(rules, invalid, allowed, forbidden)
    return Pair(result, invalidResult)
}

private fun handleInvalid(rules: List<Pair<Int, Int>>, invalid: List<List<Int>>, allowed: Map<Int, Set<Int>>, forbidden: Map<Int, Set<Int>>): Int {
    var result = 0
    invalid.forEach { sample ->
        val sortedSample: MutableList<Int> = PrimitiveIntArrayList()

        for (i in sample.indices) {
            sortedSample.add(sample[i])
            for (j in sortedSample.indices) {
                val k = sortedSample.size - 1 - j
                if (k <= 0 || valid(sortedSample, forbidden)) {
                    break
                }
                Collections.swap(sortedSample, k, k - 1)
            }
        }
        result += sortedSample[sample.size / 2]
    }
    return result
}

private fun valid(sample: List<Int>, forbidden: Map<Int, Set<Int>>): Boolean {
    for (i in sample.indices) {
        val (soFar: List<Int>, remaining: List<Int>) = splitAtPosition(i, sample)
        soFar.size
        if (elementInvalid(remaining, forbidden, sample, i)) return false
    }
    return true
}

private fun elementInvalid(
    remaining: List<Int>,
    forbidden: Map<Int, Set<Int>>,
    sample: List<Int>,
    i: Int
): Boolean {
    for (element in remaining) {
        if (forbidden[sample[i]]?.contains(element) == true) {
            return true
        }
    }
    return false
}

private fun splitAtPosition(
    i: Int,
    sample: List<Int>
): Pair<List<Int>, List<Int>> {
    val soFar: List<Int> = if (i > 0) {
        sample.subList(0, i + 1)
    } else {
        emptyList()
    }
    val remaining: List<Int> = if (i < sample.size - 1) {
        sample.subList(i + 1, sample.size)
    } else {
        emptyList()
    }
    return Pair(soFar, remaining)
}

private fun parseInput(input: List<String>): Pair<List<Pair<Int, Int>>, List<List<Int>>> {
    val ruleList: MutableList<Pair<Int, Int>> = ArrayList()
    val sampleList: MutableList<List<Int>> = ArrayList()
    input.forEach { line ->
        if (line.contains('|')) {
            val split = line.split('|')
            ruleList.add(Pair(split[0].toInt(), split[1].toInt()))
        } else if (line.contains(',')) {
            val sample = line.split(',')
            val sampleAsList: MutableList<Int> = PrimitiveIntArrayList()
            sample.forEach { sampleAsList.add(it.toInt()) }
            sampleList.add(sampleAsList)
        }
    }
    return Pair(ruleList, sampleList)
}