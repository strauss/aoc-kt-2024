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
//    thirdExample()
    fourthExample()
    // next


}

private fun BigInteger.lcm(other: BigInteger): BigInteger =
    if (this == BigInteger.ZERO || other == BigInteger.ZERO) BigInteger.ZERO
    else this.divide(this.gcd(other)).multiply(other).abs()

private fun gcdAll(xs: List<BigInteger>): BigInteger =
    xs.map { it.abs() }.fold(BigInteger.ZERO) { g, x -> if (g == BigInteger.ZERO) x else g.gcd(x) }

class RrefResult(
    val rref: Array<Array<BigFraction>>,
    val pivotCols: IntArray
)

/**
 * Gauss-Jordan RREF für BigFraction, gibt RREF + Pivotspalten zurück
 */
fun rrefBigFraction(aIn: Array<Array<BigFraction>>): RrefResult {
    val m = aIn.size
    val n = aIn[0].size
    val a = Array(m) { r -> Array(n) { c -> aIn[r][c] } }

    val pivots = mutableListOf<Int>()
    var row = 0
    for (col in 0 until n) {
        if (row >= m) break

        // Pivot suchen
        var pivotRow = -1
        for (r in row until m) {
            if (a[r][col] != BigFraction.ZERO) {
                pivotRow = r
                break
            }
        }
        if (pivotRow == -1) continue

        // Zeilen tauschen
        if (pivotRow != row) {
            val tmp = a[pivotRow]
            a[pivotRow] = a[row]
            a[row] = tmp
        }

        // Pivot auf 1 normieren
        val pivot = a[row][col]
        for (c in col until n) {
            a[row][c] = a[row][c].divide(pivot)
        }

        // Spalte eliminieren (alle anderen Zeilen)
        for (r in 0 until m) {
            if (r == row) {
                continue
            }
            val factor = a[r][col]
            if (factor != BigFraction.ZERO) {
                for (c in col until n) {
                    a[r][c] = a[r][c].subtract(factor.multiply(a[row][c]))
                }
            }
        }

        pivots += col
        row++
    }

    return RrefResult(a, pivots.toIntArray())
}

/**
 * Liefert primitive Integer-Vektoren für den Nullraum (Ax=0), falls deine Matrix ganzzahlig ist.
 * (Wenn der Nullraum eindimensional ist, bekommst du genau einen Vektor.)
 */
fun integerNullspaceBasisFromIntMatrix(a: Array<Array<BigInteger>>): List<Array<BigInteger>> {
    val height = a.size
    val width = a[0].size

    // exakt nach BigFraction
    val bf = Array(height) { row ->
        Array(width) { col -> BigFraction(a[row][col]) }
    }

    val result = rrefBigFraction(bf)
    val rref = result.rref
    val pivotCols = result.pivotCols
    val pivotSet = pivotCols.toSet()
    val freeCols = (0 until width).filter { it !in pivotSet }

    if (freeCols.isEmpty()) return emptyList() // nur triviale Lösung

    // Für jede freie Variable ein Basisvektor
    val basis = mutableListOf<Array<BigInteger>>()
    for (free in freeCols) {
        // x_free = 1, andere freie = 0
        val xFrac = Array(width) { BigFraction.ZERO }
        xFrac[free] = BigFraction.ONE

        // Pivotvariablen aus den RREF-Zeilen: x_pivot = - sum_{free} rref[row][free] * x_free
        for (i in pivotCols.indices) {
            val pCol = pivotCols[i]
            val coeff = rref[i][free] // weil nur diese freie Variable =1 ist
            xFrac[pCol] = coeff.negate()
        }

        // Auf Integer skalieren: kgV aller Nenner
        var lcmDen = BigInteger.ONE
        for (f in xFrac) lcmDen =
            lcmDen.lcm(f.denominator)  // BigFraction.getDenominator() :contentReference[oaicite:1]{index=1}

        val ints = xFrac.map { f ->
            f.numerator.multiply(lcmDen.divide(f.denominator)) // BigFraction.getNumerator()/getDenominator :contentReference[oaicite:2]{index=2}
        }

        // primitive Form: durch gcd teilen, Vorzeichen fixieren
        val g = gcdAll(ints)
        val reduced = ints.map { it.divide(g) }.toMutableList()
        val firstNonZero = reduced.firstOrNull { it != BigInteger.ZERO }
        if (firstNonZero != null && firstNonZero < BigInteger.ZERO) {
            for (k in reduced.indices) reduced[k] = reduced[k].negate()
        }

        basis += reduced.toTypedArray()
    }

    return basis
}

private fun fourthExample() {
    val row1 = listOf(BigFraction(4), BigFraction(3), BigFraction(-1), BigFraction(2)).toTypedArray()
    val row2 = listOf(BigFraction(-3), BigFraction(-4), BigFraction(5), BigFraction(-5)).toTypedArray()
    val row3 = listOf(BigFraction(-2), BigFraction(2), BigFraction(1), BigFraction(6)).toTypedArray()
    val input = listOf(row1, row2, row3).toTypedArray()
    val result = rrefBigFraction(input)
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