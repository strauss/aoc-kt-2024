package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveCharArray
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
    val out: PrimitiveMultiDimArray<Char> = PrimitiveMultiDimArray(dim, dim) { size -> PrimitiveCharArray(size) }
    var j = 0
    input.forEach { line ->
        for (i in line.indices) {
            out[j, i] = line[i]
        }
        j += 1
    }
    return out
}