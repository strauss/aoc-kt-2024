package aoc_2023

import aoc_util.Primitive2DCharArray
import aoc_util.readInput2023
import aoc_util.solve
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.util.BitSet
import kotlin.collections.ArrayDeque

private const val connectNorth = "|LJ"
private const val connectEast = "-LF"
private const val connectSouth = "|7F"
private const val connectWest = "-J7"

private const val scale = 10

fun main() {
    val testLines = readInput2023("Day10_test")
    val testArray = parseAsArray(testLines)
    solve("Test result", testArray, ::solve1)
    val lines2 = readInput2023("Day10_test_a")
    val array2 = parseAsArray(lines2)
    solve("Test 2 result", array2, ::solve2)

    val lines = readInput2023("Day10")
    val array = parseAsArray(lines)
    solve("Result", array, ::solve1)
    solve("Result 2", array, ::solve2)
}

private fun solve2(array: Primitive2DCharArray): Int {
    val start = findStart(array)
    // create path2D -> area
    val search = ArraySearch(array, SearchType.DFS)
    val visitor = PerimeterVisitor(array.height, array.width)
    search.execute(start, visitor)
    val perimeter = visitor.perimeter
    val onPerimeter = visitor.visited
    val checkArea = Area(perimeter)

//    val img = BufferedImage(array.width * scale, array.height * scale, BufferedImage.TYPE_INT_RGB)
//    (img.graphics as Graphics2D).run {
//        color = Color.red
//        draw(checkArea)
//    }
//    ImageIO.write(img, "PNG", File("out.png"))

    val arrayOut = Primitive2DCharArray(array.height, array.width)
    var result = 0
    // then create a small square for each cell and count those inside... ignore those on the perimeter
    for (row in 0..<array.height) {
        for (col in 0..<array.width) {
            if (onPerimeter[row][col]) {
                arrayOut[row, col] = array[row, col]
                continue
            }
            val r2d = Rectangle2D.Float(col.toFloat() * scale, row.toFloat() * scale, scale.toFloat(), scale.toFloat())
            if (checkArea.contains(r2d)) {
                result += 1
                arrayOut[row, col] = 'O'
            } else {
                arrayOut[row, col] = '.'
            }
        }
    }

    println(arrayOut)

    return result
}

private interface Day10Visitor {
    fun visitStart(start: Pair<Int, Int>)
    fun visit(cell: Pair<Int, Int>)
    fun done()
}

private class PerimeterVisitor(height: Int, val width: Int) : Day10Visitor {
    val visited = Array(height) { BitSet(width) }
    val perimeter = Path2D.Float()

    override fun visitStart(start: Pair<Int, Int>) {
        visited[start] = true
        val (row, col) = start
        perimeter.moveTo(col.toFloat() * scale, row.toFloat() * scale)
    }

    override fun visit(cell: Pair<Int, Int>) {
        visited[cell] = true
        val (row, col) = cell
        println("row: $row | col: $col")
        perimeter.lineTo(col.toFloat() * scale, row.toFloat() * scale)
    }

    override fun done() {
        perimeter.closePath()
    }
}

private enum class SearchType {
    BFS, DFS
}

private class ArraySearch(val array: Primitive2DCharArray, val type: SearchType = SearchType.BFS) {
    val depth = HashMap<Pair<Int, Int>, Int>()
    var maxDepth = 0

    fun execute(start: Pair<Int, Int>, visitor: Day10Visitor? = null) {
        depth.clear()
        val (rowS, colS) = start

        val currentDepth = 0
        depth[start] = currentDepth
        visitor?.visitStart(start)

        val q = ArrayDeque<Pair<Int, Int>>()

        val nextStates = ArrayList<Pair<Int, Int>>()
        // check north
        val rowNorth = rowS - 1
        if (rowNorth >= 0 && array[rowNorth, colS] in connectSouth) {
            nextStates.add(rowNorth to colS)
        }
        // check east
        val colEast = colS + 1
        if (colEast < array.width && array[rowS, colEast] in connectWest) {
            nextStates.add(rowS to colEast)
        }
        // check south
        val rowSouth = rowS + 1
        if (rowSouth < array.height && array[rowSouth, colS] in connectNorth) {
            nextStates.add(rowSouth to colS)
        }
        // check west
        val colWest = colS - 1
        if (colWest >= 0 && array[rowS, colWest] in connectEast) {
            nextStates.add(rowS to colWest)
        }

        when (type) {
            SearchType.BFS -> {
                for (idx in 0..<nextStates.size) {
                    val nextState = nextStates[idx]
                    depth[nextState] = 1
                    visitor?.visit(nextState)
                    q.addLast(nextState)
                }
            }

            SearchType.DFS -> {
                // when DFS, we only want one state
                val nextState = nextStates[0]
                depth[nextState] = 1
                visitor?.visit(nextState)
                q.addLast(nextStates[0])
            }
        }

        // start search
        while (q.isNotEmpty()) {
            val current = when (type) {
                SearchType.BFS -> q.removeFirst()
                SearchType.DFS -> q.removeLast()
            }
            val currentDepth = depth[current] ?: 0
            maxDepth = maxDepth.coerceAtLeast(currentDepth)
            val (row, col) = current
            val currentChar = array[row, col]
            if (currentChar in connectNorth) {
                val north = (row - 1) to col
                if (depth[north] == null) {
                    depth[north] = currentDepth + 1
                    visitor?.visit(north)
                    q.addLast(north)
                }
            }
            if (currentChar in connectEast) {
                val east = row to col + 1
                if (depth[east] == null) {
                    depth[east] = currentDepth + 1
                    visitor?.visit(east)
                    q.addLast(east)
                }
            }
            if (currentChar in connectSouth) {
                val south = row + 1 to col
                if (depth[south] == null) {
                    depth[south] = currentDepth + 1
                    visitor?.visit(south)
                    q.addLast(south)
                }
            }
            if (currentChar in connectWest) {
                val west = row to col - 1
                if (depth[west] == null) {
                    depth[west] = currentDepth + 1
                    visitor?.visit(west)
                    q.addLast(west)
                }
            }
        }
        visitor?.done()
    }
}

private fun solve1(array: Primitive2DCharArray): Int {
    // search the S
    val start = findStart(array)

    val search = ArraySearch(array)
    search.execute(start)

    return search.maxDepth
}

private operator fun Array<BitSet>.set(index: Pair<Int, Int>, value: Boolean) {
    val (row, col) = index
    this[row][col] = value
}

private fun findStart(array: Primitive2DCharArray): Pair<Int, Int> {
    for (row in 0..<array.height) {
        for (col in 0..<array.width) {
            if (array[row, col] == 'S') {
                return row to col
            }
        }
    }
    return -1 to -1
}


private fun parseAsArray(lines: List<String>) = Primitive2DCharArray.parseFromLines(lines, '.')

