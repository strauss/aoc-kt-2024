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

fun main() {
    val list = listOf(1, 3, 989, 3, 3, 9098, 3, 0, 3)
    println(list)
    println(list.split(3, inclusive = true, keepTrailingEmptyList = false))
}

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
 * Pairs up entries in [this] list. If [this] list has an odd size, the last element is dropped. If [this] list is empty, an empty list is returned.
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

/**
 * Creates a list of transition pairs from [this] list. Every element is paired up with its successor. Each element appears twice in the result
 * list, once on each side of a pair in the result list. If [this] list's size is less than 2, [this] list has no transitions an empty list is the
 * result.
 */
fun <T> List<T>.createTransitionPairs(): List<Pair<T, T>> {
    val out = ArrayList<Pair<T, T>>()
    val iterator = iterator()
    if (iterator.hasNext()) {
        var current = iterator.next()
        while (iterator.hasNext()) {
            val next = iterator.next()
            out.add(current to next)
            current = next
        }
    }
    return out
}

/**
 * Splits the [this] list at all positions containing the given [element]. If the element is not contained in [this] list, the result will be a list,
 * containing a copy of [this] list. If [inclusive] is true, each segment will contain the given [element] in the last position, except for the last
 * segment. If the last element of [this] list is the given [element], the last segment will be an empty list without the given [element]. If
 * [keepTrailingEmptyList] is set to false, a potential trailing empty list will be omitted.
 */
fun <T> List<T>.split(element: T, inclusive: Boolean = false, keepTrailingEmptyList: Boolean = true): List<List<T>> {
    val out = ArrayList<List<T>>()
    var currentSegment = ArrayList<T>()
    for (e in this) {
        if (e == element) {
            if (inclusive) {
                currentSegment.add(e)
            }
            out.add(currentSegment)
            currentSegment = ArrayList()
        } else {
            currentSegment.add(e)
        }
    }
    if (keepTrailingEmptyList || currentSegment.isNotEmpty()) {
        out.add(currentSegment)
    }
    return out
}

/**
 * Counts chunks of repeating entries in [this] list.
 */
fun <T> List<T>.countChunks(): Int {
    if (isEmpty()) {
        return 0
    }
    val iterator = iterator()
    var previous = iterator.next()
    var chunks = 1
    while (iterator.hasNext()) {
        val current = iterator.next()
        if (previous != current) {
            chunks += 1
        }
        previous = current
    }
    return chunks
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
