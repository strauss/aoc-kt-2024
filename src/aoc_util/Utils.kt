package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveCharArray
import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readText

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

fun inBounds(position: Pair<Int, Int>, height: Int, width: Int): Boolean {
    return inBounds(position.first, position.second, height, width)
}

fun inBounds(y: Int, x: Int, height: Int, width: Int): Boolean {
    return y in 0..<height && x in 0..<width
}