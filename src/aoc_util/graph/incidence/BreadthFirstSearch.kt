package aoc_util.graph.incidence

import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

class BreadthFirstSearch<V, E>(val graph: Graph<V, E>, val visitor: SearchVisitor<V, E>? = null) {
    private val entered = PrimitiveIntSetB()
    private val vertexBuffer = ArrayDeque<Graph<V, E>.Vertex>()

    fun search(root: Graph<V, E>.Vertex? = null) {
        val complete = root == null
        val vertexIterator = graph.vertexSequence().iterator()
        if (!vertexIterator.hasNext()) {
            return
        }
        var currentRoot = root ?: vertexIterator.next()

        visitor?.visitRoot(currentRoot)
        visitor?.visitVertex(currentRoot)

        entered.add(currentRoot.id)
        vertexBuffer.addLast(currentRoot)

        while (vertexBuffer.isNotEmpty()) {
            val currentVertex = vertexBuffer.removeFirst()

            val incidenceOutIterator = currentVertex.incidenceOutSequence().iterator()

            while (incidenceOutIterator.hasNext()) {
                val currentEdge = incidenceOutIterator.next()
                visitor?.visitEdge(currentEdge)
                val nextVertex = currentEdge.omega
                if (nextVertex.id !in entered) {
                    entered.add(nextVertex.id)
                    vertexBuffer.add(nextVertex)
                    visitor?.visitTreeEdge(currentEdge)
                    visitor?.visitVertex(nextVertex)
                    visitor?.leaveTreeEdge(currentEdge)
                } else {
                    visitor?.visitFrond(currentEdge)
                }
                visitor?.leaveEdge(currentEdge)
            }

            if (vertexBuffer.isEmpty()) {
                visitor?.leaveRoot(currentRoot)
                if (complete) {
                    while (vertexIterator.hasNext()) {
                        val nextPossibleRoot = vertexIterator.next()
                        if (nextPossibleRoot.id !in entered) {
                            currentRoot = nextPossibleRoot

                            visitor?.visitRoot(currentRoot)

                            entered.add(currentRoot.id)
                            vertexBuffer.add(currentRoot)
                            break
                        }
                    }
                }
            }
        }

    }

    fun reset() {
        visitor?.reset()
        entered.clear()
        vertexBuffer.clear()
    }
}