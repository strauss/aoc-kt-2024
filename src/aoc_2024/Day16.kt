package aoc_2024

import aoc_util.PrimitiveMultiDimArray
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2024
import aoc_util.show
import java.util.*

private const val wall = '#'
private const val free = '.'
private const val start = 'S'
private const val target = 'E'
private const val north = 0
private const val east = 90
private const val south = 180
private const val west = 270
private const val stepCost = 1
private const val turnCost = 1000

fun main() {
    val testInput = readInput2024("Day16_test")
    val testMaze = parseInputAsMultiDimArray(testInput)
    val testResult = costToTarget(testMaze)
    println(show(testMaze))
    println("Test result: $testResult")

    val input = readInput2024("Day16")
    val maze = parseInputAsMultiDimArray(input)
    val result = costToTarget(maze)
    println("Result: $result")
}

private fun costToTarget(maze: PrimitiveMultiDimArray<Char>): Int {
    val start = searchFor(maze, start)
    val target = searchFor(maze, target)
    val parent = performSearch(maze, start, target)
    val moves = determinePath(parent, target)
    return moves.asSequence().map { it.cost }.sum()
}

private fun determinePath(parent: Map<Position, Position>, target: Pair<Int, Int>): List<Move> {
    val (tRow, tCol) = target
    val targetPosition: Position?
    val targetToNorth = Position(tRow, tCol, north)
    val targetToEast = Position(tRow, tCol, east)
    val targetToSouth = Position(tRow, tCol, south)
    val targetToWest = Position(tRow, tCol, west)
    targetPosition = when {
        parent.containsKey(targetToNorth) -> targetToNorth
        parent.containsKey(targetToEast) -> targetToEast
        parent.containsKey(targetToSouth) -> targetToSouth
        parent.containsKey(targetToWest) -> targetToWest
        else -> null
    }
    if (targetPosition == null) {
        return emptyList()
    }
    val result = mutableListOf<Move>()
    var currentChild: Position = targetPosition
    while (true) {
        val currentParent = parent[currentChild] ?: break
        result.add(currentParent.getMove(currentChild))
        currentChild = currentParent
    }
    return result.reversed()
}

private fun performSearch(maze: PrimitiveMultiDimArray<Char>, start: Pair<Int, Int>, target: Pair<Int, Int>): Map<Position, Position> {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    val queue: PriorityQueue<PositionAndCost> = PriorityQueue()
    val visited = mutableSetOf<Pair<Int, Int>>()
    val parent = mutableMapOf<Position, Position>()
    val startPosition = Position(start.first, start.second, east)
    queue.offer(PositionAndCost(startPosition, 0))
    visited.add(start)
    while (queue.isNotEmpty()) {
        val currentPositionAndCost = queue.poll()
        val (currentPosition, currentCost) = currentPositionAndCost
        val (row, col, direction) = currentPosition
        val current = Pair(row, col)
        val adjacentPositions = getAdjacentPositions(row, col, height, width).filter { maze[it.first, it.second] != wall }
        val straight = current + direction.direction()
        val leftDirection = direction.turn(-90)
        val left = current + leftDirection.direction()
        val rightDirection = direction.turn(+90)
        val right = current + rightDirection.direction()
        if (adjacentPositions.contains(straight) && !visited.contains(straight)) {
            val position = Position(straight.first, straight.second, direction)
            queue.offer(PositionAndCost(position, currentCost + stepCost))
            parent[position] = currentPosition
            visited.add(straight)
        }
        if (adjacentPositions.contains(left) && !visited.contains(left)) {
            val position = Position(left.first, left.second, leftDirection)
            queue.offer(PositionAndCost(position, currentCost + stepCost + turnCost))
            parent[position] = currentPosition
            visited.add(left)
        }
        if (adjacentPositions.contains(right) && !visited.contains(right) && maze[right.first, right.second] != wall) {
            // for dead ends, we always turn right
            val position = Position(right.first, right.second, rightDirection)
            queue.offer(PositionAndCost(position, currentCost + stepCost + turnCost))
            parent[position] = currentPosition
            visited.add(right)
        }
    }
    return parent
}

private fun getAdjacentPositions(y: Int, x: Int, height: Int, width: Int): List<Pair<Int, Int>> {
    return mutableListOf(Pair(y + 1, x), Pair(y - 1, x), Pair(y, x + 1), Pair(y, x - 1)).filter { (y, x) ->
        y in 0..<height && x in 0..<width
    }
}

private fun searchFor(array: PrimitiveMultiDimArray<Char>, searchFor: Char): Pair<Int, Int> {
    val height = array.getDimensionSize(0)
    val width = array.getDimensionSize(1)
    for (row in 0 until height) {
        for (col in 0 until width) {
            val currentField = array[row, col]
            if (currentField == searchFor) {
                return Pair(row, col)
            }
        }
    }
    return Pair(-1, -1)
}

private data class Position(val row: Int, val col: Int, val direction: Int)

private data class PositionAndCost(val position: Position, val cost: Int) : Comparable<PositionAndCost> {
    override fun compareTo(other: PositionAndCost): Int {
        return cost.compareTo(other.cost)
    }
}

private data class Move(val vert: Int, val hor: Int, val turn: Int, val cost: Int)

//private fun Position.move(move: Move) = Position(row + move.vert, col + move.hor, direction.turn(move.turn))
private fun Position.getMove(other: Position): Move {
    val turnDifference = other.direction - direction
    val turn = if (turnDifference >= 0) turnDifference else 360 + turnDifference
    val turnCost = turnCost * ((turn % 180) / 90)
    val moveCost = if (row == other.row && col == other.col) 0 else 1

    return Move(other.row - row, other.col - col, turn, turnCost + moveCost)
}

private fun Int.turn(delta: Int): Int {
    val newDirection = this + delta
    if (newDirection < 0) {
        return (360 + newDirection)
    }
    return (this + delta) % 360
}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) = Pair(this.first + other.first, this.second + other.second)

private fun Int.isNorth() = this == north
private fun Int.isEast() = this == east
private fun Int.isSouth() = this == south
private fun Int.isWest() = this == west

private fun Int.direction(): Pair<Int, Int> {
    return when {
        this.isNorth() -> Pair(-1, 0)
        this.isEast() -> Pair(0, 1)
        this.isSouth() -> Pair(1, 0)
        this.isWest() -> Pair(0, -1)
        else -> Pair(0, 0)
    }
}