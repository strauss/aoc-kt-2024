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
private const val marker = 'O'
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
    val testPathFields = markAllBestPaths(testMaze)
    println("Test path fields: $testPathFields")

    val input = readInput2024("Day16")
    val maze = parseInputAsMultiDimArray(input)
    val result = costToTarget(maze)
    println("Result: $result")
    val pathFields = markAllBestPaths(maze)
    println("Path fields: $pathFields")
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

private fun markAllBestPaths(maze: PrimitiveMultiDimArray<Char>): Int {
    val maxCost = costToTarget(maze)
    val start = searchFor(maze, start)
    val startPosition = Position(start.first, start.second, east)
    val target = searchFor(maze, target)
    val allBestPaths = exhaustiveSearch(maze, startPosition, target, 0, maxCost, setOf(startPosition), listOf(start), emptyList())
    val markMaze = maze.createCopy()
    for (path in allBestPaths) {
        for ((row, col) in path) {
            markMaze[row, col] = marker
        }
    }
    return countMarks(markMaze)
}

private fun countMarks(maze: PrimitiveMultiDimArray<Char>): Int {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    var out = 0
    for (row in 0..<height) {
        for (col in 0..<width) {
            val currentField = maze[row, col]
            if (currentField == marker) {
                out += 1
            }
        }
    }
    return out
}

private fun exhaustiveSearch(
    maze: PrimitiveMultiDimArray<Char>,
    position: Position,
    target: Pair<Int, Int>,
    currentCost: Int,
    maxCost: Int,
    fieldsSoFar: Set<Position>,
    pathSoFar: List<Pair<Int, Int>>,
    solutionsSoFar: List<List<Pair<Int, Int>>>,
): List<List<Pair<Int, Int>>> {
    if (currentCost > maxCost) {
        return solutionsSoFar
    }
    val currentPosition = Pair(position.row, position.col)
    val currentDirection = position.direction
    if (currentPosition == target) {
        return solutionsSoFar.plus(listOf(pathSoFar))
    }
    val localResultList = ArrayList(solutionsSoFar)
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    val (row, col) = currentPosition
    val adjacentPositions = getAdjacentPositions(row, col, height, width).filter { maze[it.first, it.second] != wall }

    val straight = currentPosition + currentDirection.direction()
    val straightPosition = Position(straight.first, straight.second, currentDirection)
    if (adjacentPositions.contains(straight) && !fieldsSoFar.contains(straightPosition)) {
        val localFieldsSoFar = fieldsSoFar.plus(straightPosition)
        val localPathSoFar = pathSoFar.plus(straight)
        val straightResult = exhaustiveSearch(
            maze,
            straightPosition,
            target,
            currentCost + stepCost,
            maxCost,
            localFieldsSoFar,
            localPathSoFar,
            solutionsSoFar
        )
        localResultList.addAll(straightResult)
    }
    val leftDirection = currentDirection.turn(-90)
    val left = currentPosition + leftDirection.direction()
    val leftPosition = Position(left.first, left.second, leftDirection)
    if (adjacentPositions.contains(left) && !fieldsSoFar.contains(leftPosition)) {
        val localFieldsSoFar = fieldsSoFar.plus(leftPosition)
        val localPathSoFar = pathSoFar.plus(left)
        val leftResult = exhaustiveSearch(
            maze,
            leftPosition,
            target,
            currentCost + stepCost + turnCost,
            maxCost,
            localFieldsSoFar,
            localPathSoFar,
            solutionsSoFar
        )
        localResultList.addAll(leftResult)
    }

    val rightDirection = currentDirection.turn(+90)
    val right = currentPosition + rightDirection.direction()
    val rightPosition = Position(right.first, right.second, rightDirection)
    if (adjacentPositions.contains(right) && !fieldsSoFar.contains(rightPosition)) {
        val localFieldsSoFar = fieldsSoFar.plus(rightPosition)
        val localPathSoFar = pathSoFar.plus(right)
        val rightResult = exhaustiveSearch(
            maze,
            rightPosition,
            target,
            currentCost + stepCost + turnCost,
            maxCost,
            localFieldsSoFar,
            localPathSoFar,
            solutionsSoFar
        )
        localResultList.addAll(rightResult)
    }
    return localResultList
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
    for (row in 0..<height) {
        for (col in 0..<width) {
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