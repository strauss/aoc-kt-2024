package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveCharArray
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.math.abs

fun readInput2023(name: String) = internalReadInput(name, 2023)

fun readInput2024(name: String) = internalReadInput(name, 2024)

/**
 * Reads lines from the given input txt file.
 */
private fun internalReadInput(name: String, year: Int) = Path("aoc/$year/$name.txt").readText().trim().lines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)

fun parseInputAsMultiDimArray(input: List<String>): PrimitiveMultiDimArray<Char> {
    val dim = input[0].length // I don't care about any exception here ... if empty -> bad luck
    val out: PrimitiveMultiDimArray<Char> = PrimitiveMultiDimArray(input.size, dim) { size -> PrimitiveCharArray(size) }
    var j = 0
    input.forEach { line ->
        for (i in line.indices) {
            out[j, i] = line[i]
        }
        j += 1
    }
    return out
}

val intNumberRegex = "(-?\\d+)".toRegex()
fun String.extractInts(row: Int = 0): List<IntAndLocation> {
    val out: MutableList<IntAndLocation> = ArrayList()
    intNumberRegex.findAll(this).iterator().forEach { matchResult: MatchResult ->
        out.add(IntAndLocation(matchResult.groupValues[1].toInt(), row, matchResult.range))
    }
    return out
}

fun String.extractSchlong(): List<Long> {
    val out = PrimitiveLongArrayList()
    intNumberRegex.findAll(this).iterator().forEach { matchResult: MatchResult ->
        out.add(matchResult.groupValues[1].toLong())
    }
    return out
}

/**
 * Pairs up entries in a list. If the list has an odd size, the last element is dropped.
 */
fun <T> List<T>.createPairs(): List<Pair<T, T>> {
    val out = ArrayList<Pair<T, T>>()
    val iterator = iterator()
    while (iterator.hasNext()) {
        val first = iterator.next()
        if (iterator.hasNext()) {
            val second = iterator.next()
            out.add(Pair(first, second))
        }
    }
    return out
}

data class IntAndLocation(val number: Int, val row: Int, val range: IntRange)

fun getAdjacentValues(
    array: PrimitiveMultiDimArray<Char>,
    row: Int,
    col: Int,
    filter: (Char) -> Boolean = { true }
): List<Pair<Char, Pair<Int, Int>>> {
    val result: MutableList<Pair<Char, Pair<Int, Int>>> = ArrayList()

    result.addIfInBounds(array, row - 1, col, filter)// north
    result.addIfInBounds(array, row - 1, col + 1, filter) // north-east
    result.addIfInBounds(array, row, col + 1, filter) // east
    result.addIfInBounds(array, row + 1, col + 1, filter) // south-east
    result.addIfInBounds(array, row + 1, col, filter) // south
    result.addIfInBounds(array, row + 1, col - 1, filter) // south-west
    result.addIfInBounds(array, row, col - 1, filter) // west
    result.addIfInBounds(array, row - 1, col - 1, filter) // north-west

    return result
}

fun MutableList<Pair<Char, Pair<Int, Int>>>.addIfInBounds(
    from: PrimitiveMultiDimArray<Char>,
    row: Int,
    col: Int,
    filter: (Char) -> Boolean = { true }
) {
    val height = from.getDimensionSize(0)
    val width = from.getDimensionSize(1)
    if (inBounds(row, col, height, width)) {
        val element = from[row, col]
        if (filter(element)) {
            this.add(Pair(element, Pair(row, col)))
        }
    }
}

fun show(maze: PrimitiveMultiDimArray<Char>): String {
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

fun inBounds(position: Pair<Int, Int>, height: Int, width: Int): Boolean {
    return inBounds(position.first, position.second, height, width)
}

fun inBounds(y: Int, x: Int, height: Int, width: Int): Boolean {
    return y in 0..<height && x in 0..<width
}

fun String.asOctalLong(): Long {
    if (isBlank()) {
        return 0
    }
    return when (this) {
        "00" -> 0L
        "01" -> 1L
        "02" -> 2L
        "03" -> 3L
        "04" -> 4L
        "05" -> 5L
        "06" -> 6L
        "07" -> 7L
//        "08" -> 8L
//        "09" -> 9L
        else -> this.toLong(8)
    }
}

fun Long.asOctalString(): String {
    if (this in 0L..<8L) {
        return when (this) {
            0L -> "00"
            1L -> "01"
            2L -> "02"
            3L -> "03"
            4L -> "04"
            5L -> "05"
            6L -> "06"
            7L -> "07"
            else -> error("unreachable")
        }
    }
    val out = StringBuilder()
    var n = abs(this)
    while (n > 0) {
        out.append(n % 8)
        n /= 8
    }
    if (this < 0) {
        out.append("-")
    }
    return out.toString().reversed()
}

fun main() {
    println("11".asOctalLong())

    println(16L.asOctalString())
}

data class Coordinate(val row: Int, val col: Int) {
    fun getNorth() = Coordinate(row - 1, col)
    fun getEast() = Coordinate(row, col + 1)
    fun getSouth() = Coordinate(row + 1, col)
    fun getWest() = Coordinate(row, col - 1)

    fun manhattanDistance(other: Coordinate): Int {
        val rowDistance = abs(row - other.row)
        val colDistance = abs(col - other.col)
        return rowDistance + colDistance
    }

    operator fun plus(movement: Movement) = Coordinate(row + movement.deltaRow, col + movement.deltaCol)

    operator fun plus(movements: List<Movement>): Coordinate {
        var deltaRow = 0
        var deltaCol = 0
        for (movement in movements) {
            deltaRow += movement.deltaRow
            deltaCol += movement.deltaCol
        }
        return Coordinate(row + deltaRow, col + deltaCol)
    }
}

enum class Movement(val deltaRow: Int, val deltaCol: Int) {
    NORTH(-1, 0),
    NORTH_EAST(-1, 1),
    EAST(0, 1),
    SOUTH_EAST(1, 1),
    SOUTH(1, 0),
    SOUTH_WEST(1, -1),
    WEST(0, -1),
    NORTH_WEST(-1, -1),
    STAY(0, 0)
}

fun <T> Pair<T, T>.getInverse() = this.second to this.first