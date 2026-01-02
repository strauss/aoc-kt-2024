package aoc_2023

import aoc_util.Vector
import aoc_util.gcd
import aoc_util.readInput2023
import aoc_util.solve
import kotlin.math.*

private val EPSILON: Double = 10.0.pow(-16.0)

fun main() {
    val testLines = readInput2023("Day24_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput) {
        val input2D = testInput.map { it.makeIt2D() }
        countIntersections2D(input2D, 7.0..27.0)
    }
    solve("Test 2 result", testInput, ::evaluateStoneLine)

    val lines = readInput2023("Day24")
    val input = parseInput(lines)
    solve("Result", input) {
        val input2D = input.map { it.makeIt2D() }
        countIntersections2D(input2D, 200000000000000.0..400000000000000.0)
    }

    solve("Result", input, ::evaluateStoneLine)


//
//    val p1: Vector = input[0].p
//    val v1: Vector = input[0].v
//    val p2: Vector = input[1].p
//    val v2: Vector = input[1].v
//    val np = p1 + v1
//    val op = p2 + (v2 * 2.0)
//
//    val nv = op - np
//    val newLine = Line3D(np, nv)
//    val i1 = newLine.intersects(input[0])
//    val i2 = newLine.intersects(input[1])
//    val i2a = input[1].intersects(newLine)
//    val i3 = newLine.intersects(input[2])

}

private fun evaluateStoneLine(input: List<Line3D>): Long {
    val result = findStoneLine(input)
    if (result != null) {
        val (stoneLine, intersections) = result
        val actualV = if (intersections[0].param2!! < intersections[1].param2!!) stoneLine.v else -stoneLine.v
        var firstIntersection = intersections[0]
        for (intersection in intersections) {
            if (intersection.param2!! < firstIntersection.param2!!) {
                firstIntersection = intersection
            }
        }
        val start =
            (firstIntersection.line2.p + (firstIntersection.line2.v * (firstIntersection.param2!!))) - (actualV * firstIntersection.param2)
        println("Velocity: $actualV")
        println("Start   : $start")
        return (start.x + start.y + start.z).toLong()
    }
    return -1L
}


private fun findStoneLine(input: List<Line3D>): Pair<Line3D, List<IntersectionInformation>>? {
    val probe1 = input[0]
    val probe2 = input[1]
    val p1 = probe1.p
    val v1 = probe1.v
    val p2 = probe2.p
    val v2 = probe2.v
    val max = input.size * 20 // this is arbitrary, we do not know the correct max

    var maxIntersections = Integer.MIN_VALUE
    for (t1 in 1..max) {
        val np = p1 + (v1 * t1.toDouble())
        for (t2 in 2..max) {
            if (t1 == t2) {
                continue
            }
            val op = p2 + (v2 * t2.toDouble())
            val nv = (op - np).reduce()
            val currentLine = Line3D(np, nv)
            var intersections = 2 // the two probe lines are always intersected
            for (idx in 2..input.lastIndex) {
                val result = currentLine.intersects(input[idx])
                if (result.intersects) {
                    intersections += 1
                }
            }
            maxIntersections = maxIntersections.coerceAtLeast(intersections)
            if (intersections == input.size) {
                val resultList = ArrayList<IntersectionInformation>()
                for (idx in input.indices) {
                    val result = currentLine.intersects(input[idx])
                    resultList.add(result)
                }
                return currentLine to resultList
            }
        }
    }
    println(maxIntersections)
    return null
}

private fun Vector.reduce(): Vector {
    val (x, y, z) = this
    if (floor(x) == x && floor(y) == y && floor(z) == z) {
        // if all are (long) integers in disguise
        var gcd = gcd(abs(x).toLong(), abs(y).toLong())
        gcd = gcd(gcd, abs(z).toLong())
        return Vector(x / gcd, y / gcd, z / gcd)
    } else {
        return this
    }
}

private data class IntersectionInformation(
//    val line1: Line3D,
    val line2: Line3D,
    val intersects: Boolean = false,
    val parallel: Boolean = false,
    val param1: Double? = null,
    val param2: Double? = null
)

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
        val p = Vector(
            point[0].trim().toDouble(),
            point[1].trim().toDouble(),
            point[2].trim().toDouble(),
        )
        val v = Vector(
            vector[0].trim().toDouble(),
            vector[1].trim().toDouble(),
            vector[2].trim().toDouble()
        )
        val line3D = Line3D(p, v)
        result.add(line3D)
    }

    return result
}

private data class Line2D(val x: Double, val y: Double, val vx: Double, val vy: Double)

private data class Line3D(val p: Vector, val v: Vector) {
    fun makeIt2D(): Line2D = Line2D(p.x, p.y, v.x, v.y)

    fun intersects(otherLine: Line3D): IntersectionInformation {
        val thisLine = this
        val thisP = thisLine.p
        val otherP = otherLine.p
        val r = otherP - thisP
        val thisV = thisLine.v
        val otherV = otherLine.v
        val n = thisV.cross(otherV)
        val nSquared = n * n
        val nSize = sqrt(nSquared)
//        if (n == Vector.ZERO) {
        if (nSize <= EPSILON) {
            // does not intersect and is parallel
            return IntersectionInformation(
//                thisLine,
                otherLine,
                parallel = true
            )
        }
        val rSquared = r * r
        val rSize = sqrt(rSquared)
//        if (r * n != 0.0) {
        val comp = (r * n) / (rSize * nSize)
        if (comp > EPSILON) {
            // does not intersect and is not parallel
            return IntersectionInformation(
//                thisLine,
                otherLine
            )
        }
        // intersects
        val param1 = (r.cross(otherV) * n) / nSquared
        val param2 = (r.cross(thisV) * n) / nSquared
        return IntersectionInformation(
//            thisLine,
            otherLine,
            intersects = true,
            param1 = param1.roundToLong().toDouble(),
            param2 = param2.roundToLong().toDouble()
        )
    }
}