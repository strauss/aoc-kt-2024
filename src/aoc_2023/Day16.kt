package aoc_2023

import aoc_util.Primitive2DCharArray
import aoc_util.readInput2023
import aoc_util.solve
import java.util.BitSet
import kotlin.collections.ArrayDeque


private const val V_SPLIT = '|'
private const val H_SPLIT = '-'
private const val L_MIRROR = '\\'
private const val R_MIRROR = '/'
private const val SPACE = '.'

fun main() {
    val testLines = readInput2023("Day16_test")
    val testInput = Primitive2DCharArray.parseFromLines(testLines)
    solve("Test result", testInput, ::searchPaths)
    solve("Test 2 result", testInput, ::searchBestConfiguration)

    val lines = readInput2023("Day16")
    val input = Primitive2DCharArray.parseFromLines(lines)
    solve("Result", input, ::searchPaths)
    solve("Result2", input, ::searchBestConfiguration)

}

private fun searchBestConfiguration(array: Primitive2DCharArray): Int {
    // determine all start states
    val startStates = ArrayList<SearchState>()
    val top = array.getRow(0)
    for (col in top.indices) {
        startStates.add(SearchState(0, col, Direction.SOUTH))
    }
    val bottom = array.getRow(array.height - 1)
    for (col in bottom.indices) {
        startStates.add(SearchState(array.height - 1, col, Direction.NORTH))
    }
    val left = array.getColumn(0)
    for (row in left.indices) {
        startStates.add(SearchState(row, 0, Direction.EAST))
    }
    val right = array.getColumn(array.height - 1)
    for (row in right.indices) {
        startStates.add(SearchState(row, array.height - 1, Direction.WEST))
    }

    return startStates.map { searchPaths(array, it) }.max()
}

private fun searchPaths(array: Primitive2DCharArray, startState: SearchState = SearchState(0, 0, Direction.EAST)): Int {
    // Initialize search stuff
    val energized = BitSet(array.width * array.height)
    val buffer = ArrayDeque<SearchState>()
    val visited = HashSet<SearchState>()

    // handle start state
    visited.add(startState)
    buffer.addLast(startState)

    fun createAndAddNextState(row: Int, col: Int, dir: Direction) {
        val (dRow, dCol) = dir.delta
        val nextRow = row + dRow
        val nextCol = col + dCol
        if (nextRow in 0..<array.height && nextCol in 0..<array.width) { // only if next state is in bounds
            val nextState = SearchState(nextRow, nextCol, dir)
            if (nextState !in visited) {
                visited.add(nextState)
                buffer.addLast(nextState)
            }
        }
    }

    while (buffer.isNotEmpty()) {
        val (currentRow, currentCol, dir) = buffer.removeFirst()
        energized.set(currentRow * array.width + currentCol)
        val currentValue = array[currentRow, currentCol]
        when (currentValue) {
            SPACE -> createAndAddNextState(currentRow, currentCol, dir)
            L_MIRROR -> createAndAddNextState(currentRow, currentCol, dir.reflectWithLeftMirror())
            R_MIRROR -> createAndAddNextState(currentRow, currentCol, dir.reflectWithRightMirror())
            V_SPLIT -> {
                when (dir) {
                    Direction.NORTH, Direction.SOUTH -> createAndAddNextState(currentRow, currentCol, dir)
                    Direction.EAST, Direction.WEST -> {
                        createAndAddNextState(currentRow, currentCol, dir.rotateLeft())
                        createAndAddNextState(currentRow, currentCol, dir.rotateRight())
                    }
                }
            }

            H_SPLIT -> {
                when (dir) {
                    Direction.NORTH, Direction.SOUTH -> {
                        createAndAddNextState(currentRow, currentCol, dir.rotateLeft())
                        createAndAddNextState(currentRow, currentCol, dir.rotateRight())
                    }

                    Direction.EAST, Direction.WEST -> createAndAddNextState(currentRow, currentCol, dir)
                }
            }
        }

    }

    return energized.cardinality()
}


private enum class Direction(val delta: Pair<Int, Int>) {
    NORTH(-1 to 0), EAST(0 to 1), SOUTH(1 to 0), WEST(0 to -1);

    fun reflectWithRightMirror(): Direction = when (this) {
        NORTH -> EAST
        EAST -> NORTH
        SOUTH -> WEST
        WEST -> SOUTH
    }

    fun reflectWithLeftMirror(): Direction = when (this) {
        NORTH -> WEST
        EAST -> SOUTH
        SOUTH -> EAST
        WEST -> NORTH
    }

    fun rotateRight(): Direction = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }

    fun rotateLeft(): Direction = when (this) {
        NORTH -> WEST
        EAST -> NORTH
        SOUTH -> EAST
        WEST -> SOUTH
    }

}

private data class SearchState(val row: Int, val col: Int, val dir: Direction)

