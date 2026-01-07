package aoc_2023

import aoc_util.*
import com.google.ortools.Loader
import com.google.ortools.sat.*
import org.apache.commons.math3.linear.MatrixUtils
import java.math.BigInteger
import kotlin.math.sqrt

// Example solution
// Velocity: Vector(x=-3.0, y=1.0, z=2.0)
// Start   : Vector(x=24.0, y=13.0, z=10.0)

fun main() {
//    trials()

    val testLines = readInput2023("Day24")
    val testInput = parseInput(testLines)
    findStoneLine(testInput)

}

private fun trials() {
    val line1 = DiscreteLine3D(
        DiscreteVector(19.toBigInteger(), 13.toBigInteger(), 30.toBigInteger()),
        DiscreteVector((-2).toBigInteger(), 1.toBigInteger(), (-2).toBigInteger())
    )
    val line2 = DiscreteLine3D(
        DiscreteVector(24.toBigInteger(), 13.toBigInteger(), 10.toBigInteger()),
        DiscreteVector((-3).toBigInteger(), 1.toBigInteger(), 2.toBigInteger())
    )
    println(line1 intersects line2)
    val result = line1 intersect line2
    val point = line1.p.asVector() + (line1.v * result.param1!!)
    println(point)

    val line1d = line1.asLine3D()
    val line2d = line2.asLine3D()

    val resultd = line1d.intersects(line2d)
    val pointd = line1d.p + (line1d.v * resultd.param1!!)
    println(pointd)
}

private fun findStoneLine(input: List<DiscreteLine3D>): Pair<DiscreteLine3D, List<IntersectionInformation>>? {
    Loader.loadNativeLibraries()

    val (d, m) = getLineWithApacheMath(input)
//    val (d, m) = getLineWithOrTools(input)

    val s = d * d
    val u = d.cross(m)

//    val result = Vector(
//        u.x.toDouble() / s.toDouble(),
//        u.y.toDouble() / s.toDouble(),
//        u.z.toDouble() / s.toDouble()
//    )
//
//    println(result)


    val kModel = CpModel()
    val x = kModel.newIntVar(0.toLong(), s.toLong() - 1L, "x")
    val k1 = kModel.newIntVar(0, Int.MAX_VALUE.toLong(), "k1")
    val k2 = kModel.newIntVar(0, Int.MAX_VALUE.toLong(), "k2")
    val k3 = kModel.newIntVar(0, Int.MAX_VALUE.toLong(), "k3")

    run {
        val expr = LinearExpr.newBuilder()
        expr.addTerm(x, d.x.toLong())
        expr.addTerm(k1, -(s.toLong()))
        kModel.addEquality(expr, -(u.x.toLong()))
    }

    run {
        val expr = LinearExpr.newBuilder()
        expr.addTerm(x, d.y.toLong())
        expr.addTerm(k2, -(s.toLong()))
        kModel.addEquality(expr, -(u.y.toLong()))
    }

    run {
        val expr = LinearExpr.newBuilder()
        expr.addTerm(x, d.z.toLong())
        expr.addTerm(k3, -(s.toLong()))
        kModel.addEquality(expr, -(u.z.toLong()))
    }

    val kSolver = CpSolver()
    val kStatus = kSolver.solve(kModel)

    val k = kSolver.value(x)

    val p = (u + (d * k.toBigInteger()))
    val kResult = DiscreteVector(p.x / s, p.y / s, p.z / s)

    println("Velocity: $d")
    println("Point: $kResult")


//    p = (u + kd) / s


    return null
}

private fun getLineWithApacheMath(input: List<DiscreteLine3D>): Pair<DiscreteVector, DiscreteVector> {
    val inputAsCoefficients: Array<DoubleArray> = input.asSequence().take(5).map {
        doubleArrayOf(
            it.d.x.toDouble(),
            it.d.y.toDouble(),
            it.d.z.toDouble(),
            it.m.x.toDouble(),
            it.m.y.toDouble(),
            it.m.z.toDouble()
        )
    }.toList().toTypedArray()

    val lgs = MatrixUtils.createRealMatrix(inputAsCoefficients)

//    val base = if (input.size < 6) nullSpaceLessEquationsThanDimensions(lgs) else nullSpace(lgs)
    val base = nullSpaceLessEquationsThanDimensions(lgs)

    val solution = base.first()
    val scaledSolution = scaleToInteger(solution)

    val d = DiscreteVector(scaledSolution[3], scaledSolution[4], scaledSolution[5])
    val m = DiscreteVector(scaledSolution[0], scaledSolution[1], scaledSolution[2])

    return d to m
}

private fun getLineWithOrTools(input: List<DiscreteLine3D>): Pair<DiscreteVector, DiscreteVector> {
    val model = CpModel()
    val dx: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "dx")
    val dy: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "dy")
    val dz: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "dz")
    val mx: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "mx")
    val my: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "my")
    val mz: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "mz")

    val px: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "px")
    val py: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "py")
    val pz: IntVar = model.newIntVar(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong(), "pz")

    model.addMultiplicationEquality(px, dx, mx)
    model.addMultiplicationEquality(py, dy, my)
    model.addMultiplicationEquality(pz, dz, mz)
    model.addEquality(LinearExpr.sum(arrayOf(px, py, pz)), 0)
    model.addForbiddenAssignments(arrayOf(dx, dy, dz)).addTuples(arrayOf(longArrayOf(0L, 0L, 0L)))


    for (idx in input.indices) {
        val line = input[idx]
        val d = line.d
        val m = line.m
        val expr = LinearExpr.newBuilder()
        expr.addTerm(dx, d.x.toLong())
        expr.addTerm(dy, d.y.toLong())
        expr.addTerm(dz, d.z.toLong())
        expr.addTerm(mx, m.x.toLong())
        expr.addTerm(my, m.y.toLong())
        expr.addTerm(mz, m.z.toLong())
        model.addEquality(expr, 0L)
    }

    val solver = CpSolver()
    val status = solver.solve(model)

    when (status) {
        CpSolverStatus.OPTIMAL, CpSolverStatus.FEASIBLE -> {
            println("Status: $status")
            println("d = (${solver.value(dx)}, ${solver.value(dy)}, ${solver.value(dz)})")
            println("m = (${solver.value(mx)}, ${solver.value(my)}, ${solver.value(mz)})")
        }

        else -> println("No integer solution found for status $status")
    }

    val v = DiscreteVector(
        solver.value(mx).toBigInteger(),
        solver.value(my).toBigInteger(),
        solver.value(mz).toBigInteger()
    ) // I know, it should be d, but who cares?
    val actualM = DiscreteVector(
        solver.value(dx).toBigInteger(),
        solver.value(dy).toBigInteger(),
        solver.value(dz).toBigInteger()
    )
    return Pair(v, actualM)
}

private data class DiscreteIntersectionInformation(
//    val line1: Line3D,
    val line2: DiscreteLine3D,
    val distance: Double,
    val intersects: Boolean = false,
    val parallel: Boolean = false,
    val param1: Double? = null,
    val param2: Double? = null
)

private data class DiscreteLine3D(val p: DiscreteVector, val v: DiscreteVector) {
    val d
        get() = v
    val m = p.cross(v)

    infix fun intersects(otherLine: DiscreteLine3D): Boolean = d * otherLine.m + otherLine.d * m == BigInteger.ZERO

    infix fun parallelTo(otherLine: DiscreteLine3D): Boolean = d.cross(otherLine.d) == DiscreteVector.ZERO

    infix fun intersect(otherLine: DiscreteLine3D): DiscreteIntersectionInformation {
        val thisLine = this
        val thisP = thisLine.p
        val otherP = otherLine.p
        val r = otherP - thisP
        val thisV = thisLine.v
        val otherV = otherLine.v
        val n = thisV.cross(otherV)
        val nSquared = n * n
        val nSize = sqrt(nSquared.toDouble())
        if (n == DiscreteVector.ZERO) {
            // does not intersect and is parallel
            val rCrossV = r.cross(thisV)
            val rCrossVSquared = rCrossV * rCrossV
            val rCrossVSize = sqrt(rCrossVSquared.toDouble())
            val thisVSquared = v * v
            val thisVSize = sqrt(thisVSquared.toDouble())
            val parallelDistance = rCrossVSize / thisVSize
            return DiscreteIntersectionInformation(
//                thisLine,
                otherLine,
                parallelDistance,
                parallel = true
            )
        }
//        val rSquared = r * r
//        val rSize = sqrt(rSquared.toDouble())
        val rTimesN = r * n
        if (rTimesN != BigInteger.ZERO) {
            // does not intersect and is not parallel
            val distance = rTimesN.toDouble() / nSize
            return DiscreteIntersectionInformation(
//                thisLine,
                otherLine,
                distance
            )
        }
        // intersects
        val param1 = (r.cross(otherV) * n).toDouble() / nSquared.toDouble()
        val param2 = (r.cross(thisV) * n).toDouble() / nSquared.toDouble()
        return DiscreteIntersectionInformation(
//            thisLine,
            otherLine,
            0.0, // the distance is normalized to 0.0 if they intersect
            intersects = true,
            param1 = param1, //.roundToLong().toDouble(),
            param2 = param2 //.roundToLong().toDouble()
        )
    }

    fun asLine3D() = Line3D(p.asVector(), v.asVector())

}

private fun DiscreteVector.reduce(): DiscreteVector {
    val (x, y, z) = this
    var gcd = gcd(x.abs(), y.abs())
    gcd = gcd(gcd, z.abs())
    if (gcd > BigInteger.ONE) {
        return DiscreteVector(x / gcd, y / gcd, z / gcd)
    }
    return this
}

private fun parseInput(lines: List<String>): List<DiscreteLine3D> {
    val result = ArrayList<DiscreteLine3D>(lines.size)

    val atSep = " @ ".toRegex()
    val commaSep = ", ".toRegex()

    for (line in lines) {
        val sep1 = atSep.split(line)
        val point = commaSep.split(sep1[0])
        val vector = commaSep.split(sep1[1])
        val p = DiscreteVector(
            point[0].trim().toBigInteger(),
            point[1].trim().toBigInteger(),
            point[2].trim().toBigInteger(),
        )
        val v = DiscreteVector(
            vector[0].trim().toBigInteger(),
            vector[1].trim().toBigInteger(),
            vector[2].trim().toBigInteger()
        )
        val line3D = DiscreteLine3D(p, v)
        result.add(line3D)
    }
    return result
}