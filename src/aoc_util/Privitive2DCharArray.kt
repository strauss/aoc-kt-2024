package aoc_util

import de.dreamcube.hornet_queen.array.PrimitiveCharArray

/**
 * Convenience class for representing 2D-Char-Arrays. Wraps around a [PrimitiveMultiDimArray], which in turn wraps
 * around a [PrimitiveCharArray].
 */
class Primitive2DCharArray(val height: Int, val width: Int) {

    companion object {
        /**
         * Creates a [Primitive2DCharArray] from the input lines. The [height] is determined by the length
         * of the [lines] list. The [width] is determined by the longest element. Shorter rows are filled with the
         * [fillChar], which defaults to a space character (0x20).
         */
        fun parseFromLines(lines: List<String>, fillChar: Char = ' '): Primitive2DCharArray {
            val width = lines.asSequence().map { it.length }.max()
            val out = Primitive2DCharArray(lines.size, width)
            var row = 0
            lines.forEach { line ->
                for (col in 0..<width) {
                    out[row, col] = if (col < line.length) line[col] else fillChar
                }
                row += 1
            }
            return out
        }
    }

    /**
     * Internal representation as [PrimitiveMultiDimArray]
     */
    private val internalArray = PrimitiveMultiDimArray(height, width) { size -> PrimitiveCharArray(size) }

    /**
     * Primary access method. Allows for accessing with:
     * ```kotlin
     * val foo = array[row, col]
     * ```
     */
    operator fun get(row: Int, col: Int) = internalArray[row, col]

    /**
     * Primary write method. Allows for writing with:
     * ```kotlin
     * array[row, col] = bar
     * ```
     */
    operator fun set(row: Int, col: Int, value: Char) {
        internalArray[row, col] = value
    }

    /**
     * Iterates all elements row by row from left to right. Falls back to the internal 1D-Array for this.
     */
    operator fun iterator() = internalArray.iterator()

    /**
     * Retrieves a single row as [String].
     */
    fun getRow(row: Int): String {
        val charArray = CharArray(width)
        for (col in 0..<width) {
            charArray[col] = this[row, col]
        }
        return String(charArray)
    }

    /**
     * Retrieves a single column as [String].
     */
    fun getColumn(col: Int): String {
        val charArray = CharArray(height)
        for (row in 0..<height) {
            charArray[row] = this[row, col]
        }
        return String(charArray)
    }

    /**
     * Lists all rows from top to bottom.
     */
    fun rows(): List<String> = buildList {
        for (row in 0..<height) {
            add(getRow(row))
        }
    }

    /**
     * Lists all columns from left to right.
     */
    fun columns(): List<String> = buildList {
        for (col in 0..<width) {
            add(getColumn(col))
        }
    }

    /**
     * Creates a new [Primitive2DCharArray] and applies the transpose operation while creating it. The [height] of the
     * created array will be the [width] of this array and the other way around. The elements will appear as if
     * reflected at the main diagonal. You can also think of it as "rotate 90° counterclockwise and flip vertically".
     * However, we are reflecting, because it is faster.
     */
    fun transpose(): Primitive2DCharArray {
        val result = Primitive2DCharArray(width, height)
        for (oldRow in 0..<height) {
            for (oldCol in 0..<width) {
                result[oldCol, oldRow] = this[oldRow, oldCol]
            }
        }
        return result
    }

    /**
     * String representation. If it was parsed, it reflects the input data. It uses '\n' as line break.
     */
    override fun toString(): String = buildString {
        if (height <= 0) {
            return ""
        }
        append(getRow(0))
        for (row in 1..<height) {
            append('\n')
            append(getRow(row))
        }
    }
}