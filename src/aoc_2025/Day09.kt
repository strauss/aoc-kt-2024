package aoc_2025

import aoc_util.extractInts
import aoc_util.readInput2025
import aoc_util.solve
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.util.*
import kotlin.math.max
import kotlin.math.min

fun main() {
    val testLines = readInput2025("Day09_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::biggestRectSize)
    solve("Test 2 result", testInput, ::solveButSlow)
    solve("Test 2 altern", testInput, ::biggestLimitedRectSize)

    val lines = readInput2025("Day09")
    val input = parseInput(lines)
    solve("Result", input, ::biggestRectSize)
    solve("Result 2", input, ::biggestLimitedRectSize)
    solve("Result 2 slow", input, ::solveButSlow)
}

private fun solveButSlow(input: List<Pair<Int, Int>>): Long {
    val q = createRectangles(input)
    val maxX = input.asSequence().map { it.first }.max()
    val maxY = input.asSequence().map { it.second }.max()
    val perimeter = perimeter(input)
    var result = q.poll()
    while (!result.isValidWithRespectTo(input, perimeter, maxX, maxY)) {
        result = q.poll()
    }
    return result.area
}

private fun perimeter(input: List<Pair<Int, Int>>): HashSet<Pair<Int, Int>> {
    val points = HashSet<Pair<Int, Int>>()
    val first = input[0]
    var (xc, yc) = first
    fun process(next: Pair<Int, Int>) {
        val (xn, yn) = next
        while (xc != xn || yc != yn) {
            points.add(xc to yc)
            when {
                xc < xn -> xc += 1
                xc > xn -> xc -= 1
                yc < yn -> yc += 1
                else -> yc -= 1
            }
        }
    }
    for (i in 1..<input.size) {
        val next = input[i]
        process(next)
    }
    process(first)
    return points
}

private fun biggestLimitedRectSize(input: List<Pair<Int, Int>>): Long {
    val q = createRectangles(input)

    val path = Path2D.Float().also {
        it.windingRule = Path2D.WIND_NON_ZERO
        val (x0, y0) = input[0]
        it.moveTo(x0.toFloat(), y0.toFloat())
        for (i in 1..<input.size) {
            val (x, y) = input[i]
            it.lineTo(x.toFloat(), y.toFloat())
        }
        it.closePath()
    }
    val a = Area(path)

    while (q.isNotEmpty()) {
        val r = q.poll()
        val xx = r.tlX.toFloat()
        val yy = r.tlY.toFloat()
        val width = r.brX - xx
        val height = r.brY - yy
        val r2d = Rectangle2D.Float(xx, yy, width, height)
        if (a.contains(r2d)) {
            return r.area
        }
    }
    return -1L
}

private fun biggestRectSize(input: List<Pair<Int, Int>>): Long {
    val q = createRectangles(input)
    return q.peek().area
}

private fun createRectangles(input: List<Pair<Int, Int>>): PriorityQueue<Rect> {
    val comp = Comparator.comparing<Rect, Long> { it.area }
    val q = PriorityQueue(comp.reversed())
    for (i in 0..<input.size) {
        val (x1, y1) = input[i]
        for (j in i + 1..<input.size) {
            val (x2, y2) = input[j]
            val r = Rect(x1, y1, x2, y2)
            q.add(r)
        }
    }
    return q
}

class Rect(x1: Int, y1: Int, x2: Int, y2: Int) {
    val tlX = min(x1, x2)
    val tlY = min(y1, y2)
    val brX = max(x1, x2)
    val brY = max(y1, y2)
    val width
        get() = (brX.toLong() - tlX.toLong() + 1)
    val height
        get() = (brY - tlY + 1)
    val area: Long = width * height
    override fun toString(): String = "($tlX,$tlY)($brX, $brY)"

    fun containedIn(points: Set<Pair<Int, Int>>): Boolean {
        if (!points.contains(brX to brY)) {
            return false
        }
        for (y in tlY..brY) {
            for (x in tlX..brX) {
                if (!points.contains(x to y)) {
                    return false
                }
            }
        }
        return true
    }

    fun isValidWithRespectTo(
        redPoints: List<Pair<Int, Int>>,
        perimeter: HashSet<Pair<Int, Int>>,
        maxX: Int,
        maxY: Int
    ): Boolean {
        for (point in perimeter) {
            val (x, y) = point
            if (x in tlX + 1..brX - 1 && y in tlY + 1..brY - 1) {
                return false
            }
        }
        val topLeft = tlX to tlY
        val topRight = brX to tlY
        val bottomLeft = tlX to brY
        val bottomRight = brX to brY

        return hitPerimeter(topLeft, perimeter, maxX, maxY, deltaY = -1) &&
                hitPerimeter(topLeft, perimeter, maxX, maxY, deltaX = -1) &&
                hitPerimeter(topRight, perimeter, maxX, maxY, deltaY = -1) &&
                hitPerimeter(topRight, perimeter, maxX, maxY, deltaX = 1) &&
                hitPerimeter(bottomLeft, perimeter, maxX, maxY, deltaY = 1) &&
                hitPerimeter(bottomLeft, perimeter, maxX, maxY, deltaX = -1) &&
                hitPerimeter(bottomRight, perimeter, maxX, maxY, deltaY = 1) &&
                hitPerimeter(bottomRight, perimeter, maxX, maxY, deltaX = 1)
    }

    private fun hitPerimeter(
        point: Pair<Int, Int>,
        perimeter: HashSet<Pair<Int, Int>>,
        maxX: Int,
        maxY: Int,
        deltaX: Int = 0,
        deltaY: Int = 0
    ): Boolean {
        if (perimeter.contains(point)) {
            return true
        }
        if (deltaX == 0 && deltaY == 0) {
            return false
        }
        var (x, y) = point
        while (true) {
            x += deltaX
            y += deltaY
            if (perimeter.contains(x to y)) {
                return true
            }
            if (x < 0 || x > maxX || y < 0 || y > maxY) {
                return false
            }
        }
    }
}

private fun parseInput(lines: List<String>): List<Pair<Int, Int>> {
    return buildList {
        for (line in lines) {
            val ints = line.extractInts()
            add(ints[0] to ints[1])
        }
    }
}