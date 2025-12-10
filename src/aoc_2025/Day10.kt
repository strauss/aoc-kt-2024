package aoc_2025

import aoc_util.extractInts
import aoc_util.readInput2025
import aoc_util.solve
import java.util.*
import kotlin.math.abs

fun main() {
    val testLines = readInput2025("Day10_test")
    val testInput = parseInput(testLines)
    solve("Test Result", testInput, ::totalFewestPresses)
    solve("Test 2 Result", testInput, ::totalFewestJoltagePresses)

    val lines = readInput2025("Day10")
    val input = parseInput(lines)
    solve("Result", input, ::totalFewestPresses)
    solve("Result 2", input, ::totalFewestJoltagePresses)
}

private fun totalFewestJoltagePresses(confs: List<Configuration>): Long {
    var result = 0L
    confs.forEach {
        result += fewestJoltagePresses(it)
    }
    return result
}

private fun totalFewestPresses(confs: List<Configuration>): Int {
    var result = 0
    confs.forEach {
        result += fewestPresses(it)
    }
    return result
}

private fun fewestJoltagePresses(conf: Configuration): Int {
    val goal = conf.joltages.toIntArray()
    val buttons = conf.buttons
    val initial = IntArray(goal.size) // all 0

    class SearchState(val payload: IntArray, val estimation: Int)

    val initialState = SearchState(initial, goal.manhattanDistance(initial))
    val visited = HashSet<List<Int>>()
    val depth = HashMap<IntArray, Int>()
    var found: IntArray? = null


    val q = PriorityQueue<SearchState>(compareBy { it.estimation }) // Maybe PQueue and A* is better
    visited.add(initial.toList())
    depth[initial] = 0
    q.offer(initialState)
    while (q.isNotEmpty() && found == null) {
        val currentState = q.poll()
        val current = currentState.payload
        for (button in buttons) {
            // calculate next
            val next = IntArray(current.size)
            // copy
            System.arraycopy(current, 0, next, 0, current.size)
            for (pos in button) {
                next[pos] += 1
            }

            // we cut those that will never lead us to the goal
            val nextAsList = next.toList()
            if (!visited.contains(nextAsList) && next.allSmallerOrEquals(goal)) {
                visited.add(nextAsList)
                val currentDepth = depth[current] ?: 0
                val nextDepth = currentDepth + 1
                depth[next] = nextDepth
                val nextState = SearchState(next, goal.manhattanDistance(next))
                q.offer(nextState)
                if (goal.contentEquals(next)) {
                    found = next
                }
            }
        }
    }
    return depth[found] ?: -1
}

private fun IntArray.manhattanDistance(other: IntArray): Int {
    if (size != other.size) {
        return Integer.MAX_VALUE
    }
    var out = 0
    for (idx in 0..<size) {
        out += abs(this[idx] - other[idx])
    }
    return out
}

private fun IntArray.allSmallerOrEquals(other: IntArray): Boolean {
    if (size != other.size) {
        return false
    }
    for (idx in 0..<size) {
        if (this[idx] > other[idx]) {
            return false
        }
    }
    return true
}

private fun fewestPresses(conf: Configuration): Int {
    val goal = conf.indicator
    val buttons = conf.buttons
    val initial = BitSet() // all off
    val visited = HashSet<BitSet>()
    val depth = HashMap<BitSet, Int>()
    var done = false
    val q: Queue<BitSet> = LinkedList()
    visited.add(initial)
    depth[initial] = 0
    q.offer(initial)
    while (q.isNotEmpty() && !done) {
        val current = q.poll()
        for (button in buttons) {
            // calculate next
            val next = BitSet()
            next.or(current) // copy
            for (pos in button) {
                next.flip(pos)
            }

            if (!visited.contains(next)) {
                visited.add(next)
                val currentDepth = depth[current] ?: 0
                depth[next] = currentDepth + 1
                q.offer(next)
                if (next == goal) {
                    done = true
                }
            }
        }
    }
    return depth[goal] ?: -1
}

private fun fewestJoltagePresses2(conf: Configuration): Int {

    TODO()
}

data class Configuration(val indicator: BitSet, val buttons: List<List<Int>>, val joltages: List<Int>)

private fun parseInput(lines: List<String>): List<Configuration> {
    val out = ArrayList<Configuration>()
    for (line in lines) {
        val tokens: List<String> = line.split(" ")
        val indicator = BitSet()
        val indicatorString = tokens.first().replace("[", "").replace("]", "")
        for (idx in indicatorString.indices) {
            val c = indicatorString[idx]
            if (c == '#') {
                indicator.set(idx)
            }
        }

        val buttons: MutableList<List<Int>> = ArrayList()
        for (idx in 1..tokens.size - 2) {
            val currentButtons = tokens[idx].replace("(", "").replace(")", "")
            buttons.add(currentButtons.extractInts())
        }

        val joltageString = tokens.last().replace("{", "").replace("}", "")
        val joltages = joltageString.extractInts()

        out.add(Configuration(indicator, buttons, joltages))

    }
    return out
}