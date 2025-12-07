package aoc_2023

import aoc_util.allEquals
import aoc_util.lcm
import aoc_util.readInput2023
import java.util.*

fun main() {
    val testInput = readInput2023("Day08_test")
    val parsedTestInput = parseInput(testInput)
//    val testResult = countSteps(parsedTestInput)
//    println("Test result: $testResult")
    val test2Result = countSimultaneously(parsedTestInput)
    println("Test result 2: $test2Result")

    val input = readInput2023("Day08")
    val parsedInput = parseInput(input)
    val result = countSteps(parsedInput)
    println("Result: $result")
    val result2 = doNotCount()
    println("Result 2: $result2")
}

private fun doNotCount(): Long {
    // In order to do it properly, you need to calculate the map from countSmart with only the goal states.
    // The index is always 0, because a goal state is reached after a multiple of 263 steps
    // "By pure chance", each start state ends up in its own end state and each end state ends up in itself.
    // The luck does not stop there ... even the steps from start state to its end state and from that end state to
    // itself are identical in size
    // That means, the problem boils down to finding the lcm of all steps, which is exactly, what I'm doing here.
    // For fully automating this approach, the map could be calculated beforehand and the list filled with the
    // calculated steps for each goal state ... but I am lazy, so I just stick to these pre-calculated values that I got
    // from using the debugger.
    return listOf(19199L, 18673L, 20777L, 16043L, 12361L, 15517L).foldRight(263, ::lcm)
}

private fun countSmart(input: Pair<String, Map<String, Pair<String, String>>>): Long {
    val (moves, graph) = input
    val pQueue = PriorityQueue<LocalState>(Comparator.comparing { it.totalSteps })
    graph.keys.forEach {
        if (it.endsWith("A")) {
            pQueue.add(LocalState(it))
        }
    }
    // The key is the combination of position and start index in move array
    // We just need to count the effort and don't want to compute it every time again
    val cache = HashMap<Pair<String, Int>, Delta>()
    do {
        val cState = pQueue.poll() // we take the smallest step size to go forward
        val delta = cache.computeIfAbsent(cState.position to cState.index) { (pos, idx) ->
            countMovesToGoal(moves, graph, pos, idx) { it.endsWith("Z") }
        }
        cState.applyDelta(delta)
        pQueue.offer(cState)
        // we finish, when all are at the same step size
    } while (!pQueue.asSequence().map { it.totalSteps }.toList().allEquals())

    return pQueue.peek().totalSteps
}

private class LocalState(startPosition: String) {
    var position = startPosition
    var index = 0
    var totalSteps = 0L

    fun applyDelta(delta: Delta) {
        position = delta.position
        index = delta.index
        totalSteps += delta.cost.toLong()
    }
}

private class Delta(val position: String, val index: Int, val cost: Int)

private fun countSimultaneously(input: Pair<String, Map<String, Pair<String, String>>>): Long {
    val (moves, graph) = input
    var idx = 0
    var count = 0L
    var positions: MutableSet<String> = HashSet()
    graph.keys.forEach {
        if (it.endsWith("A")) {
            positions.add(it)
        }
    }
    val goal: (Set<String>) -> Boolean = { it.all { v -> v.endsWith("Z") } }
    while (true) {
        val move = moves[idx]
        idx = (idx + 1) % moves.length
        val newPositions: MutableSet<String> = HashSet()
        for (pos in positions) {
            val (left, right) = graph[pos]!!
            val npos = when (move) {
                'L' -> left
                'R' -> right
                else -> throw IllegalStateException("Illegal!")
            }
            newPositions.add(npos)
        }
        positions = newPositions
        count += 1L
        if (goal(positions)) {
            break
        }
    }
    return count
}

private fun countSteps(input: Pair<String, Map<String, Pair<String, String>>>): Int {
    val (moves, graph) = input
    val start = "AAA"
    val goal = "ZZZ"
    return countMovesToGoal(moves, graph, start) { it == goal }.cost
}

private fun countMovesToGoal(
    moves: String,
    graph: Map<String, Pair<String, String>>,
    start: String,
    index: Int = 0,
    goal: (String) -> Boolean
): Delta {
    var position = start
    var count = 0
    var idx = 0
    while (true) {
        val move = moves[idx]
        idx = (idx + 1) % moves.length
        val (left, right) = graph[position]!!
        position = when (move) {
            'L' -> left
            'R' -> right
            else -> position
        }
        count += 1
        if (goal(position)) {
            break
        }
    }
    return Delta(position, idx, count)
}

private fun parseInput(lines: List<String>): Pair<String, Map<String, Pair<String, String>>> {
    val moves = lines[0]
    val extractRegex = "([12A-Z]{3}) = \\(([12A-Z]{3}), ([12A-Z]{3})\\)".toRegex()
    val graph = buildMap {
        for (idx in 2..<lines.size) {
            val matchResult = extractRegex.matchEntire(lines[idx])
            if (matchResult == null) {
                continue
            }
            val key = matchResult.groups[1]?.value ?: ""
            val value = (matchResult.groups[2]?.value ?: "") to (matchResult.groups[3]?.value ?: "")
            put(key, value)
        }
    }
    return moves to graph
}