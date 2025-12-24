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
    solve("Result", input, ::searchOptimalHeatLoss)

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
    // Initialize search stuff
    val cost = HashMap<Coordinate, Int>()
    val visited = HashSet<LavaSearchState>()
    val parent = HashMap<LavaSearchState, LavaSearchState>()
    val buffer = PriorityQueue<Pair<LavaSearchState, Int>>(Comparator.comparing { it.second })

    // handle start state
    cost[Coordinate(start.row, start.col)] = 0
    buffer.offer(start to 0)

    var foundGoal: LavaSearchState? = null

    fun createAndAddNextState(currentState: LavaSearchState, op: Operation, costSoFar: Int) {
        val (row, col, straight, dir) = currentState
        val nextDir = when (op) {
            Operation.FORWARD -> dir
            Operation.LEFT -> dir.rotateLeft()
            Operation.RIGHT -> dir.rotateRight()
        }
        val nextStraight = when (op) {
            Operation.LEFT, Operation.RIGHT -> 1
            Operation.FORWARD -> straight + 1
        }
        val (dRow, dCol) = nextDir.delta
        val nextRow = row + dRow
        val nextCol = col + dCol
        if (
            nextStraight <= 3 && // TODO: here something is wrong ... maybe Dijkstra is not the way to go after all :-( or maybe we need a graph
            nextRow in 0..<array.height && nextCol in 0..<array.width
        ) { // only if next state is in bounds
            val costKey = Coordinate(nextRow, nextCol)
            val nextCost = costSoFar + array[nextRow, nextCol].digitToInt()
            if (nextCost < (cost[costKey] ?: Integer.MAX_VALUE)) {
                val nextState = LavaSearchState(nextRow, nextCol, nextStraight, nextDir)
                parent[nextState] = currentState
                cost[costKey] = nextCost
                buffer.offer(nextState to nextCost)
                if (goal.row == nextState.row && goal.col == nextState.col) {
                    foundGoal = currentState
                }
            }
        }
    }


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

    var current = foundGoal
    val path = ArrayDeque<LavaSearchState>()
    while (current != null) {
        path.addFirst(current)
        current = parent[current]
    }

    path.toList().forEach { println(it) }

    return cost[goal] ?: -1
}

private data class LavaSearchState(
    val row: Int,
    val col: Int,
    val straight: Int,
    val direction: Direction
)

private enum class Operation {
    LEFT, RIGHT, FORWARD
}

