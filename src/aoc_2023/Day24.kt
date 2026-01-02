package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day24_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput) {
        val input2D = testInput.map { it.makeIt2D() }
        countIntersections2D(input2D, 7.0..27.0)
    }

    val lines = readInput2023("Day24")
    val input = parseInput(lines)
    solve("Result", input) {
        val input2D = input.map { it.makeIt2D() }
        countIntersections2D(input2D, 200000000000000.0..400000000000000.0)
    }
}

private fun countIntersections2D(lines: List<Line2D>, range: ClosedFloatingPointRange<Double>): Int {
    var result = 0

    for (fidx in 0..lines.lastIndex) {
        for (sidx in fidx + 1..lines.lastIndex) {
            val firstLine = lines[fidx]
            val secondLine = lines[sidx]
            val det = firstLine.vx * secondLine.vy - firstLine.vy * secondLine.vx
            if (det == 0.0) {
                continue
            }
            val deltaX = secondLine.x - firstLine.x
            val deltaY = secondLine.y - firstLine.y

            val tFirst = (deltaX * secondLine.vy - deltaY * secondLine.vx) / det
            val tSecond = (deltaX * firstLine.vy - deltaY * firstLine.vx) / det

            if (tFirst < 0 || tSecond < 0) {
                continue
            }

            val intersectionX = firstLine.x + tFirst * firstLine.vx
            val intersectionY = firstLine.y + tFirst * firstLine.vy

            if (intersectionX in range && intersectionY in range) {
                result += 1
            }
        }
    }

    return result
}

private fun parseInput(lines: List<String>): List<Line3D> {
    val result = ArrayList<Line3D>(lines.size)

    val atSep = " @ ".toRegex()
    val commaSep = ", ".toRegex()

    for (line in lines) {
        val sep1 = atSep.split(line)
        val point = commaSep.split(sep1[0])
        val vector = commaSep.split(sep1[1])
        val line3D = Line3D(
            point[0].toDouble(),
            point[1].toDouble(),
            point[2].toDouble(),
            vector[0].toDouble(),
            vector[1].toDouble(),
            vector[2].toDouble()
        )
        result.add(line3D)
    }

    return result
}

private data class Line2D(val x: Double, val y: Double, val vx: Double, val vy: Double)

private data class Line3D(val x: Double, val y: Double, val z: Double, val vx: Double, val vy: Double, val vz: Double) {
    fun makeIt2D(): Line2D = Line2D(x, y, vx, vy)
}