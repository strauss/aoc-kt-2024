package aoc_util.graph

import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

// TODO: make it work or toss it away
class ExhaustiveDfs<V>(val graph: BitSetAdjacencyBasedGraph<V>) {
    private val intermediateResults: MutableMap<Int, MutableList<List<Int>>> = HashMap()
    private val computedSet: MutableSet<Pair<Int, Int>> = HashSet()

    private var currentPath = Path()

    fun getResult(v: V): List<List<Int>> {
        graph.run {
            return intermediateResults[v.getId()] ?: listOf()
        }
    }

    fun execute(v: V, goal: V, blacklist: Set<V>) {
        internalExecute(v, goal, blacklist)
    }

    fun internalExecute(v: V, goal: V, blacklist: Set<V>) {
        graph.run {
            val vId = v.getId()
            currentPath.add(vId)
            val adj = v.adjacencies()
            for (n in adj) {
                if (blacklist.contains(n)) {
                    continue
                }
                val nId = n.getId()
                if (currentPath.visited(nId)) {
                    continue
                }
                if (computedSet.contains(vId to nId)) {
                    continue
                }
                if (n == goal) {
                    currentPath.add(nId)
                    val allPaths = currentPath.extractAllPaths()
                    allPaths.forEach { (from: Int, newPath: List<Int>) ->
                        val iResults: MutableList<List<Int>> = intermediateResults.getOrPut(from) { ArrayList() }
                        iResults.add(newPath)
                    }
                    currentPath.removeLast()
                    continue
                }

                internalExecute(n, goal, blacklist)

                computedSet.add(vId to nId)
            }
            currentPath.removeLast()
        }
    }

    class Path() {
        private val path = PrimitiveIntArrayList()
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

        fun extractAllPaths(): List<Pair<Int, List<Int>>> {
            return buildList {
                for (idx in 0..path.size - 2) {
                    val current = path[idx]
                    val innerList = PrimitiveIntArrayList()
                    for (nidx in idx..<path.size) {
                        innerList.add(path[nidx])
                    }
                    add(current to innerList)
                }
            }
        }
    }
}