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
    solve("Test 2 result", testInput, ::solve2ForReal)

    val lines = readInput2025("Day09")
    val input = parseInput(lines)
    solve("Result", input, ::biggestRectSize)
    solve("Result 2", input, ::solve2ForReal)
}

private fun solve2ForReal(input: List<Pair<Int, Int>>): Long {
    val maxX = input.asSequence().map { it.first }.max()
    val maxY = input.asSequence().map { it.second }.max()
    val perimeter = perimeter(input)
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
    var result = q.poll()
    while (!result.isValidWithRespectTo(input, perimeter, maxX, maxY)) {
        result = q.poll()
    }
    return result.area
}

private fun biggestLimitedRectSizeManual(input: List<Pair<Int, Int>>): Long {
    val perimeter = perimeter(input)
    val fillPoint = determineFillPoint(perimeter)
    fill(perimeter, fillPoint)

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

    var result = q.poll()
    while (!result.containedIn(perimeter)) {
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

private fun fill(points: MutableSet<Pair<Int, Int>>, fillPoint: Pair<Int, Int>) {
    val fq: Queue<Pair<Int, Int>> = LinkedList()
    fq.offer(fillPoint)
    while (fq.isNotEmpty()) {
        val current = fq.poll()
        if (points.contains(current)) {
            continue
        }
        points.add(current)
        val (x, y) = current
        fq.run {
            fun offerIf(element: Pair<Int, Int>) {
                if (!points.contains(element)) {
                    offer(element)
                }
            }
            offerIf(x + 1 to y)
            offerIf(x - 1 to y)
            offerIf(x to y + 1)
            offerIf(x to y - 1)
        }
    }
}

private fun determineFillPoint(points: Set<Pair<Int, Int>>): Pair<Int, Int> {
    val limitX = points.asSequence().map { it.first }.max()
    val limitY = points.asSequence().map { it.second }.max()

    val x = limitX / 2
    var crossed = false
    for (y in 1..limitY) {
        val current = x to y
        if (points.contains(current)) {
            crossed = true
        } else if (crossed) {
            return current
        }
    }
    return -1 to -1
}

private fun biggestLimitedRectSize(input: List<Pair<Int, Int>>): Long {
    val path = Path2D.Float().also {
        val (x0, y0) = input[0]
        it.moveTo(x0.toFloat(), y0.toFloat())
        for (i in 1..<input.size) {
            val (x, y) = input[i]
            it.lineTo(x.toFloat(), y.toFloat())
        }
        it.lineTo(x0.toFloat(), y0.toFloat())
        it.windingRule = Path2D.WIND_NON_ZERO
        it.closePath()
    }
    val a = Area(path)
    val comp = Comparator.comparing<Rect, Long> { it.area }
    val q = PriorityQueue(comp.reversed())
    for (i in 0..<input.size) {
        val (x1, y1) = input[i]
        for (j in i + 1..<input.size) {
            val (x2, y2) = input[j]
            val r = Rect(x1, y1, x2, y2)
            val r2d =
                Rectangle2D.Float(
                    r.tlX.toFloat() + 0.1f,
                    r.tlY.toFloat() + 0.1f,
                    r.width.toFloat() - 0.2f,
                    r.height.toFloat() - 0.2f
                )
            if (a.contains(r2d)) {
                q.add(r)
            }
        }
    }
    return q.peek().area
}

private fun biggestRectSize(input: List<Pair<Int, Int>>): Long {
    val comp = Comparator.comparing<Rect, Long> { it.area }
    val q = PriorityQueue(comp.reversed())
    for (i in 0..<input.size) {
        val (x1, y1) = input[i]
        for (j in i + 1..<input.size) {
            val (x2, y2) = input[j]
            q.add(Rect(x1, y1, x2, y2))
        }
    }
    return q.peek().area
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