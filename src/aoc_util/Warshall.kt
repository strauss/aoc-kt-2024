package aoc_util

class Warshall<V>(val graph: BitSetAdjacencyBasedGraph<V>) {
    private val reachable = HashSet<Pair<Int, Int>>()

    fun execute() {
        graph.run {
            vertexIterator().forEach {
                val id = it.getId()
                reachable.add(id to id)
            }

            getEdges().forEach { edge ->
                reachable.add(edge.alpha.getId() to edge.omega.getId())
            }

            vertexIterator().forEach { v ->
                val vId = v.getId()
                vertexIterator().forEach { u ->
                    val uId = u.getId()
                    val r1 = uId to vId
                    vertexIterator().forEach { w ->
                        val wId = w.getId()
                        val r2 = vId to wId
                        val r3 = uId to wId
                        if (reachable.contains(r1) && reachable.contains(r2) && !reachable.contains(r3)) {
                            reachable.add(r3)
                        }
                    }
                }
            }
        }
    }

    fun isReachable(from: V, to: V): Boolean {
        graph.run {
            return reachable.contains(from.getId() to to.getId())
        }
    }
}