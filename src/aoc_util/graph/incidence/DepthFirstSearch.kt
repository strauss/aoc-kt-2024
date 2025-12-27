package aoc_util.graph.incidence

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

class DepthFirstSearch<V, E>(val graph: Graph<V, E>, val visitor: DfsVisitor<V, E>? = null) {
    val number: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val rnumber: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val parent: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val marker = PrimitiveIntSetB()

    var num: Int = 0
    var rnum: Int = 0

    fun execute(root: Graph<V, E>.Vertex? = null) {
        if (root != null) {
            // perform search just for the given root
            visitor?.visitRoot(root)
            dfs(root)
            visitor?.leaveRoot(root)
        } else {
            // perform a complete search and automatically select new roots that have not been visited yet
            val vertexIterator = graph.vertexSequence().iterator()
            while (vertexIterator.hasNext()) {
                val currentRoot = vertexIterator.next()
                if (currentRoot.id !in marker) {
                    visitor?.visitRoot(currentRoot)
                    dfs(currentRoot)
                    visitor?.leaveRoot(currentRoot)
                }
            }
        }


    }

    private fun dfs(vertex: Graph<V, E>.Vertex) {
        val vId = vertex.id
        num += 1
        number[vId] = num
        marker.add(vId)
        visitor?.visitVertex(vertex)

        val incidenceOutIterator = vertex.incidenceOutSequence().iterator()
        while (incidenceOutIterator.hasNext()) {
            val currentEdge = incidenceOutIterator.next()
            val nextVertex = currentEdge.omega
            val nId = nextVertex.id
            visitor?.visitEdge(currentEdge)

            if (nId !in marker) {
                parent[nId] = vId
                visitor?.visitTreeEdge(currentEdge)

                dfs(nextVertex)

                visitor?.leaveTreeEdge(currentEdge)
            } else {
                visitor?.visitFrond(currentEdge)
                val nRnum: Int? = rnumber[nId]
                if (nRnum == null) {
                    visitor?.visitBackwardArc(currentEdge)
                } else if ((number[nId] ?: 0) > (number[vId] ?: 0)) {
                    visitor?.visitForwardArc(currentEdge)
                } else {
                    visitor?.visitCrossLink(currentEdge)
                }
            }

            visitor?.leaveEdge(currentEdge)
        }

        rnum += 1
        rnumber[vId] = rnum
        visitor?.leaveVertex(vertex)
    }

    fun reset() {
        number.clear()
        rnumber.clear()
        parent.clear()
        marker.clear()
    }
}

fun main() {
    val sampleGraph = Graph<String, Unit>()
    val a = sampleGraph.createVertex("A")
    val b = sampleGraph.createVertex("B")
    val c = sampleGraph.createVertex("C")
    val d = sampleGraph.createVertex("D")

    sampleGraph.createEdge(a, b, Unit)
    sampleGraph.createEdge(a, c, Unit)
    sampleGraph.createEdge(a, d, Unit)

    sampleGraph.createEdge(b, d, Unit)

    sampleGraph.createEdge(c, b, Unit)

    sampleGraph.createEdge(d, a, Unit)

    val visitor = object : DfsVisitor<String, Unit>() {
        override fun visitTreeEdge(treeEdge: Graph<String, Unit>.Edge) {
            println("Detected tree edge ${treeEdge.alpha.value} -> ${treeEdge.omega.value}")
        }

        override fun visitForwardArc(forwardArc: Graph<String, Unit>.Edge) {
            println("Detected forward arc ${forwardArc.alpha.value} -> ${forwardArc.omega.value}")
        }

        override fun visitBackwardArc(backwardArc: Graph<String, Unit>.Edge) {
            println("Detected backward arc ${backwardArc.alpha.value} -> ${backwardArc.omega.value}")
        }

        override fun visitCrossLink(crossLink: Graph<String, Unit>.Edge) {
            println("Detected cross link ${crossLink.alpha.value} -> ${crossLink.omega.value}")
        }
    }

    val dfs = DepthFirstSearch(sampleGraph, visitor)

    dfs.execute(a)

}