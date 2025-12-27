package aoc_util.graph.incidence

/**
 * "Simple" graph implementation using incidence lists.
 *
 * In this implementation, both vertices and edges may hold homogeneous value types.
 * Deletion operations are not supported in this simple implementation.
 *
 * @param V the value type stored in vertices
 * @param E the value type stored in edges
 */
class Graph<V, E> {

    /**
     * The vertex list.
     */
    private val vertices: MutableList<Vertex> = ArrayList()

    /**
     * The edge list.
     */
    private val edges: MutableList<Edge> = ArrayList()

    /**
     * The next free vertex id.
     */
    private var nextVertexId: Int = 1

    /**
     * The next free edge id.
     */
    private var nextEdgeId: Int = 1

    /**
     * Inner vertex class.
     *
     * The constructor is internal to allow construction within the same module,
     * while still discouraging external creation.
     */
    inner class Vertex internal constructor(
        /**
         * The unique id of this vertex.
         */
        val id: Int,
        /**
         * The value of this vertex.
         */
        val value: V
    ) {
        /**
         * Incidence list of incoming edges.
         */
        internal val incidencesIn: MutableList<Edge> = ArrayList()

        /**
         * Incidence list of outgoing edges.
         */
        internal val incidencesOut: MutableList<Edge> = ArrayList()

        /**
         * Returns a sequence of incoming edges.
         *
         * This is also one way to expose a list in a read-only manner.
         */
        fun incidenceInSequence(): Sequence<Edge> = incidencesIn.asSequence()

        /**
         * Returns a sequence of outgoing edges.
         *
         * This is also one way to expose a list in a read-only manner.
         */
        fun incidenceOutSequence(): Sequence<Edge> = incidencesOut.asSequence()

        /**
         * Returns the [Graph] instance this vertex belongs to.
         */
        fun graph(): Graph<V, E> = this@Graph

        override fun toString(): String = "$id: $value"
    }

    /**
     * Inner edge class representing directed edges.
     *
     * The constructor is internal to allow construction within the same module,
     * while still discouraging external creation.
     */
    inner class Edge internal constructor(
        /**
         * The id of this edge.
         */
        val id: Int,
        /**
         * The start vertex (alpha) of this edge.
         */
        val alpha: Vertex,
        /**
         * The end vertex (omega) of this edge.
         */
        val omega: Vertex,
        /**
         * The value of this edge.
         */
        val value: E
    ) {
        /**
         * Returns the [Graph] instance this edge belongs to.
         */
        fun graph(): Graph<V, E> = this@Graph

        override fun toString(): String = "($alpha) -> ($omega) $value"
    }

    /**
     * Creates a vertex and adds it to this graph. Initially, the vertex will be isolated.
     */
    fun createVertex(value: V): Vertex {
        val newVertex = Vertex(nextVertexId, value)
        vertices.add(newVertex)
        nextVertexId += 1
        return newVertex
    }

    /**
     * Returns the vertex for the given id, or `null` if none exists.
     */
    fun getVertex(vertexId: Int): Vertex? {
        val vertexIndex = vertexId - 1
        if (vertexIndex < 0 || vertexIndex >= vertices.size) return null
        return vertices[vertexIndex]
    }

    /**
     * Returns a sequence of all vertices.
     *
     * This is also one way to expose a list in a read-only manner.
     */
    fun vertexSequence(): Sequence<Vertex> = vertices.asSequence()

    /**
     * Returns how many vertices are contained in this graph.
     */
    fun countVertices(): Int = vertices.size

    /**
     * Creates a new edge and adds it to this graph.
     *
     * You must provide the alpha vertex, the omega vertex, and the edge value.
     * The involved vertices must be part of this graph.
     *
     * @throws IllegalArgumentException if alpha/omega are not part of this graph
     */
    fun createEdge(alpha: Vertex, omega: Vertex, value: E): Edge {
        if (alpha.graph() !== this || omega.graph() !== this) {
            throw IllegalArgumentException("The vertices have to be part of this graph.")
        }

        val newEdge = Edge(nextEdgeId, alpha, omega, value)
        edges.add(newEdge)

        // Maintain incidence lists
        alpha.incidencesOut.add(newEdge)
        omega.incidencesIn.add(newEdge)

        nextEdgeId += 1
        return newEdge
    }

    /**
     * Creates a new edge and adds it to this graph.
     *
     * You must provide the ids of the alpha and omega vertices and the edge value.
     * A vertex must exist in this graph for both ids.
     */
    fun createEdge(alphaId: Int, omegaId: Int, value: E): Edge {
        val alpha = getVertex(alphaId)
            ?: throw IllegalArgumentException("No vertex exists for alphaId=$alphaId.")
        val omega = getVertex(omegaId)
            ?: throw IllegalArgumentException("No vertex exists for omegaId=$omegaId.")
        return createEdge(alpha, omega, value)
    }

    /**
     * Returns the edge for the given id, or `null` if none exists.
     */
    fun getEdge(edgeId: Int): Edge? {
        val edgeIndex = edgeId - 1
        if (edgeIndex < 0 || edgeIndex >= edges.size) return null
        return edges[edgeIndex]
    }

    /**
     * Returns a sequence of all edges.
     *
     * This is also one way to expose a list in a read-only manner.
     */
    fun edgeSequence(): Sequence<Edge> = edges.asSequence()

    /**
     * Returns how many edges are contained in this graph.
     */
    fun countEdges(): Int = edges.size
}

