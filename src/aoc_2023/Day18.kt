package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve
import java.util.TreeMap
import java.util.TreeSet
import kotlin.collections.ArrayDeque
import kotlin.math.abs

fun main() {
    val testLines = readInput2023("Day18_test")
    val (testInput, test2Input) = parseInputPart1(testLines)
//    solve("Test result", testInput, ::calculateAreaByFilling)
//    solve("Test result", testInput, ::calculateAreaWithSweepLine)
//    solve("Test 2 result", test2Input, ::calculateAreaWithSweepLine)

    val testPerimeter = setOf(
        0 at 0,
        7 at 0,

        1 at 1,
        6 at 1,

        3 at 3,
        4 at 3,
        6 at 3,
        7 at 3,

        1 at 4,
        3 at 4,

        0 at 5,
        4 at 5
    )
    val result = sweepLine(testPerimeter)
    println("Test perimeter: $result")

    val lines = readInput2023("Day18")
    val (input, input2) = parseInputPart1(lines)
//    solve("Result", input, ::calculateAreaByFilling)
    solve("Result", input, ::calculateAreaWithSweepLine)
//    solve("Result", input2, ::calculateAreaWithSweepLine)
}

private fun calculateAreaWithSweepLine(instructions: List<Instruction>): Long {
    // create perimeter
    val perimeterResult = buildPerimeter(instructions, full = false)
    return sweepLine(perimeterResult.perimeter)
}

private fun sweepLine(perimeter: Set<PerimeterPoint>): Long {
    // arrange perimeter points in a "better" structure for sweep line
    val columnToPerimeterPoints = TreeMap<Int, MutableSet<PerimeterPoint>>()
    for (perimeterPoint in perimeter) {
        // we sort the points by their row
        val currentSet: MutableSet<PerimeterPoint> =
            columnToPerimeterPoints.computeIfAbsent(perimeterPoint.col) { TreeSet<PerimeterPoint>(Comparator.comparing { it.row }) }
        currentSet.add(perimeterPoint)
    }

    // now we can easily sweep the line
    var result = 0L
    val partialRectangles = PartialRectangleCollection()

    for ((col, perimeterPoints) in columnToPerimeterPoints) {
        val perimeterPointList = ArrayList(perimeterPoints)
        val iterator: ListIterator<PerimeterPoint> = perimeterPointList.listIterator()

        assert(perimeterPoints.size >= 2)
        var top: PerimeterPoint = iterator.next()
        var bottom: PerimeterPoint
        var lastClosedBottomRow: Int = Integer.MIN_VALUE // this is required for determining skippable perimeter points
        while (iterator.hasNext()) {
            // we assume, that all cases handle the top perimeter point
            bottom = iterator.next()
            val tpRect = partialRectangles.getRectangleAtRow(top.row)
            val bpRect = partialRectangles.getRectangleAtRow(bottom.row)

            // easy case, no open rectangle, we just create a new one
            if (tpRect == null && bpRect == null) {
                partialRectangles.add(PartialRectangle(top.row, bottom.row, col))
                if (iterator.hasNext()) {
                    top = iterator.next()
                    assert(iterator.hasNext())
                }
                continue
            }

            // Concave on the inside, convex at the right outside
            if (tpRect != null && bpRect != null && tpRect == bpRect) {
                if (tpRect.tRow == top.row && tpRect.bRow == bottom.row) {
                    // if both perimeter points intersect with the same rectangle, and they are the right corners of it
                    // #####T
                    // .....#
                    // .....#
                    // #####B

                    // here we include the current column
                    result += tpRect.finalizeAtColumn(col) // we don't need any coordinates of this rectangle anymore
                } else if (tpRect.tRow != top.row && tpRect.bRow != bottom.row) {
                    // Concave finish with two new partial rectangles
                    // ###X###
                    // .......
                    // ...T###
                    // ...#
                    // ...B###
                    // .......
                    // ###X###
                    //
                    // Here we need to finalize the bigger rectangle on the left and start two new ones with the two
                    // artificial X perimeter points

                    // here we include the current column
                    result += tpRect.finalizeAtColumn(col)
                    // we start the rectangles one column further
                    partialRectangles.add(PartialRectangle(tpRect.tRow, top.row, col + 1))
                    partialRectangles.add(PartialRectangle(bottom.row, tpRect.bRow, col + 1))
                } else if (tpRect.tRow == top.row) { // && tpRect.bRow != bottom.row) {
                    // Curve at bottom
                    // ###T
                    // ...#
                    // ...#
                    // ...#
                    // ...B###
                    // .......
                    // ###X###
                    //
                    // Here we need to finalize the bigger rectangle on the left and start one new one with bottom at
                    // top and an artificial X perimeter point

                    // here we include the current column
                    result += tpRect.finalizeAtColumn(col)
                    // we start the rectangle one column further
                    partialRectangles.add(PartialRectangle(bottom.row, tpRect.bRow, col + 1))
                } else { // if(tpRect.tRow != top.row && tpRect.bRow == bottom.row)
                    // Curve at top
                    // ###X###
                    // .......
                    // ...T###
                    // ...#
                    // ...#
                    // ...#
                    // ###B
                    //
                    // Here we need to finalize the bigger rectangle on the left and start one new one with top at
                    // bottom and an artificial X perimeter point

                    // here we include the current column
                    result += tpRect.finalizeAtColumn(col)
                    // we start the rectangle one column further
                    partialRectangles.add(PartialRectangle(tpRect.tRow, top.row, col + 1))
                }

                // in all the above cases, we used up both top and bottom and need to fill the top before we can
                // get the bottom at the beginning of the main loop
                partialRectangles.remove(tpRect)
                if (iterator.hasNext()) {
                    top = iterator.next()
                    assert(iterator.hasNext())
                }
                continue
            }

            // Convex on the inside, concave on the left outside

            // first look at the top it is either a bottom left corner of an already existing open rectangle
            // or a top corner of a new
            if (tpRect != null) {
                assert(tpRect.bRow == top.row) // this should only be possible
                // Special case:
                // ####X###
                // #.......
                // ####T...
                //     #...
                //
                // Here we close the rectangle, but create a new top point (X), we don't know (yet), what to do with the
                // bottom. The new top point is destined to become the top left edge of a new rectangle.
                top = PerimeterPoint(tpRect.tRow, col)
                // here we exclude the current column
                result += tpRect.finalizeAtColumn(col - 1)
                partialRectangles.remove(tpRect)
            } else {
                // The top defines the top right corner of a new rectangle but the bottom is part of a different one
                //     T###
                //     #...
                //     #...
                // ####B...
                // #.......
                //
                // or
                //     T###
                //     #...
                //     #...
                //     B###
                // Here we do not need to do anything special, just keep the top
            }

            // now we consider the bottom
            // Multiples of these can occur
            // A    #...
            //      #...
            // #####B...
            // .........
            // #####N...
            //      #...
            //
            // Finalized by either this
            // B    #...
            //      #...
            //      B###
            //
            // or something like this
            // C    #...
            //      #...
            // #####B...
            // #........
            // #####X###
            //
            // Option A and C ar similar

            if (bpRect == null) {
                if (lastClosedBottomRow != bottom.row) {
                    // This is option B
                    // we take the previously saved top point and attach this bottom for opening a new rectangle
                    partialRectangles.add(PartialRectangle(top.row, bottom.row, col))
                }
                // The else case of this would be option A, where the corresponding rectangle has been closed one step
                // before. But in this case, we just ignore the point.
                if (iterator.hasNext()) {
                    top = iterator.next()
                    assert(iterator.hasNext())
                }
                continue
            }

            // assert(bpRect != null)
            // This is option A or C (we handle it in the same way)

            assert(bpRect.tRow == bottom.row)
            // here we exclude the current column
            result += bpRect.finalizeAtColumn(col - 1)
            partialRectangles.remove(bpRect)
            lastClosedBottomRow = bpRect.bRow // this is VERY important for the next iteration

            // distinguish between A and C ... in case of C a new rectangle has to be opened.
            var openRectangle = false
            if (!iterator.hasNext()) {
                openRectangle = true
            } else {
                val peek = iterator.next()
                iterator.previous() // turn it back
                if (peek.row != lastClosedBottomRow) {
                    // this happens, if there are more perimeter points below that are out of our current scope
                    openRectangle = true
                }
            }
            if (openRectangle) {
                // this is option C
                // the X is the row of the bpRect we just closed and this col
                partialRectangles.add(PartialRectangle(top.row, bpRect.bRow, col))
                if (iterator.hasNext()) {
                    top = iterator.next()
                    assert(iterator.hasNext())
                    continue
                }
            }
        }
    }
    return result
}

private class PartialRectangleCollection() {

    private val partialRectangles: MutableSet<PartialRectangle> = LinkedHashSet()

    fun getRectangleAtRow(row: Int): PartialRectangle? = partialRectangles.find { row in it.tRow..it.bRow }

    fun remove(pRectangle: PartialRectangle): Boolean = partialRectangles.remove(pRectangle)

    fun add(pRectangle: PartialRectangle): Boolean = partialRectangles.add(pRectangle)

    fun size() = partialRectangles.size

    fun isEmpty() = partialRectangles.isEmpty()

    fun isNotEmpty() = partialRectangles.isNotEmpty()

}

/**
 * Defines a partial rectangle. It is intended to be startet "from the left". In order to define this partial starting
 * state, the [tRow] (top row) and [bRow] (bottom row), as well, as the [lCol] (left column) have to be specified.
 */
private data class PartialRectangle(val tRow: Int, val bRow: Int, val lCol: Int) {

    /**
     * Closes this [PartialRectangle] at the given [rCol] right column. The result is the area.
     */
    fun finalizeAtColumn(rCol: Int): Long = (abs(bRow - tRow + 1).toLong() * abs(rCol - lCol + 1).toLong())

}

private data class PerimeterPoint(val row: Int, val col: Int)

private infix fun Int.at(col: Int) = PerimeterPoint(this, col)

private val deltaRight = 0 to 1
private val deltaLeft = 0 to -1
private val deltaUp = -1 to 0
private val deltaDown = 1 to 0
private val deltaInvalid = 0 to 0

private fun Char.getDelta(): Pair<Int, Int> = when (this) {
    'R' -> deltaRight
    'L' -> deltaLeft
    'U' -> deltaUp
    'D' -> deltaDown
    else -> deltaInvalid
}

private data class PerimeterResult(
    val perimeter: Set<PerimeterPoint>,
    val minRow: Int,
    val maxRow: Int,
    val minCol: Int,
    val maxCol: Int
)

private fun buildPerimeter(instructions: List<Instruction>, full: Boolean = true): PerimeterResult {
    val perimeter = LinkedHashSet<PerimeterPoint>()
    val start = 0 at 0
    perimeter.add(start)
    var minRow = start.row
    var minCol = start.col
    var maxRow = start.row
    var maxCol = start.col
    var row = start.row
    var col = start.col
    for (instruction in instructions) {
        val (char, steps) = instruction
        val (dRow, dCol) = char.getDelta()
        if (full) {
            for (i in 1..steps) {
                row += dRow
                col += dCol
                perimeter.add(row at col)
                minRow = minRow.coerceAtMost(row)
                minCol = minCol.coerceAtMost(col)
                maxRow = maxRow.coerceAtLeast(row)
                maxCol = maxCol.coerceAtLeast(col)
            }
        } else {
            row += dRow * steps
            col += dCol * steps
            perimeter.add(row at col)
            minRow = minRow.coerceAtMost(row)
            minCol = minCol.coerceAtMost(col)
            maxRow = maxRow.coerceAtLeast(row)
            maxCol = maxCol.coerceAtLeast(col)
        }
    }
    return PerimeterResult(perimeter, minRow, maxRow, minCol, maxCol)
}

private fun calculateAreaByFilling(instructions: List<Instruction>): Int {
    // create perimeter
    val (perimeter, minRow, maxRow, minCol, _) = buildPerimeter(instructions)

    // determine inside value
    val startRow = (maxRow + minRow) / 2
    var currentColumn = minCol

    // search until perimeter is hit
    while (startRow at currentColumn !in perimeter) {
        currentColumn += 1
    }

    // search until "not perimeter" is hit
    while (startRow at currentColumn in perimeter) {
        currentColumn += 1
    }

    val fillStart = startRow at currentColumn
    // no we fill it up
    val filled = HashSet<PerimeterPoint>().also { it.addAll(perimeter) }
    val buffer = ArrayDeque<PerimeterPoint>()
    buffer.addLast(fillStart)

    fun handleNext(up: PerimeterPoint) {
        if (up !in filled) {
            buffer.addLast(up)
        }
    }

    while (buffer.isNotEmpty()) {
        val current = buffer.removeLast()
        filled.add(current)
        handleNext(current.row - 1 at current.col) // up
        handleNext(current.row + 1 at current.col) // down
        handleNext(current.row at current.col - 1) // left
        handleNext(current.row at current.col + 1) // right
    }

    return filled.size
}

@OptIn(ExperimentalStdlibApi::class)
private fun parseInputPart1(lines: List<String>): Pair<List<Instruction>, List<Instruction>> {
    val result1 = ArrayList<Instruction>(lines.size)
    val result2 = ArrayList<Instruction>(lines.size)
    val spaceSplit = " ".toRegex()
    for (line in lines) {
        val split = spaceSplit.split(line)
        val char = split[0][0]
        val steps = split[1].toInt()
        result1.add(Instruction(char, steps))

        val remainder = split[2].trim('(', ')')
        val char2 = when (remainder[remainder.lastIndex]) {
            '0' -> 'R'
            '1' -> 'D'
            '2' -> 'L'
            '3' -> 'U'
            else -> 'X' // illegal
        }
        val steps2 = remainder.substring(1..<remainder.lastIndex).hexToInt()
        result2.add(Instruction(char2, steps2))
    }
    return result1 to result2
}

private data class Instruction(val char: Char, val steps: Int)