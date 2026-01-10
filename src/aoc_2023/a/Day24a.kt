package aoc_2023.a

import aoc_util.DiscreteVector
import aoc_util.gcd
import aoc_util.readInput2023
import aoc_util.solve
import java.math.BigInteger
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

private val EPSILON: Double = 10.0.pow(-16.0)
private const val X_OFFSET = 8567975921442
private const val Y_OFFSET = 9079190797619
private const val Z_OFFSET = 11125812972389

fun main() {
    val testLines = readInput2023("Day24_test")
    val testInput = parseInput(testLines, offset = false)
    solve("Test result", testInput) {
        val input2D = testInput.map { it.makeIt2D() }
        countIntersections2D(input2D, 7.0..27.0)
    }
    solve("Test 2 result", testInput) {
        evaluateStoneLine(it, offset = false)
    }

    val lines = readInput2023("Day24")
    val input = parseInput(lines, offset = false)
    solve("Result", input) {
        val input2D = input.map { it.makeIt2D() }
        countIntersections2D(input2D, 200000000000000.0..400000000000000.0)
    }
    val oInput = parseInput(lines, offset = true)

//    solve("Result", oInput, ::evaluateStoneLine)

    val iRes = getBestCandidates(oInput)


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

private fun evaluateStoneLine(input: List<Line3D>, offset: Boolean = true): Long {
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
        val xOffset = if (offset) X_OFFSET else 0
        val yOffset = if (offset) Y_OFFSET else 0
        val zOffset = if (offset) Z_OFFSET else 0
        val earliestParam2 = firstIntersection.param2!!.roundToLong().toBigInteger() // TODO: this might be incorrect
        val result =
            (firstIntersection.line2.p + (firstIntersection.line2.v * earliestParam2)) - (actualV * earliestParam2)
        val start = result + DiscreteVector(xOffset.toBigInteger(), yOffset.toBigInteger(), zOffset.toBigInteger())
        println("Velocity: $actualV")
        println("Start   : $start")

        return (start.x + start.y + start.z).toLong()
    }
    return -1L
}

private fun getBestCandidates(input: List<Line3D>): Pair<Int, Int> {
    var minDistance = Double.POSITIVE_INFINITY
    var mindex1 = -1
    var mindex2 = -1
    var intersects = 0
    var parallel = 0
    for (idx1 in 0..input.lastIndex) {
        val currentFirstLine = input[idx1]
        for (idx2 in idx1 + 1..input.lastIndex) {
            val currentSecondLine = input[idx2]
            val currentResult = currentFirstLine.intersects(currentSecondLine)
            if (currentResult.parallel) {
                parallel += 1
                continue
            }
            if (currentResult.intersects) {
                intersects += 1
                continue
            }
            val currentDistance = currentResult.distance
            if (currentDistance < minDistance) {
                minDistance = currentDistance
                mindex1 = idx1
                mindex2 = idx2
            }
        }
    }
    return mindex1 to mindex2
}

private fun findStoneLine(input: List<Line3D>): Pair<Line3D, List<IntersectionInformation>>? {
//    val (idx1, idx2) = getBestCandidates(input)
    val idx1 = 0
    val idx2 = 1
    val probe1 = input[idx1]
    val probe2 = input[idx2]
    val debug = probe1.intersects(probe2)
    val p1 = probe1.p
    val v1 = probe1.v
    val p2 = probe2.p
    val v2 = probe2.v
    val max = input.size * 20 // this is arbitrary, we do not know the correct max

    var maxIntersections = Integer.MIN_VALUE
    for (t1 in 0..max) {
        val np = p1 + (v1 * t1.toBigInteger())
        for (t2 in 0..max) {
            if (t1 == t2) {
                continue
            }
            val op = p2 + (v2 * t2.toBigInteger())
            val nv = (op - np).reduce()
            val currentLine = Line3D(np, nv)
            var validIntersections = 2 // the two probe lines are always intersected
            for (idx in 0..input.lastIndex) {
                if (idx == idx1 || idx == idx2) {
                    continue
                }
                val result = currentLine.intersects(input[idx])
                if (result.intersects) { // && result.param1 != 0.0 && result.param2 != 0.0) {
                    validIntersections += 1
                }
            }
            maxIntersections = maxIntersections.coerceAtLeast(validIntersections)
            if (validIntersections == input.size) {
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

fun DiscreteVector.reduce(): DiscreteVector {
    val (x, y, z) = this
    var gcd = gcd(x.abs(), y.abs())
    gcd = gcd(gcd, z.abs())
    if (gcd > BigInteger.ONE) {
        return DiscreteVector(x / gcd, y / gcd, z / gcd)
    }
    return this
}

private data class IntersectionInformation(
//    val line1: Line3D,
    val line2: Line3D,
    val distance: Double,
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

private fun parseInput(lines: List<String>, offset: Boolean = true): List<Line3D> {
    val result = ArrayList<Line3D>(lines.size)

    val atSep = " @ ".toRegex()
    val commaSep = ", ".toRegex()

    val xOffset = if (offset) X_OFFSET else 0
    val yOffset = if (offset) Y_OFFSET else 0
    val zOffset = if (offset) Z_OFFSET else 0

    for (line in lines) {
        val sep1 = atSep.split(line)
        val point = commaSep.split(sep1[0])
        val vector = commaSep.split(sep1[1])
        val p = DiscreteVector(
            point[0].trim().toBigInteger() - xOffset.toBigInteger(),
            point[1].trim().toBigInteger() - yOffset.toBigInteger(),
            point[2].trim().toBigInteger() - zOffset.toBigInteger(),
        )
        val v = DiscreteVector(
            vector[0].trim().toBigInteger(),
            vector[1].trim().toBigInteger(),
            vector[2].trim().toBigInteger()
        )
        val line3D = Line3D(p, v)
        result.add(line3D)
    }

//    analyzeInput(result)

    return result
}

private data class Line2D(val x: Double, val y: Double, val vx: Double, val vy: Double)

private data class Line3D(val p: DiscreteVector, val v: DiscreteVector) {
    fun makeIt2D(): Line2D = Line2D(p.x.toDouble(), p.y.toDouble(), v.x.toDouble(), v.y.toDouble())

    fun intersects(otherLine: Line3D): IntersectionInformation {
        val thisLine = this
        val thisP = thisLine.p
        val otherP = otherLine.p
        val r = otherP - thisP
        val thisV = thisLine.v
        val otherV = otherLine.v
        val n = thisV.cross(otherV)
        val nSquared = n * n
        val nSize = sqrt(nSquared.toDouble())
//        if (n == Vector.ZERO) {
//        if (nSize <= EPSILON) {
        if (n == DiscreteVector.ZERO) {
            // does not intersect and is parallel
            val rCrossV = r.cross(thisV)
            val rCrossVSquared = rCrossV * rCrossV
            val rCrossVSize = sqrt(rCrossVSquared.toDouble())
            val thisVSquared = v * v
            val thisVSize = sqrt(thisVSquared.toDouble())
            val parallelDistance = rCrossVSize / thisVSize
            return IntersectionInformation(
//                thisLine,
                otherLine,
                parallelDistance,
                parallel = true
            )
        }
//        val rSquared = r * r
//        val rSize = sqrt(rSquared.toDouble())
        val rTimesN = r * n
//        val comp = rTimesN / (rSize * nSize)
//        if (comp > EPSILON) {
        if (rTimesN != BigInteger.ZERO) {
            // does not intersect and is not parallel
            val distance = rTimesN.toDouble() / nSize
            return IntersectionInformation(
//                thisLine,
                otherLine,
                distance
            )
        }
        // intersects
        val param1 = (r.cross(otherV) * n).toDouble() / nSquared.toDouble()
        val param2 = (r.cross(thisV) * n).toDouble() / nSquared.toDouble()
        return IntersectionInformation(
//            thisLine,
            otherLine,
            0.0, // the distance is normalized to 0.0 if they intersect
            intersects = true,
            param1 = param1, //.roundToLong().toDouble(),
            param2 = param2 //.roundToLong().toDouble()
        )
    }
}