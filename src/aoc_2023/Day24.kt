package aoc_2023

import aoc_util.Vector
import aoc_util.gcd
import aoc_util.readInput2023
import aoc_util.solve
import kotlin.math.*

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

    solve("Result", oInput) {
        evaluateStoneLineFast(it, offset = true)
    }

//    val newStoneLine = findStoneLineFast(oInput)


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
    val (idx1, idx2) = getBestCandidates(input)
    val probe1 = input[idx1]
    val probe2 = input[idx2]
    return internalEvaluateStoneLine(result, probe1, probe2, offset)
}

private fun evaluateStoneLineFast(input: List<Line3D>, offset: Boolean = true): Long {
    val (idx1, idx2) = getBestCandidates(input)
    val probe1 = input[idx1]
    val probe2 = input[idx2]
    val result = findStoneLineFast(input, probe1, probe2)
    return internalEvaluateStoneLine(result, probe1, probe2, offset)
}

private fun internalEvaluateStoneLine(
    result: Pair<Line3D, List<IntersectionInformation>>?,
    probe1: Line3D,
    probe2: Line3D,
    offset: Boolean
): Long {
    if (result != null) {
        val (stoneLine, intersections) = result
        val probe1Intersection = intersections.find { it.line2 == probe1 }!!
        val probe2Intersection = intersections.find { it.line2 == probe2 }!!
        val actualV = if (probe1Intersection.param2!! < probe2Intersection.param2!!) stoneLine.v else -stoneLine.v
        var firstIntersection = probe1Intersection
        for (intersection in intersections) {
            if (intersection.param2 != null) {
                if (intersection.param2 < firstIntersection.param2!!) {
                    firstIntersection = intersection
                }
            }
        }
        val xOffset = if (offset) X_OFFSET else 0
        val yOffset = if (offset) Y_OFFSET else 0
        val zOffset = if (offset) Z_OFFSET else 0
        val earliestParam2 = firstIntersection.param2!!.roundToLong().toDouble()
        val result =
            (firstIntersection.line2.p + (firstIntersection.line2.v * earliestParam2)) - (actualV * earliestParam2)
        val start = result + Vector(xOffset.toDouble(), yOffset.toDouble(), zOffset.toDouble())
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

private fun findStoneLineFast(
    input: List<Line3D>,
    probe1: Line3D,
    probe2: Line3D
): Pair<Line3D, List<IntersectionInformation>>? {
    /*
     * Idea:
     * - Instead of counting the intersections measure the sum of "errors" (distances) of all intersections
     * - Move both indexes forward until a "sweet spot" is hit
     * - Move the indexes independently, one at a time
     *    - Search greedy towards the lowest error in all four directions
     *    - When the error hits 0, we are done and have found the "stone line"
     */

    val (p1, v) = probe1
    val (p2, w) = probe2

    fun getIntersections(line: Line3D): List<IntersectionInformation> = buildList {
        for (idx in input.indices) {
            add(line.intersects(input[idx]))
        }
    }

    val (t1d, t2d, d1, d2) = determineStartParameters(probe1, probe2)

    var t1 = 914447960840//t1d
    var t2 = 914276650550//t2d
    var delta1 = d1
    var delta2 = d2

    fun assembleCurrentLine(t1: Long, t2: Long): Line3D {
        val np = p1 + (v * t1.toDouble())
        val op = p2 + (w * t2.toDouble())
        val nv = (op - np).reduce()
        val currentLine = Line3D(np, nv)
        return currentLine
    }

    // first we search for a good starting point
    // we move forward with t1 and t2 simultaneously until we hit a local minimum
//    var delta = 100_000_000L
    fun calculateDistances(t1: Long, t2: Long): Double {
        val currentLine = assembleCurrentLine(t1, t2)
        val intersections = getIntersections(currentLine)
        val nextSumOfDistances = intersections.sumOf { it.distance }
        return nextSumOfDistances
    }

    var currentBestSumOfDistances = calculateDistances(t1, t2)

    while (true) {
//        val both = calculateDistances(t1 + delta1, t2 + delta2)
        val onlyFirst = calculateDistances(t1 + delta1, t2)
        val onlySecond = calculateDistances(t1, t2 + delta2)
        val min = minOf(
//            both,
            onlyFirst, onlySecond
        )

        if (min < currentBestSumOfDistances) {
            currentBestSumOfDistances = min
            when (min) {
//                both -> {
//                    t1 += delta1
//                    t2 += delta2
//                }
                onlyFirst -> t1 += delta1
                onlySecond -> t2 += delta2
            }
        } else {
            break
        }
    }

    println(t1)
    println(t2)

    val stoneLine = assembleCurrentLine(t1, t2)
    val intersections = getIntersections(stoneLine)

    return stoneLine to intersections
}

private data class StartConfiguration(val p1: Long, val p2: Long, val d1: Long, val d2: Long)

private fun determineStartParameters(probe1: Line3D, probe2: Line3D): StartConfiguration {
    val (p1, v) = probe1
    val (p2, w) = probe2
    // determine closest points on both lines and use their vectors as initial parameters
//    val intersectProbes = probe1.intersects(probe2)
    val r = p1 - p2
    val a = v * v
    val b = v * w
    val c = w * w
    val d = v * r
    val e = w * r
    val det = a * c - b * b
    val t1d = ((b * e - c * d) / det).roundToLong() - 39965742L // values determined by experimentation
    val t2d = ((a * e - b * d) / det).roundToLong() + 8302794L


//
//    val np = p1 + (v * t1d.toDouble())
//    val op = p2 + (w * t2d.toDouble())
//    val nv = (op - np).reduce()
//    val currentLine = Line3D(np, nv)
//
//    val intersections = getIntersections(currentLine)
//    val nextSumOfDistances = intersections.sumOf { it.distance }
//    val intersectionCount = intersections.count { it.intersects }
    return StartConfiguration(t1d, t2d, -1L, 1L)
}

private fun findStoneLine(input: List<Line3D>): Pair<Line3D, List<IntersectionInformation>>? {
    val (idx1, idx2) = getBestCandidates(input)
    val probe1 = input[idx1]
    val probe2 = input[idx2]
    val debug = probe1.intersects(probe2)
    val p1 = probe1.p
    val v1 = probe1.v
    val p2 = probe2.p
    val v2 = probe2.v

    val max = input.size * 10 // this is arbitrary, we do not know the correct max

    var maxIntersections = Integer.MIN_VALUE
    for (t1 in 0..max) {
        val np = p1 + (v1 * t1.toDouble())
        mainSearch@ for (t2 in 0..max) {
            if (t1 == t2) {
                continue
            }
            val op = p2 + (v2 * t2.toDouble())
            val nv = (op - np).reduce()
            val currentLine = Line3D(np, nv)
            var validIntersections = 2 // the two probe lines are always intersected
            for (idx in 0..input.lastIndex) {
                if (idx == idx1 || idx == idx2) {
                    continue
                }
                val result = currentLine.intersects(input[idx])
                if (result.intersects && result.param1 != 0.0 && result.param2 != 0.0) {
                    validIntersections += 1
                }
            }
            maxIntersections = maxIntersections.coerceAtLeast(validIntersections)
            if (validIntersections == input.size) {
                val resultList = ArrayList<IntersectionInformation>()
                for (idx in input.indices) {
                    val result = currentLine.intersects(input[idx])
                    if (
                        abs(result.param1!!.roundToLong().toDouble() - result.param1) > 0.01 ||
                        abs(result.param2!!.roundToLong().toDouble() - result.param2) > 0.01
                    ) {
                        continue@mainSearch
                    }
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

internal data class IntersectionInformation(
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
        val p = Vector(
            point[0].trim().toDouble() - xOffset,
            point[1].trim().toDouble() - yOffset,
            point[2].trim().toDouble() - zOffset,
        )
        val v = Vector(
            vector[0].trim().toDouble(),
            vector[1].trim().toDouble(),
            vector[2].trim().toDouble()
        )
        val line3D = Line3D(p, v)
        result.add(line3D)
    }

//    analyzeInput(result)

    return result
}

private fun analyzeInput(input: List<Line3D>) {
    var minX = Double.POSITIVE_INFINITY
    var minY = Double.POSITIVE_INFINITY
    var minZ = Double.POSITIVE_INFINITY

    for (line in input) {
        minX = minX.coerceAtMost(line.p.x)
        minY = minY.coerceAtMost(line.p.y)
        minZ = minZ.coerceAtMost(line.p.z)
    }

    println("MinX: ${minX.toLong()}")
    println("MinY: ${minY.toLong()}")
    println("MinZ: ${minZ.toLong()}")
}

internal data class Line2D(val x: Double, val y: Double, val vx: Double, val vy: Double)

internal data class Line3D(val p: Vector, val v: Vector) {
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
            val rCrossV = r.cross(thisV)
            val rCrossVSquared = rCrossV * rCrossV
            val rCrossVSize = sqrt(rCrossVSquared)
            val thisVSquared = v * v
            val thisVSize = sqrt(thisVSquared)
            val parallelDistance = rCrossVSize / thisVSize
            return IntersectionInformation(
//                thisLine,
                otherLine,
                parallelDistance,
                parallel = true
            )
        }
        val rSquared = r * r
        val rSize = sqrt(rSquared)
//        if (r * n != 0.0) {
        val rTimesN = r * n
        val comp = rTimesN / (rSize * nSize)
        if (comp > EPSILON) {
            // does not intersect and is not parallel
            val distance = rTimesN / nSize
            return IntersectionInformation(
//                thisLine,
                otherLine,
                distance
            )
        }
        // intersects
        val param1 = (r.cross(otherV) * n) / nSquared
        val param2 = (r.cross(thisV) * n) / nSquared
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