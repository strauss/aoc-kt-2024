package aoc_util

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

class RecursiveDfs<V>(val graph: BitSetAdjacencyBasedGraph<V>) {
    val number: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val rnumber: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val parent: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val marker = PrimitiveIntSetB()

    var num: Int = 0
    var rnum: Int = 0

    fun execute(v: V, visitor: DfsVisitor<V>) {
        graph.run {
            val vId = v.getId()
            num += 1
            number[vId] = num
            marker.add(vId)
            visitor.visitVertex(v)
            for (n in v.adjacencies()) {
                val nId = n.getId()
                visitor.visitEdge(v, n)
                if (!marker.contains(nId)) {
                    parent[nId] = vId
                    visitor.visitTreeEdge(v, n)

                    // recursive call
                    execute(n, visitor)

                    visitor.leaveTreeEdge(v, n)
                } else {
                    visitor.visitFrond(v, n)
                    val nRnum: Int? = rnumber[nId]
                    if (nRnum == null) {
                        visitor.visitBackwardArc(v, n)
                    } else if ((number[nId] ?: 0) > (number[vId] ?: 0)) {
                        visitor.visitForwardArc(v, n)
                    } else {
                        visitor.visitCrossLink(v, n)
                    }
                }
            }
            rnum += 1
            rnumber[vId] = rnum
            visitor.leaveVertex(v)
        }
    }

    abstract class DfsVisitor<V>(val rDfs: RecursiveDfs<V>) {

        open fun visitVertex(vertex: V) {
            // Default empty implementation
        }

        open fun leaveVertex(vertex: V) {
            // Default empty implementation
        }

        open fun visitEdge(from: V, to: V) {
            // Default empty implementation
        }

        open fun visitTreeEdge(from: V, to: V) {
            // Default empty implementation
        }

        open fun leaveTreeEdge(from: V, to: V) {
            // Default empty implementation
        }

        open fun visitFrond(from: V, to: V) {
            // Default empty implementation
        }

        open fun visitBackwardArc(from: V, to: V) {
            // Default empty implementation
        }

        open fun visitForwardArc(from: V, to: V) {
            // Default empty implementation
        }

        open fun visitCrossLink(from: V, to: V) {
            // Default empty implementation
        }
    }
}