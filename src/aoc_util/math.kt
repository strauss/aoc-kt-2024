package aoc_util

import org.apache.commons.math3.fraction.BigFraction
import org.apache.commons.math3.linear.*
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

fun main() {
//    firstExample()
//    secondExample()
    thirdExample()

    // next


}

private fun thirdExample() {
    // Beispielmatrix mit nichttrivialem Nullraum:
    // Zeile 2 ist 2 * Zeile 1 -> Rang < n -> Nullraumdimension >= 1
    val a = MatrixUtils.createRealMatrix(
        arrayOf(
            doubleArrayOf(-2.0, 1.0, -2.0, -56.0, -22.0, 45.0),
            doubleArrayOf(-1.0, -1.0, -2.0, -16.0, 14.0, 1.0),
            doubleArrayOf(-2.0, -2.0, -4.0, -32.0, 12.0, 10.0),
            doubleArrayOf(-1.0, -2.0, -1.0, 25.0, -16.0, 7.0),
            doubleArrayOf(1.0, -5.0, -3.0, 18.0, 75.0, -119.0)
        )
    )

    val basis = nullSpaceLessEquationsThanDimensions(a)

    if (basis.isEmpty()) {
        println("Nur triviale Lösung x=0 (A ist numerisch vollrangig).")
        return
    }

    println("Nullraum-Dimension: ${basis.size}")
    basis.forEachIndexed { idx, v ->
        val residual = norm2(a.operate(v)) // sollte ~0 sein
        println("Basisvektor $idx: ${v.toArray().contentToString()} | ||A v|| = $residual")
    }

    // Eine nichttriviale Lösung: nimm einfach einen Basisvektor
    val x = basis.first()
    val xa = x.toArray()
    println("Eine nichttriviale Lösung x: ${xa.contentToString()}")
    val xi = scaleToInteger(x)
    println("Hochskaliert: ${xi.contentToString()}")
}

fun scaleToInteger(v: RealVector, maxDen: Int = 1_000_000): Array<BigInteger> {
    val arr = v.toArray()
    val s = arr.maxOf { abs(it) }
    require(s > 0) { "v darf nicht der Nullvektor sein" }
    val normed = arr.map { it / s }

    val fracs = normed.map { BigFraction(it, maxDen) } // :contentReference[oaicite:4]{index=4}

    var lcmDen = BigInteger.ONE
    for (f in fracs) lcmDen = lcmDen.divide(lcmDen.gcd(f.denominator)).multiply(f.denominator).abs()

    val ints = fracs.map { f ->
        f.numerator.multiply(lcmDen.divide(f.denominator))
    }.toMutableList()

    // gcd kürzen, Vorzeichen fixieren
    val g = ints.map { it.abs() }.fold(BigInteger.ZERO) { acc, x -> if (acc == BigInteger.ZERO) x else acc.gcd(x) }
    for (i in ints.indices) ints[i] = ints[i].divide(g)
    val firstNonZero = ints.firstOrNull { it != BigInteger.ZERO }
    if (firstNonZero != null && firstNonZero < BigInteger.ZERO) {
        for (i in ints.indices) ints[i] = ints[i].negate()
    }
    return ints.toTypedArray()
}

fun nullSpaceLessEquationsThanDimensions(a: RealMatrix, relTol: Double = 1e-12): List<RealVector> {
    val m = a.rowDimension
    val n = a.columnDimension

    // 1) Rang r robust via SVD (nur Singulärwerte, kein Zugriff auf V-Spalten nötig)
    val svd = SingularValueDecomposition(a)
    val s = svd.singularValues // Länge min(m,n)
    val maxS = s.maxOrNull() ?: 0.0
    val tol = max(1.0, maxS) * relTol

    var r = 0
    for (sigma in s) if (sigma > tol) r++

    // 2) Nullraum-Basis über QR von A^T: letzte (n-r) Spalten von Q
    val qr = QRDecomposition(a.transpose())
    val q = qr.q // n x n

    val basis = mutableListOf<RealVector>()
    for (i in r until n) {
        basis += q.getColumnVector(i)
    }
    return basis
}

fun nullSpace(a: RealMatrix, relTol: Double = 1e-12): List<RealVector> {
    val svd = SingularValueDecomposition(a)
    val s = svd.singularValues
    val v = svd.v // right singular vectors (columns)

    val maxS = s.maxOrNull() ?: 0.0
    // Toleranz: relativ zum größten Singulärwert (robuster als absolute 0)
    val tol = max(1.0, maxS) * relTol

    val basis = mutableListOf<RealVector>()
    for (i in s.indices) {
        if (s[i] <= tol) {
            basis += v.getColumnVector(i)
        }
    }
    return basis
}

fun norm2(v: RealVector): Double {
    // euklidische Norm
    val arr = v.toArray()
    var sum = 0.0
    for (x in arr) sum += x * x
    return sqrt(sum)
}


private fun secondExample() {
    val a = MatrixUtils.createRealMatrix(
        arrayOf(
            doubleArrayOf(1.0, 1.0),
            doubleArrayOf(1.0, -1.0),
            doubleArrayOf(2.0, 1.0)
        )
    )

    val b = MatrixUtils.createRealVector(doubleArrayOf(2.0, 0.0, 2.9))

    val x = QRDecomposition(a).solver.solve(b)

    println(x.toArray().contentToString())
}

private fun firstExample() {
    val a = MatrixUtils.createRealMatrix(
        arrayOf(
            doubleArrayOf(2.0, 1.0),
            doubleArrayOf(1.0, 3.0)
        )
    )

    val b = MatrixUtils.createRealVector(doubleArrayOf(5.0, 6.0))

    val solver = LUDecomposition(a).solver

    val x = solver.solve(b)

    println(x.toArray().contentToString())
}