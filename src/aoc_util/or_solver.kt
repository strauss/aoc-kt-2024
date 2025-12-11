package aoc_util

import com.google.ortools.Loader
import com.google.ortools.sat.*

fun main() {
    Loader.loadNativeLibraries()

    // Zielvektor b
    val b = intArrayOf(3, 5, 4, 7)

    // 0/1-Vektoren
    val vectors = arrayOf(
        intArrayOf(0, 0, 0, 1),
        intArrayOf(0, 0, 1, 0),
        intArrayOf(0, 1, 0, 1),
        intArrayOf(0, 0, 1, 1),
        intArrayOf(1, 0, 1, 0),
        intArrayOf(1, 1, 0, 0)
    )

    val (model, x: Array<IntVar>) = createModel(vectors, b)

    // Ziel: minimiere Summe aller x[j]
    model.minimize(LinearExpr.sum(x))

    val solver = CpSolver()
    val status = solver.solve(model)

    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
        println("Status: $status")
        println("Min sum(x) = ${solver.objectiveValue().toLong()}")
        for (j in x.indices) {
            val v = solver.value(x[j])
            if (v != 0L) println("${x[j].name} = $v  vector=${vectors[j].contentToString()}")
        }
    } else {
        println("No solution. Status: $status")
    }
}

fun createModel(
    vectors: Array<IntArray>,
    b: IntArray
): Pair<CpModel, Array<IntVar>> {
    val model = CpModel()

    // x[j] = wie oft benutze ich vectors[j]
    val x: Array<IntVar> = Array(vectors.size) { j ->
        // einfache Obergrenze: min(b[i]) über alle Dimensionen, wo vectors[j][i] == 1
        var ub = Int.MAX_VALUE
        for (i in b.indices) if (vectors[j][i] == 1) ub = minOf(ub, b[i])
        if (ub == Int.MAX_VALUE) ub = 0 // all-zero Vektor -> hier nutzlos
        model.newIntVar(0, ub.toLong(), "x$j")
    }

    // Für jede Dimension i: Summe der x[j] mit vectors[j][i]==1 muss b[i] ergeben
    for (i in b.indices) {
        val varsInThisDim = x.indices
            .filter { j -> vectors[j][i] == 1 }
            .map { j -> x[j] }
            .toTypedArray()

        model.addEquality(LinearExpr.sum(varsInThisDim), b[i].toLong())
    }
    return Pair(model, x)
}