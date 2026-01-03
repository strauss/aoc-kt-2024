package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveCharArray
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.time.measureTimedValue

fun readInput2023(name: String) = internalReadInput(name, 2023)

fun readInput2024(name: String) = internalReadInput(name, 2024)

fun readInput2025(name: String) = internalReadInput(name, 2025)

fun <In, Out> solve(prefix: String, input: In, with: (In) -> Out): Out {
    val (result, duration) = measureTimedValue {
        with(input)
    }
    println("$prefix: $result (Duration: $duration)")
    return result
}

/**
 * Reads lines from the given input txt file.
 */
private fun internalReadInput(name: String, year: Int) = Path("aoc/$year/$name.txt").readText().lines()

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
    val dim =
        input.asSequence().map { it.length }.max() // I don't care about any exception here ... if empty -> bad luck
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
fun String.extractIntsWithLocation(row: Int = 0): List<IntAndLocation> {
    val out: MutableList<IntAndLocation> = ArrayList()
    intNumberRegex.findAll(this).iterator().forEach { matchResult: MatchResult ->
        out.add(IntAndLocation(matchResult.groupValues[1].toInt(), row, matchResult.range))
    }
    return out
}

fun String.extractLongs(): List<Long> {
    val out = PrimitiveLongArrayList()
    intNumberRegex.findAll(this).iterator().forEach { matchResult: MatchResult ->
        out.add(matchResult.groupValues[1].toLong())
    }
    return out
}

fun String.extractInts(): List<Int> {
    val out = PrimitiveIntArrayList()
    intNumberRegex.findAll(this).iterator().forEach { matchResult: MatchResult ->
        out.add(matchResult.groupValues[1].toInt())
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

fun <T> List<T>.containsSublist(potentialSublist: List<T>): Boolean {
    if (potentialSublist.size > size) {
        return false
    }
    if (potentialSublist.isEmpty()) {
        return true
    }
    outer@ for (i in indices) {
        for (j in potentialSublist.indices) {
            if (i + j > lastIndex) {
                return false
            }
            if (this[i + j] != potentialSublist[j]) {
                continue@outer
            }
        }
        return true
    }
    return false
}

fun <T> List<T>.startsWith(startList: List<T>): Boolean {
    if (startList.size > size) {
        return false
    }
    if (startList.isEmpty()) {
        return true
    }
    val iterator = iterator()
    val otherIterator = startList.iterator()
    while (otherIterator.hasNext()) {
        val myNext = iterator.next()
        val otherNext = otherIterator.next()
        if (myNext != otherNext) {
            return false
        }
    }
    return true
}

data class IntAndLocation(val number: Int, val row: Int, val range: IntRange)

fun <T> PrimitiveMultiDimArray<T>.getAdjacentValues(
    row: Int,
    col: Int,
    filter: (T) -> Boolean = { true }
): List<Pair<T, Pair<Int, Int>>> {
    val result: MutableList<Pair<T, Pair<Int, Int>>> = ArrayList()
    result.addIfInBounds(this, row - 1, col, filter)// north
    result.addIfInBounds(this, row - 1, col + 1, filter) // north-east
    result.addIfInBounds(this, row, col + 1, filter) // east
    result.addIfInBounds(this, row + 1, col + 1, filter) // south-east
    result.addIfInBounds(this, row + 1, col, filter) // south
    result.addIfInBounds(this, row + 1, col - 1, filter) // south-west
    result.addIfInBounds(this, row, col - 1, filter) // west
    result.addIfInBounds(this, row - 1, col - 1, filter) // north-west
    return result
}


private fun <T> PrimitiveMultiDimArray<T>.height(): Int = getDimensionSize(0)
private fun <T> PrimitiveMultiDimArray<T>.width(): Int = getDimensionSize(1)

fun <T> MutableList<Pair<T, Pair<Int, Int>>>.addIfInBounds(
    from: PrimitiveMultiDimArray<T>,
    row: Int,
    col: Int,
    filter: (T) -> Boolean = { true }
) {
    val height = from.height()
    val width = from.width()
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

    override fun toString(): String = "($row, $col)"
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

fun List<Any>.allEquals(): Boolean {
    if (size <= 1) {
        return true
    }
    val compareWith = this[0]
    for (any in this) {
        if (any != compareWith) {
            return false
        }
    }
    return true
}

fun <T> Pair<T, T>.getInverse() = this.second to this.first

tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

tailrec fun gcd(a: BigInteger, b: BigInteger): BigInteger = if (b == BigInteger.ZERO) a else gcd(b, a.mod(b))

fun lcm(a: Long, b: Long): Long {
    val gcd: Long = gcd(a, b)
    return (a * b).absoluteValue / gcd
}

fun combine(high: Int, low: Int): Long = high.toLong().shl(32) or (low.toLong() and 0xFFFF_FFFFL)

fun Long.getHigh(): Int = (this ushr 32).toInt()

fun Long.getLow(): Int = (this and 0xFFFF_FFFFL).toInt()

fun String.replaceLast(oldChar: Char, newChar: Char): String = this.reversed().replaceFirst(oldChar, newChar).reversed()

/**
 * Creates a new string with the character at the given [position] replaced with [newChar].
 */
fun String.changePosition(newChar: Char, position: Int): String {
    return replaceRange(position, position + 1, newChar.toString())
}

fun String.changePositions(newChar: Char, vararg positions: Int): String {
    val chars = this.toCharArray()
    for (i in positions) {
        chars[i] = newChar
    }
    return String(chars)
}

/**
 * Creates a sequence of [List]s based on [this] list. The given [indexes] mark the separators. The separator is placed
 * to the right of the actual element. Out of bound indexes are ignored. The resulting sublists are never empty.
 * Duplicate indexes are ignored.
 *
 * Examples: Given the list `[0,1,2,3,4,5]`. Then the following results will be given.
 *
 * - [indexes] = `[0]` -> `[[0], [1,2,3,4,5]]`
 * - [indexes] = `[1,4]` -> `[[0,1], [2,3,4], [5]]`
 * - [indexes] = `[]` -> `[[0,1,2,3,4,5]]`
 * - [indexes] = `[-1]` -> `[[0,1,2,3,4,5]]`
 * - [indexes] = `[5]` -> `[[0,1,2,3,4,5]]`
 * - [indexes] = `[6]` -> `[[0,1,2,3,4,5]]`
 * - [indexes] = `[-10, 3, 10]` -> `[[0,1,2,3], [4,5]]`
 * - [indexes] = `[1,3,3,3,4]` -> `[[0,1], [2,3], [4], [5]]`
 */
fun <T> List<T>.separateList(vararg indexes: Int): List<List<T>> {
    val list = this
    val indexesToUse = IntArray(indexes.size)
    System.arraycopy(indexes, 0, indexesToUse, 0, indexes.size)
    indexesToUse.sort()
    return buildList {
        var currentIndex = 0
        for (idx in indexesToUse) {
            if (idx !in 0..<list.size) {
                continue
            }
            val currentList: MutableList<T> = ArrayList()
            while (currentIndex <= idx) {
                currentList.add(list[currentIndex])
                currentIndex += 1
            }
            if (currentList.isNotEmpty()) { // "empty" can happen, if duplicates are included in the indexes.
                add(currentList)
            }
        }
        if (currentIndex < list.size) {
            val currentList: MutableList<T> = ArrayList()
            while (currentIndex < list.size) {
                currentList.add(list[currentIndex])
                currentIndex += 1
            }
            add(currentList)
        }
    }
}

fun List<Int>.isSorted(): Boolean {
    if (isEmpty() || size == 1) {
        return true
    }
    for (idx in 1..<this.size) {
        if (get(idx - 1) > get(idx)) {
            return false
        }
    }
    return true
}

fun <T> List<T>.allSeparations(size: Int): List<List<List<T>>> {
    if (size <= 0) {
        return listOf()
    }
    val list = this
    return buildList {
        val iterator = CombinatorialIterator(0..list.size - 2, size - 1, distinct = true)
        iterator.iterate { combination: List<Int> ->
            if (combination.isSorted()) {
                add(list.separateList(*combination.toIntArray()))
            }
        }
    }
}

fun IntRange.size(): Int = (-1).coerceAtLeast(endInclusive - start + 1)
