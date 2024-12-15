package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveCharArray
import de.dreamcube.hornet_queen.list.PrimitiveCharArrayList
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
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
fun String.extractInts(): List<Int> {
    val out = PrimitiveIntArrayList()
    intNumberRegex.findAll(this).iterator().forEach { matchResult ->
        out.add(matchResult.groupValues[1].toInt())
    }
    return out
}

fun getAdjacentValues(array: PrimitiveMultiDimArray<Char>, row: Int, col: Int): List<Char> {
    val result = PrimitiveCharArrayList()

    result.addIfInBounds(array, row - 1, col) // north
    result.addIfInBounds(array, row - 1, col + 1) // north-east
    result.addIfInBounds(array, row, col + 1) // east
    result.addIfInBounds(array, row + 1, col + 1) // south-east
    result.addIfInBounds(array, row + 1, col) // south
    result.addIfInBounds(array, row + 1, col - 1) // south-west
    result.addIfInBounds(array, row, col - 1) // west
    result.addIfInBounds(array, row - 1, col - 1) // north-west

    return result
}

fun MutableList<Char>.addIfInBounds(
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
            this.add(element)
        }
    }
}

fun inBounds(position: Pair<Int, Int>, height: Int, width: Int): Boolean {
    return inBounds(position.first, position.second, height, width)
}

fun inBounds(y: Int, x: Int, height: Int, width: Int): Boolean {
    return y in 0..<height && x in 0..<width
}