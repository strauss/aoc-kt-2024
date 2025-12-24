package aoc_2023

import aoc_util.Coordinate
import aoc_util.Primitive2DCharArray
import aoc_util.readInput2023
import aoc_util.solve
import java.util.*

fun main() {
    val testLines = readInput2023("Day17_test")
    val testInput = Primitive2DCharArray.parseFromLines(testLines)
    solve("Test result", testInput, ::searchOptimalHeatLoss)

    val lines = readInput2023("Day17")
    val input = Primitive2DCharArray.parseFromLines(lines)
//    solve("Result", input, ::searchOptimalHeatLoss)

}

// TODO: write an alternative solution
/*
 * - BFS from starting point with allowed operations in mind (this is the hard part)
 * - Create the graph while searching
 * - vertices with "row, col, dir"
 * - edges with "op, cost"
 */

private fun searchOptimalHeatLoss(
    array: Primitive2DCharArray,
    start: LavaSearchState = LavaSearchState(0, 0, 0, Direction.EAST),
    goal: Coordinate = Coordinate(array.height - 1, array.width - 1)
): Int {
    // we perform a backwards Dijkstra for calculating the heuristic for a follow-up A*
    val backwardsStart = LavaSearchState(goal.row, goal.col, 0, Direction.NORTH)
    val heuristic = performDijkstra(array, backwardsStart)

    // now we do A* with the precalculated heuristic
    val marker = HashSet<LavaSearchState>()
    val buffer = PriorityQueue<Pair<LavaSearchState, Int>>(Comparator.comparing { it.second })
    val cost = HashMap<Coordinate, Int>()
    val parent = HashMap<LavaSearchState, LavaSearchState>()

    // prepare start state
    val startCoordinate = Coordinate(start.row, start.col)
    cost[startCoordinate] = 0
    buffer.offer(start to (heuristic[startCoordinate] ?: 0))

    // prepare target found
    var target: LavaSearchState? = null

    while (buffer.isNotEmpty()) {
        val (currentState, _) = buffer.poll()
        val currentCoordinate = Coordinate(currentState.row, currentState.col)
        if (currentState in marker) {
            continue
        }
        marker.add(currentState)
        // check if we reached the goal
        if (goal == currentCoordinate) {
            target = currentState
            break
        }

        // follow-up states
        val currentDir = currentState.dir
        val currentStraight = currentState.straight
        val costSoFar = cost[currentCoordinate]!! // if this is null, the algorithm is broken

        for (op: Operation in Operation.entries) {
            val (nextDir, nextStraight) = nextDirAndStraight(currentDir, currentStraight, op)
            val (dRow, dCol) = nextDir.delta
            val nextRow = currentState.row + dRow
            val nextCol = currentState.col + dCol
            if (nextStraight <= 3 &&
                nextRow in 0..<array.height && nextCol in 0..<array.width
            ) { // only if next state is in bounds
                val costKey = Coordinate(nextRow, nextCol)
                val nextCost = costSoFar + array[nextRow, nextCol].digitToInt()
                if (nextCost < (cost[costKey] ?: Integer.MAX_VALUE)) {
                    cost[costKey] = nextCost
                    val nextState = LavaSearchState(nextRow, nextCol, nextStraight, nextDir)
                    parent[nextState] = currentState
                    buffer.offer(nextState to nextCost + (heuristic[costKey] ?: 0))
                }
            }
        }
    }

    // traceback
    var current = target
    val path = ArrayDeque<LavaSearchState>()
    while (current != null) {
        path.addFirst(current)
        current = parent[current]
    }

    fun determineOperation(prevDir: Direction, curDir: Direction): String {
        return when {
            prevDir == curDir -> Operation.FORWARD.toString()
            prevDir.rotateLeft() == curDir -> Operation.LEFT.toString()
            prevDir.rotateRight() == curDir -> Operation.RIGHT.toString()
            else -> "FORBIDDEN"
        }
    }

    // print
    var previous: LavaSearchState? = null
    for (state in path) {
        print("$state")
        if (previous == null) {
            println()
        } else {
            println(determineOperation(previous.dir, state.dir))
        }
        previous = state
    }

    return cost[goal] ?: -1
}

private fun performDijkstra(
    array: Primitive2DCharArray,
    startState: LavaSearchState
): HashMap<Coordinate, Int> {
    // Initialize search stuff
    val cost = HashMap<Coordinate, Int>()
    val visited = HashSet<LavaSearchState>()
    val parent = HashMap<LavaSearchState, LavaSearchState>()
    val buffer = PriorityQueue<Pair<LavaSearchState, Int>>(Comparator.comparing { it.second })

    // handle start state
    cost[Coordinate(startState.row, startState.col)] = 0
    buffer.offer(startState to 0)

    fun createAndAddNextState(currentState: LavaSearchState, op: Operation, costSoFar: Int) {
        val (row, col, straight, dir) = currentState
        val (nextDir, nextStraight) = nextDirAndStraight(dir, straight, op)
        val (dRow, dCol) = nextDir.delta
        val nextRow = row + dRow
        val nextCol = col + dCol
        if (nextRow in 0..<array.height && nextCol in 0..<array.width) { // only if next state is in bounds
            val costKey = Coordinate(nextRow, nextCol)
            val nextCost = costSoFar + array[nextRow, nextCol].digitToInt()
            if (nextCost < (cost[costKey] ?: Integer.MAX_VALUE)) {
                cost[costKey] = nextCost
                val nextState = LavaSearchState(nextRow, nextCol, nextStraight, nextDir)
                parent[nextState] = currentState
                buffer.offer(nextState to nextCost)
            }
        }
    }

    // the actual search
    while (buffer.isNotEmpty()) {
        val (currentState, _) = buffer.poll()
        val currentCoordinate = Coordinate(currentState.row, currentState.col)
        if (currentState !in visited) {
            visited.add(currentState)
            val costSoFar: Int = cost[currentCoordinate]!! // if it is unset, the algorithm is incorrect
            createAndAddNextState(currentState, Operation.FORWARD, costSoFar)
            createAndAddNextState(currentState, Operation.RIGHT, costSoFar)
            createAndAddNextState(currentState, Operation.LEFT, costSoFar)
        }
    }

    return cost
}

private fun nextDirAndStraight(
    currentDir: Direction,
    currentStraight: Int,
    op: Operation
): Pair<Direction, Int> {
    val nextDir = when (op) {
        Operation.FORWARD -> currentDir
        Operation.LEFT -> currentDir.rotateLeft()
        Operation.RIGHT -> currentDir.rotateRight()
    }
    val nextStraight = when (op) {
        Operation.LEFT, Operation.RIGHT -> 1
        Operation.FORWARD -> currentStraight + 1
    }
    return Pair(nextDir, nextStraight)
}

private data class LavaSearchState(
    val row: Int,
    val col: Int,
    val straight: Int,
    val dir: Direction
)

private enum class Operation {
    LEFT, RIGHT, FORWARD
}

