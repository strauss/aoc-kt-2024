package aoc_util.graph

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import java.util.*

class Dijkstra<V>(val graph: WeightedGraph<V>) {

    val number: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val parent: MutableMap<Int, Int> = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
    val distance: MutableMap<Int, Double> = HashTableBasedMapBuilder.useIntKey().useDoubleValue().create()

    val pQueue: Queue<Pair<V, Double>> = PriorityQueue(Comparator.comparing(Pair<V, Double>::second))
    var num: Int = 0

    fun reset() {
        number.clear()
        parent.clear()
        distance.clear()
        parent.clear()
        num = 0
    }

    fun execute(root: V, visitor: DijkstraVisitor<V>? = null) {

        // run it in the context of the graph
        graph.run {
            for (vertex: V in vertexIterator()) {
                distance[vertex.getId()] = Double.POSITIVE_INFINITY
            }
            distance[root.getId()] = 0.0
            pQueue.offer(Pair(root, 0.0))

            visitor?.visitRoot(root)

            while (pQueue.isNotEmpty()) {
                val (vertex, _) = pQueue.poll()
                val vertexId = vertex.getId()
                if (!number.containsKey(vertexId)) {
                    num += 1
                    number[vertexId] = num

                    visitor?.visitVertex(vertex)

                    for ((adjacentVertex, weight) in vertex.adjacencies()) {
                        val newDistance: Double = (distance[vertexId] ?: 0.0) + weight

                        visitor?.visitEdge(vertex, adjacentVertex, weight)

                        val adjacentVertexId = adjacentVertex.getId()
                        if ((distance[adjacentVertexId] ?: Double.POSITIVE_INFINITY) > newDistance) {
                            parent[adjacentVertexId] = vertexId
                            distance[adjacentVertexId] = newDistance
                            pQueue.offer(Pair(adjacentVertex, newDistance))
                        }
                    }
                }
            }
        }
    }

    abstract class DijkstraVisitor<V>(val dijkstra: Dijkstra<V>) {

        open fun visitRoot(root: V) {
            // do nothing by default
        }

        open fun visitVertex(vertex: V) {
            // do nothing by default
        }

        open fun visitEdge(from: V, to: V, weight: Double) {
            // do nothing by default
        }
    }
}