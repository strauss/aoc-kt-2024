package aoc_util

import java.util.*

class Warshall<V>(val graph: BitSetAdjacencyBasedGraph<V>) {
    private val reachable = Array(graph.countVertices()) { BitSet(graph.countVertices()) }

    fun execute() {
        graph.run {
            val vertexList = vertexIterator().asSequence().toList()

            for (v in vertexList) {
                val vId = v.getId()
                reachable[vId].set(vId)
            }

            getEdges().forEach { edge ->
                reachable[edge.alpha.getId()].set(edge.omega.getId())
            }

            // optimized for loop (two instead of three)
            for (v in vertexList) {
                val vId = v.getId()
                val rowV = reachable[vId]

                for (u in vertexList) {
                    val uId = u.getId()
                    val rowU = reachable[uId]
                    if (rowU[vId]) {
                        rowU.or(rowV)
                    }
                }
            }
        }
    }

    fun isReachable(from: V, to: V): Boolean {
        graph.run {
            return reachable[from.getId()][to.getId()]
        }
    }
}