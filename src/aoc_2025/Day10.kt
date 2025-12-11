package aoc_2025

import aoc_util.extractInts
import aoc_util.readInput2025
import aoc_util.solve
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

fun main() {
    val testLines = readInput2025("Day10_test")
    val testInput = parseInput(testLines)
    solve("Test Result", testInput, ::totalFewestPresses)

    val nTestInput = testInput.map { simplifyConfiguration(it) }
//    showInputs(testInput, nTestInput)
    solve("Test 2 Result", nTestInput, ::totalFewestJoltagePresses)

    val lines = readInput2025("Day10")
    val input = parseInput(lines)
    solve("Result", input, ::totalFewestPresses)

    val nInput = input.map { simplifyConfiguration(it) }
    val nsInput = nInput.sortedBy { it.joltages.size }
//    for (i in nsInput.indices) {
//        val current = nsInput[i]
//        if (current.buttons.size < current.joltages.size) {
//            println("$i: $current")
//        }
//    }

    solve("Result 2", nsInput, ::totalFewestJoltagePresses)
}

private fun showInputs(
    testInput: List<Configuration>,
    nTestInput: List<Configuration>
) {
    for (i in testInput.indices) {
        println("Old: ${testInput[i]}")
        println("New: ${nTestInput[i]}")
        println()
    }
}

private fun simplifyConfiguration(conf: Configuration): Configuration {
    val buttons = conf.buttons
    val joltages = conf.joltages
    // remove viable duplicate entries
    val removePositions = PrimitiveIntSetB()
    for (j in joltages.indices) {
        if (removePositions.contains(j)) {
            continue
        }
        val current = joltages[j]
        for (i in j + 1..<joltages.size) {
            if (removePositions.contains(i)) {
                continue
            }
            val com = joltages[i]
            if (current == com && removalAllowed(buttons, j, i)) {
                removePositions.add(i)
            }
        }
    }
    val sRem = removePositions.asSequence().sortedDescending().toList()
    val newButtons = ArrayList<List<Int>>()
    val newJoltages = ArrayList<Int>()
    for (idx in joltages.indices) {
        if (sRem.contains(idx)) {
            continue
        }
        newJoltages.add(joltages[idx])
    }
    for (button in buttons) {
        var newButton = button
        for (rem in sRem) {
            val wButton = ArrayList<Int>()
            for (n in newButton) {
                if (n == rem) {
                    continue
                }
                if (n > rem) {
                    wButton.add(n - 1)
                } else {
                    wButton.add(n)
                }
            }
            newButton = wButton
        }
        newButtons.add(newButton)
    }
    val comparator = Comparator.comparing<List<Int>, Int> { it.size }
    newButtons.sortWith(comparator)
    return Configuration(conf.indicator, newButtons, newJoltages)
}

private fun removalAllowed(buttons: List<List<Int>>, j: Int, i: Int): Boolean {
    for (button in buttons) {
        val containsI = button.contains(i)
        val containsJ = button.contains(j)
        if (containsI != containsJ) {
            // if it is contained in only one of them, the removal is not allowed
            // in both or in none is okay
            return false
        }
    }
    return true
}

private fun totalFewestJoltagePresses(confs: List<Configuration>): Long {
    var result = 0L
    var idx = 0
    confs.forEach {
        result += fewestJoltagePressesAStar(it)
        println(idx)
        idx += 1
    }
    println()
    return result
}

private fun totalFewestPresses(confs: List<Configuration>): Int {
    var result = 0
    confs.forEach {
        result += fewestPresses(it)
    }
    return result
}

private fun fewestJoltagePressesAStar(conf: Configuration): Int {
    val goal = conf.joltages.toIntArray()
    val buttons = conf.buttons
    val maxOnes = buttons.maxOfOrNull { it.size } ?: 0
    val initial = IntArray(goal.size) // all 0

    fun heuristic(array: IntArray): Int = max(
        goal.maxMissingPerComponent(array),
        ceil(goal.missing(array).toDouble() / maxOnes).toInt()
    )

    class SearchState(val payload: IntArray, val depth: Int, val estimation: Int)

    val initialState = SearchState(initial, 0, heuristic(initial))
    val visited = HashMap<List<Int>, Int>()
    var found: SearchState? = null


    val q = PriorityQueue<SearchState>(compareBy { it.estimation + it.depth }) // Maybe PQueue and A* is better
    visited.put(initial.toList(), 0)
    q.offer(initialState)
    while (q.isNotEmpty() && found == null) {
        val currentState = q.poll()
        val current = currentState.payload
        for (button in buttons) {
            val next = calculateNext(current, button)

            // we cut those that will never lead us to the goal
            val nextAsList = next.toList()
            val nextDepth = currentState.depth + 1
            val previouslyVisitedCost: Int = visited[nextAsList] ?: Integer.MAX_VALUE
            if (previouslyVisitedCost < nextDepth) {
                visited.remove(nextAsList)
            } else if (previouslyVisitedCost > nextDepth && next.allSmallerOrEquals(goal)) {
                visited.put(nextAsList, nextDepth)
                val nextState = SearchState(next, nextDepth, heuristic(next))
                q.offer(nextState)
                if (goal.contentEquals(next)) {
                    found = nextState
                }
            }
        }
    }
    return found?.depth ?: -1
}

private fun calculateNext(current: IntArray, button: List<Int>): IntArray {
    // calculate next
    val next = IntArray(current.size)
    // copy
    System.arraycopy(current, 0, next, 0, current.size)
    for (pos in button) {
        next[pos] += 1
    }
    return next
}

private fun IntArray.missing(other: IntArray): Int {
    if (size != other.size) {
        return 0
    }
    var out = 0
    for (idx in 0..<size) {
        out += abs(this[idx] - other[idx])
    }
    return out
}

private fun IntArray.maxMissingPerComponent(other: IntArray): Int {
    if (size != other.size) {
        return 0
    }
    var out = 0
    for (idx in 0..<size) {
        out = out.coerceAtLeast(abs(this[idx] - other[idx]))
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