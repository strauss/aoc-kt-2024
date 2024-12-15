package aoc_2024

import aoc_util.PrimitiveMultiDimArray
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.array.PrimitiveCharArray

private const val wall = '#'
private const val bot = '@'
private const val box = 'O'
private const val boxLeft = '['
private const val boxRight = ']'
private const val north = '^'
private const val east = '>'
private const val south = 'v'
private const val west = '<'
private const val free = '.'

fun main() {
//    test()
    solve()
}

private fun test() {
    val input = readInput2024("Day15_testX")
    val (array, moves) = parseInput(input)
    print(showMaze(array))
    simulateMovementX(array, moves)
}

private fun solve() {
    val testInput = readInput2024("Day15_test")
    val input = readInput2024("Day15")
    val (testArray, testMoves) = parseInput(testInput)
    val testArrayX = generateExpandedArray(testArray)
    val (array, moves) = parseInput(input)
    val arrayX = generateExpandedArray(array)
    simulateMovement(testArray, testMoves)
    simulateMovement(array, moves)
    println("Test result: ${evaluateCoordinates(testArray)}")
    println("Result: ${evaluateCoordinates(array)}")
    simulateMovementX(testArrayX, testMoves)
    simulateMovementX(arrayX, moves)
    showMaze(arrayX)
    println("Test result X: ${evaluateCoordinatesX(testArrayX)}")
    println("Result X: ${evaluateCoordinatesX(arrayX)}")
    // 1488522 is too low
}

private fun simulateMovementX(maze: PrimitiveMultiDimArray<Char>, moves: String) {
    // search for bot
    var botPosition: Pair<Int, Int> = searchForBot(maze)
    var step = 0
    for (move: Char in moves) {
        println(move)
        val direction = move.parseMovement()
        val targetPosition = botPosition + direction
        val target = maze[targetPosition.first, targetPosition.second]
        when {
            target.isFree() -> {
                // simple movement
                botPosition = move(maze, botPosition, targetPosition)
            }

            target.isWall() -> {
                // do nothing
            }

            target.isBox() -> {
                @Suppress("inline") // better readability
                val boxPosition = targetPosition
                if (direction.isVerticalMove()) {
                    val otherBoxPosition = getOtherBoxPosition(target, boxPosition)
                    if (canMoveVertically(maze, boxPosition, otherBoxPosition, direction)) {
                        moveBoxesVertically(maze, boxPosition, otherBoxPosition, direction)
                        // finally move bot
                        botPosition = move(maze, botPosition, targetPosition)
                    }
                } else {
                    if (canMove(maze, boxPosition, direction)) {
                        moveBoxesHorizontally(maze, boxPosition, direction)
                        // finally move bot
                        botPosition = move(maze, botPosition, targetPosition)
                    }
                }
            }
        }
        step += 1
    }
}

private fun Pair<Int, Int>.isVerticalMove() = second == 0

private fun getOtherBoxPosition(box: Char, boxPosition: Pair<Int, Int>) =
    if (box.isBoxRight()) Pair(boxPosition.first, boxPosition.second - 1) else Pair(boxPosition.first, boxPosition.second + 1)

private fun moveBoxesVertically(
    maze: PrimitiveMultiDimArray<Char>,
    boxPosition: Pair<Int, Int>,
    otherBoxPosition: Pair<Int, Int>,
    direction: Pair<Int, Int>
) {
    val target = boxPosition + direction
    val otherTarget = otherBoxPosition + direction

    val targetElement = maze[target.first, target.second]
    val otherTargetElement = maze[otherTarget.first, otherTarget.second]

    when {
        // both are boxes
        targetElement.isBox() && otherTargetElement.isBox() -> {
            val targetOtherBoxPosition = getOtherBoxPosition(targetElement, target)
            if (targetOtherBoxPosition == otherTarget) {
                // if both boxes are "the same", they can be moved as a whole
                moveBoxesVertically(maze, target, otherTarget, direction)
            } else {
                // otherwise, we need to find the other half of the second one and move both boxes
                val otherTargetOtherBoxPosition = getOtherBoxPosition(otherTargetElement, otherTarget)
                moveBoxesVertically(maze, target, targetOtherBoxPosition, direction)
                moveBoxesVertically(maze, otherTarget, otherTargetOtherBoxPosition, direction)
            }
        }
        // one of them is a box
        targetElement.isBox() -> {
            val targetOtherBoxPosition = getOtherBoxPosition(targetElement, target)
            moveBoxesVertically(maze, target, targetOtherBoxPosition, direction)
        }

        otherTargetElement.isBox() -> {
            val otherTargetOtherBoxPosition = getOtherBoxPosition(otherTargetElement, otherTarget)
            moveBoxesVertically(maze, otherTarget, otherTargetOtherBoxPosition, direction)
        }
        // if none are boxes, both are considered free and not moved
    }
    move(maze, boxPosition, target)
    move(maze, otherBoxPosition, otherTarget)
}

private fun canMoveVertically(
    maze: PrimitiveMultiDimArray<Char>,
    source: Pair<Int, Int>,
    otherSource: Pair<Int, Int>,
    direction: Pair<Int, Int>
): Boolean {
    val target = source + direction
    val otherTarget = otherSource + direction
    val targetElement = maze[target.first, target.second]
    val otherTargetElement = maze[otherTarget.first, otherTarget.second]

    return when {
        targetElement.isWall() || otherTargetElement.isWall() -> false

        // both are boxes
        targetElement.isBox() && otherTargetElement.isBox() -> {
            val targetOtherBoxPosition = getOtherBoxPosition(targetElement, target)
            if (targetOtherBoxPosition == otherTarget) {
                // if both boxes are "the same", they can be checked as a whole
                canMoveVertically(maze, target, otherTarget, direction)
            } else {
                // otherwise, we need to find the other half of the second one and check both boxes
                val otherTargetOtherBoxPosition = getOtherBoxPosition(otherTargetElement, otherTarget)
                canMoveVertically(maze, target, targetOtherBoxPosition, direction) &&
                        canMoveVertically(maze, otherTarget, otherTargetOtherBoxPosition, direction)
            }
        }
        // one of them is a box
        targetElement.isBox() -> {
            val targetOtherBoxPosition = getOtherBoxPosition(targetElement, target)
            canMoveVertically(maze, target, targetOtherBoxPosition, direction)
        }

        otherTargetElement.isBox() -> {
            val otherTargetOtherBoxPosition = getOtherBoxPosition(otherTargetElement, otherTarget)
            canMoveVertically(maze, otherTarget, otherTargetOtherBoxPosition, direction)
        }

        else -> true
    }
}

private fun moveBoxesHorizontally(
    maze: PrimitiveMultiDimArray<Char>,
    boxPosition: Pair<Int, Int>,
    direction: Pair<Int, Int>
) {
    val target = boxPosition + direction
    val targetElement = maze[target.first, target.second]
    if (targetElement.isBox()) {
        moveBoxesHorizontally(maze, target, direction)
    }
    move(maze, boxPosition, target)
}

private tailrec fun canMove(maze: PrimitiveMultiDimArray<Char>, source: Pair<Int, Int>, direction: Pair<Int, Int>): Boolean {
    val target = source + direction
    val (targetRow, targetCol) = target
    val targetElement = maze[targetRow, targetCol]
    return when {
        targetElement.isWall() -> false
        targetElement.isBox() -> canMove(maze, target, direction)
        else -> true
    }
}

private fun simulateMovement(maze: PrimitiveMultiDimArray<Char>, moves: String) {
    // search for bot
    var botPosition: Pair<Int, Int> = searchForBot(maze)

    for (move: Char in moves) {
        val direction = move.parseMovement()
        val targetPosition = botPosition + direction
        val target = maze[targetPosition.first, targetPosition.second]
        when {
            target.isFree() -> {
                // simple movement
                botPosition = move(maze, botPosition, targetPosition)
            }

            target.isWall() -> {
                // do nothing
            }

            target.isBox() -> {
                // check if box(es) can be moved
                var localTarget = target
                var localTargetPosition = targetPosition
                while (!(localTarget.isFree() || localTarget.isWall())) {
                    localTargetPosition += direction
                    localTarget = maze[localTargetPosition.first, localTargetPosition.second]
                }
                if (localTarget.isFree()) {
                    // if they can, move them all
                    var localSourcePosition: Pair<Int, Int> = localTargetPosition
                    while (localSourcePosition != targetPosition) {
                        localSourcePosition -= direction
                        move(maze, localSourcePosition, localTargetPosition)
                        localTargetPosition -= direction
                    }
                    // finally move bot
                    botPosition = move(maze, botPosition, targetPosition)
                }
            }
        }
    }
}

private fun generateExpandedArray(maze: PrimitiveMultiDimArray<Char>): PrimitiveMultiDimArray<Char> {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    val mazeX: PrimitiveMultiDimArray<Char> = PrimitiveMultiDimArray(height, width * 2) { PrimitiveCharArray(it) }
    for (row in 0..<height) {
        for (col in 0..<width) {
            val element = maze[row, col]
            if (element.isBox()) {
                mazeX[row, col * 2] = boxLeft
                mazeX[row, col * 2 + 1] = boxRight
            } else {
                mazeX[row, col * 2] = element
                if (!element.isBot()) {
                    mazeX[row, col * 2 + 1] = element
                } else {
                    mazeX[row, col * 2 + 1] = free
                }
            }
        }
    }
    return mazeX
}

private fun evaluateCoordinates(maze: PrimitiveMultiDimArray<Char>): Int {
    var result = 0
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    for (row in 0..<height) {
        for (col in 0..<width) {
            if (maze[row, col].isBox()) {
                result += 100 * row + col
            }
        }
    }
    return result
}

private fun evaluateCoordinatesX(maze: PrimitiveMultiDimArray<Char>): Int {
    var result = 0
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    for (row in 0..<height) {
        for (col in 0..<width) {
            if (maze[row, col].isBoxLeft()) {
                result += 100 * row + col
            }
        }
    }
    return result
}

private fun move(
    maze: PrimitiveMultiDimArray<Char>,
    source: Pair<Int, Int>,
    target: Pair<Int, Int>
): Pair<Int, Int> {
    maze[target.first, target.second] = maze[source.first, source.second]
    maze[source.first, source.second] = free
    return target
}

private fun showMaze(maze: PrimitiveMultiDimArray<Char>): String {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    val out = StringBuilder()
    for (row in 0..<height) {
        for (col in 0..<width) {
            out.append(maze[row, col])
        }
        out.append('\n')
    }
    return out.toString()
}

private fun searchForBot(maze: PrimitiveMultiDimArray<Char>): Pair<Int, Int> {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    for (row in 0..<height) {
        for (col in 0..<width) {
            if (maze[row, col].isBot()) {
                return Pair(row, col)
            }
        }
    }
    return Pair(-1, -1)
}

private operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> = Pair(this.first + other.first, this.second + other.second)
private operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) = Pair(this.first - other.first, this.second - other.second)

private fun Char.parseMovement(): Pair<Int, Int> {
    return when {
        this.isNorth() -> Pair(-1, 0)
        this.isEast() -> Pair(0, 1)
        this.isSouth() -> Pair(1, 0)
        this.isWest() -> Pair(0, -1)
        else -> Pair(0, 0)
    }
}

private fun Char.isWall() = this == wall
private fun Char.isBot() = this == bot
private fun Char.isBox() = this == box || this.isBoxLeft() || this.isBoxRight()
private fun Char.isBoxLeft() = this == boxLeft
private fun Char.isBoxRight() = this == boxRight
private fun Char.isNorth() = this == north
private fun Char.isEast() = this == east
private fun Char.isSouth() = this == south
private fun Char.isWest() = this == west
private fun Char.isFree() = this == free


private fun parseInput(input: List<String>): Pair<PrimitiveMultiDimArray<Char>, String> {
    val mazeLines = mutableListOf<String>()
    val moveLines = mutableListOf<String>()
    var readMazeLines = true
    for (line in input) {
        if (line.isBlank()) {
            readMazeLines = false
            continue
        }
        if (readMazeLines) {
            mazeLines.add(line)
        } else {
            moveLines.add(line)
        }
    }
    val array = parseInputAsMultiDimArray(mazeLines)
    val moves = moveLines.joinToString()
    return Pair(array, moves)
}