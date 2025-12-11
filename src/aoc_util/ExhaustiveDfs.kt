package aoc_util

import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

class ExhaustiveDfs<V>(val graph: BitSetAdjacencyBasedGraph<V>) {
    private val result: MutableList<List<Int>> = ArrayList()
    private var currentPath = Path()

    fun getResult(): List<List<Int>> {
        val r = ArrayList<List<Int>>()
        r.addAll(result)
        return r
    }

    fun execute(v: V, goal: V, blacklist: Set<V>) {
        graph.run {
            val vId = v.getId()
            currentPath.add(vId)
            for (n in v.adjacencies()) {
                if (blacklist.contains(n)) {
                    continue
                }
                val nId = n.getId()
                if (currentPath.visited(nId)) {
                    continue
                }
                if (n == goal) {
                    val rPath = PrimitiveIntArrayList()
                    rPath.addAll(currentPath.path)
                    result.add(rPath)
                    continue
                }

                execute(v, goal, blacklist)
            }
            currentPath.removeLast()
        }
    }

    class Path() {
        val path = PrimitiveIntArrayList()
        private val visited = PrimitiveIntSetB()

        fun add(vId: Int) {
            if (!visited.contains(vId)) {
                path.add(vId)
                visited.add(vId)
            }
        }

        fun removeLast() {
            val last: Int = path.removeLast()
            visited.remove(last)
        }

        fun visited(vId: Int): Boolean {
            return visited.contains(vId)
        }
    }
}