package aoc_2023

import aoc_util.*
import com.google.ortools.Loader
import com.google.ortools.sat.*
import org.apache.commons.math3.fraction.BigFraction
import org.apache.commons.math3.linear.MatrixUtils
import java.math.BigInteger
import kotlin.math.sqrt

// Example solution
// Velocity: Vector(x=-3.0, y=1.0, z=2.0)
// Start   : Vector(x=24.0, y=13.0, z=10.0)

fun main() {
//    trials()

    val lines = readInput2023("Day24")
    val input = parseInput(lines)
//    findStoneLine(input)
    lastApproach(input)
}

private fun lastApproach(input: List<DiscreteLine3D>) {
    val line0 = input[1]
    val line1 = input[196]
    val line2 = input[201]

    val deltaV01 = line1.v - line0.v
    val deltaP01 = line1.p - line0.p
    val c01 = line0.p.cross(line0.v) - line1.p.cross(line1.v)

    val deltaV02 = line2.v - line0.v
    val deltaP02 = line2.p - line0.p
    val c02 = line0.p.cross(line0.v) - line2.p.cross(line2.v)

    // rows are ordered px, py, pz, vx, vy, vz
    val row1 = listOf<BigInteger>(
        BigInteger.ZERO, //px
        deltaV01.z, //py
        -deltaV01.y, //pz
        BigInteger.ZERO, //vx
        -deltaP01.z, //vy
        deltaP01.y, //vz
        -c01.x
    ).map { BigFraction(it) }.toTypedArray()
    val row2 = listOf<BigInteger>(
        -deltaV01.z, // px
        BigInteger.ZERO, // py
        deltaV01.x, // pz
        deltaP01.z, // vx
        BigInteger.ZERO, // vy
        -deltaP01.x, // vz
        -c01.y
    ).map { BigFraction(it) }.toTypedArray()
    val row3 = listOf<BigInteger>(
        deltaV01.y, // px
        -deltaV01.x, // py
        BigInteger.ZERO, // pz
        -deltaP01.y, // vx
        deltaP01.x, // vy
        BigInteger.ZERO, // vz
        -c01.z
    ).map { BigFraction(it) }.toTypedArray()
    val row4 = listOf<BigInteger>(
        BigInteger.ZERO, //px
        deltaV02.z, //py
        -deltaV02.y, //pz
        BigInteger.ZERO, //vx
        -deltaP02.z, //vy
        deltaP02.y, //vz
        -c02.x
    ).map { BigFraction(it) }.toTypedArray()
    val row5 = listOf<BigInteger>(
        -deltaV02.z, // px
        BigInteger.ZERO, // py
        deltaV02.x, // pz
        deltaP02.z, // vx
        BigInteger.ZERO, // vy
        -deltaP02.x, // vz
        -c02.y
    ).map { BigFraction(it) }.toTypedArray()
    val row6 = listOf<BigInteger>(
        deltaV02.y, // px
        -deltaV02.x, // py
        BigInteger.ZERO, // pz
        -deltaP02.y, // vx
        deltaP02.x, // vy
        BigInteger.ZERO, // vz
        -c02.z
    ).map { BigFraction(it) }.toTypedArray()

    val matrix = listOf(row1, row2, row3, row4, row5, row6).toTypedArray()
    val result = rrefBigFraction(matrix)
    val vx = result.rref[0][6].numerator
    val vy = result.rref[1][6].numerator
    val vz = result.rref[2][6].numerator
    println(vx + vy + vz)
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

    val (dv, mv) = getLineWithDiscreteApproach(input)
//    val (dv, mv) = getLineWithApacheMath(input)
//    val (dv, mv) = getLineWithOrTools(input)

    val s = (dv * dv)
    val uv = dv.cross(mv)

    val basePoint = Vector(
        uv.x.toDouble() / s.toDouble(),
        uv.y.toDouble() / s.toDouble(),
        uv.z.toDouble() / s.toDouble()
    )

    val stoneLine = Line3D(basePoint, dv.asVector())


    println(basePoint)

    val intersectionInformation = analyzeIntersections(input, stoneLine)

//    analyzeGcd(dv, s, uv)

//    analyzeIntersections(input, dv, mv)

    val discreteBasePoint = getBasePointDirectly(dv, uv)

    println("Velocity: $dv")
    println("Point: $discreteBasePoint")


//    p = (u + kd) / s


    return null
}

private fun analyzeIntersections(
    input: List<DiscreteLine3D>,
    dv: DiscreteVector,
    mv: DiscreteVector
) {
    val intersects: MutableSet<DiscreteLine3D> = HashSet()
    val doesNotIntersect: MutableSet<DiscreteLine3D> = HashSet()

    for (line in input) {
        if (line.intersects(dv, mv)) {
            intersects.add(line)
        } else {
            doesNotIntersect.add(line)
        }
    }
}

private fun analyzeIntersections(input: List<DiscreteLine3D>, stoneLine: Line3D): List<IntersectionInformation> {
    val intersectionInformation: MutableList<IntersectionInformation> = ArrayList()

    for (line in input) {
        val currentResult = stoneLine.intersects(line.asLine3D())
        intersectionInformation.add(currentResult)
    }

    return intersectionInformation
}

private fun analyzeGcd(dv: DiscreteVector, s: BigInteger, uv: DiscreteVector) {
    val xxx = gcd(dv.x.abs(), s)
    val yyy = gcd(dv.y.abs(), s)
    val zzz = gcd(dv.z.abs(), s)

    val dxx = uv.x.abs() % xxx
    val dyy = uv.y.abs() % yyy
    val dzz = uv.z.abs() % zzz
}

private fun getBasePointDirectly(dv: DiscreteVector, uv: DiscreteVector): DiscreteVector {
    val s = (dv * dv).toLong()
    var result = -1L
    for (current in 0..s) {
        val res1 = Math.floorMod(uv.x.toLong() + current * dv.x.toLong(), s)
        if (res1 != 0L) {
            continue
        }
        val res2 = Math.floorMod(uv.y.toLong() + current * dv.y.toLong(), s)
        if (res2 != 0L) {
            continue
        }
        val res3 = Math.floorMod(uv.z.toLong() + current * dv.z.toLong(), s)
        if (res3 != 0L) {
            continue
        }
        result = current
        break
    }

    if (result > 0) {
        val p = (uv + (dv * result.toBigInteger()))
        val kResult = DiscreteVector(p.x / s.toBigInteger(), p.y / s.toBigInteger(), p.z / s.toBigInteger())
        return kResult
    }
    return DiscreteVector.ZERO
}

private fun getBasePointWithORTools(
    dv: DiscreteVector,
    uv: DiscreteVector
): DiscreteVector {
    val s = (dv * dv).toLong()
    val a = Math.floorMod(dv.x.toLong(), s)
    val b = Math.floorMod(uv.x.toLong(), s)
    val c = Math.floorMod(dv.y.toLong(), s)
    val d = Math.floorMod(uv.y.toLong(), s)
    val e = Math.floorMod(dv.z.toLong(), s)
    val f = Math.floorMod(uv.z.toLong(), s)

    val kModel = CpModel()
    val x = kModel.newIntVar(0.toLong(), s.toLong() - 1L, "x")
    val k1 = kModel.newIntVar(0, Integer.MAX_VALUE.toLong(), "k1")
    val k2 = kModel.newIntVar(0, Integer.MAX_VALUE.toLong(), "k2")
    val k3 = kModel.newIntVar(0, Integer.MAX_VALUE.toLong(), "k3")

    run {
        val expr = LinearExpr.newBuilder()
        expr.addTerm(x, a)
        expr.addTerm(k1, -(s.toLong()))
        kModel.addEquality(expr, -b)
    }

    run {
        val expr = LinearExpr.newBuilder()
        expr.addTerm(x, c)
        expr.addTerm(k2, -(s.toLong()))
        kModel.addEquality(expr, -d)
    }

    run {
        val expr = LinearExpr.newBuilder()
        expr.addTerm(x, e)
        expr.addTerm(k3, -(s.toLong()))
        kModel.addEquality(expr, -f)
    }

    val kSolver = CpSolver()
    val kStatus = kSolver.solve(kModel)

    val k = kSolver.value(x)

    val p = (uv + (dv * k.toBigInteger()))
    val kResult = DiscreteVector(p.x / s.toBigInteger(), p.y / s.toBigInteger(), p.z / s.toBigInteger())
    return kResult
}

private fun getLineWithDiscreteApproach(input: List<DiscreteLine3D>): Pair<DiscreteVector, DiscreteVector> {
    val inputAsCoefficients: Array<Array<BigInteger>> = input.asSequence().map { line: DiscreteLine3D ->
        Array(6) { idx: Int ->
            when (idx) {
                0 -> line.d.x
                1 -> line.d.y
                2 -> line.d.z
                3 -> line.m.x
                4 -> line.m.y
                5 -> line.m.z
                else -> throw IllegalStateException()
            }
        }
    }.toList().toTypedArray()

    val base = integerNullspaceBasisFromIntMatrix(inputAsCoefficients)

    for (i in base.indices) {
        val candidateSolution = base[i]

        val d = DiscreteVector(candidateSolution[3], candidateSolution[4], candidateSolution[5])
        val m = DiscreteVector(candidateSolution[0], candidateSolution[1], candidateSolution[2])

        val test = d * m
        if (test == BigInteger.ZERO) {
            return d to m
        }
    }
    return DiscreteVector.ZERO to DiscreteVector.ZERO
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

    fun intersects(otherD: DiscreteVector, otherM: DiscreteVector) = d * otherM + otherD * m == BigInteger.ZERO

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