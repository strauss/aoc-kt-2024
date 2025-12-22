package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day12_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::solveWithRedditSolution)
    solve("Test 2 result", testInput.unfoldInput(), ::solveWithRedditSolution)

    val lines = readInput2023("Day12")
    val input = parseInput(lines)
    solve("Result", input, ::solveWithRedditSolution)
    solve("Result 2", input.unfoldInput(), ::solveWithRedditSolution)

}

private fun solveWithRedditSolution(testInput: List<Pair<String, List<Int>>>): Long {
    var result = 0L
    for (singleInput in testInput) {
        val (pattern, distribution) = singleInput
        result += solveSingle(pattern, distribution)
    }
    return result
}

private fun solveSingle(pattern: String, distribution: List<Int>): Long {
    val patternToUse = pattern //.normalize()
    val distributionToUse = distribution.plus(0)
    var position = 0
    val workList = ArrayList<State>()
    val startState = State(-1, '.', 0, 0, 1)
    workList.add(startState)

    fun getMatch(group: Int): State? {
        for (state in workList) {
            if (state.position == position && state.group == group && state.amount == 0) {
                return state
            }
        }
        return null
    }

    fun handleHash(currentState: State) {
        val currentGroup = currentState.group
        val currentAmount = currentState.amount
        val currentPermutations = currentState.permutations
        val nextAmount = currentAmount + 1
        if (nextAmount <= distributionToUse[currentGroup]) {
            val nextState = State(position, '#', currentGroup, nextAmount, currentPermutations)
            workList.add(nextState)
        }
    }

    fun handleDot(currentState: State) {
        val previousSymbol = currentState.symbolToHere
        val currentGroup = currentState.group
        val currentAmount = currentState.amount
        val currentPermutations = currentState.permutations
        if (previousSymbol == '#' && currentAmount == distributionToUse[currentGroup]) {
            // group change
            val nextGroup = currentGroup + 1
            val matchingState = getMatch(nextGroup)
            if (matchingState != null) {
                matchingState.permutations += currentPermutations
            } else {
                val singleNextState = State(position, '.', nextGroup, 0, currentPermutations)
                workList.add(singleNextState)
            }
        } else if (previousSymbol == '.') {
            val matchingState = getMatch(currentGroup)
            if (matchingState != null) {
                matchingState.permutations += currentPermutations
            } else {
                val singleNextState = State(position, '.', currentGroup, currentAmount, currentPermutations)
                workList.add(singleNextState)
            }
        }
    }

    while (workList.isNotEmpty()) {
        if (workList[0].position == position) {
            position += 1
            if (position > patternToUse.lastIndex) {
                break
            }
        }
        val currentState = workList.removeFirst()
        val currentSymbol = patternToUse[position]
        when (currentSymbol) {
            '.' -> handleDot(currentState)
            '#' -> handleHash(currentState)
            '?' -> {
                handleDot(currentState)
                handleHash(currentState)
            }
        }
    }
    workList.removeAll { it.group < distributionToUse.lastIndex - 1 }
    workList.removeAll { it.amount != distributionToUse[it.group] }
    return workList.asSequence().map { it.permutations }.sum()
}

private val splitString = "\\.+".toRegex()

private fun String.normalize(): String {
    val splitted = splitString.split(this.trim('.'))
    return splitted.joinToString(".")
}

private data class State(
    val position: Int,
    val symbolToHere: Char,
    val group: Int,
    val amount: Int,
    var permutations: Long
)

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