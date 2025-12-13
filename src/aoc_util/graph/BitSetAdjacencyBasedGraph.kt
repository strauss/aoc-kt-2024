package aoc_util.graph

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import java.util.*

class BitSetAdjacencyBasedGraph<V>(val directed: Boolean = false) : Iterable<V> {
    private var nextId = 0
    private val vertexToIdMap: MutableMap<V, Int> = HashMap()
    private val idToVertexMap: MutableMap<Int, V> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<V>().create()
    private val idToAdjacencies: MutableMap<Int, BitSet> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<BitSet>().create()
    private val idToBackwardAdjacencies: MutableMap<Int, BitSet> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<BitSet>().create()
    val size
        get() = vertexToIdMap.size

    fun createCopy(): BitSetAdjacencyBasedGraph<V> {
        val copy = BitSetAdjacencyBasedGraph<V>(directed)
        copy.nextId = nextId
        copy.vertexToIdMap.putAll(vertexToIdMap)
        copy.idToVertexMap.putAll(idToVertexMap)
        idToAdjacencies.entries.forEach { entry ->
            val value = BitSet()
            copy.idToAdjacencies[entry.key] = value
            value.or(entry.value) // add all
        }
        idToBackwardAdjacencies.forEach { entry ->
            val value = BitSet()
            copy.idToBackwardAdjacencies[entry.key] = value
            value.or(entry.value) // add all
        }
        return copy
    }

    fun introduceVertex(vertex: V): Int {
        if (vertexToIdMap.containsKey(vertex)) {
            return vertex.getId()
        }
        val id = nextId
        nextId += 1
        vertexToIdMap[vertex] = id
        idToVertexMap[id] = vertex
        idToAdjacencies[id] = BitSet()
        if (directed) {
            idToBackwardAdjacencies[id] = BitSet()
        }
        return id
    }

    fun V.remove() {
        isolate()
        val id = getId()
        idToAdjacencies.remove(id)
        if (directed) {
            idToBackwardAdjacencies.remove(id)
        }
        vertexToIdMap.remove(this)
        idToVertexMap.remove(id)
    }

    fun countVertices() = vertexToIdMap.size

    // that is probably incorrect
    fun countEdges(): Int = idToAdjacencies.values.sumOf { it.cardinality() }

    fun V.getId(): Int = vertexToIdMap[this] ?: -1

    operator fun get(id: Int): V? = idToVertexMap[id]

    fun V.connect(vertex: V) {
        // ensure both vertices are contained
        if (!vertexToIdMap.containsKey(this)) {
            introduceVertex(this)
        }
        if (!vertexToIdMap.containsKey(vertex)) {
            introduceVertex(vertex)
        }
        val vertexId = this.getId()
        val otherVertexId = vertex.getId()
        if (vertexId != -1 && otherVertexId != -1) {
            idToAdjacencies[vertexId]?.set(otherVertexId)
            if (directed) {
                idToBackwardAdjacencies[otherVertexId]?.set(vertexId)
            } else {
                idToAdjacencies[otherVertexId]?.set(vertexId)
            }
        }
    }

    fun disconnect(edge: Edge) = edge.alpha.disconnect(edge.omega)

    fun V.disconnect(vertex: V) {
        val vertexId = this.getId()
        val otherVertexId = vertex.getId()
        if (vertexId != -1 && otherVertexId != -1) {
            idToAdjacencies[vertexId]?.set(otherVertexId, false)
            if (directed) {
                idToBackwardAdjacencies[otherVertexId]?.set(vertexId, false)
            } else {
                idToAdjacencies[otherVertexId]?.set(vertexId, false)
            }
        }
    }

    fun vertexIterator(): Iterator<V> = vertexToIdMap.keys.iterator()

    override fun iterator(): Iterator<V> = vertexIterator()

    fun getEdges(): List<Edge> {
        val edges: MutableList<Edge> = ArrayList()
        val vertexIterator = vertexIterator()
        while (vertexIterator.hasNext()) {
            val vertex = vertexIterator.next()
            val adjacencies = vertex.adjacencies()
            while (adjacencies.hasNext()) {
                val adjacency = adjacencies.next()
                if (directed || adjacency.getId() > vertex.getId()) {
                    edges.add(Edge(vertex, adjacency))
                }
            }
        }
        return edges
    }

    fun V.getDegree(): Int = idToAdjacencies[getId()]?.cardinality() ?: 0

    fun V.getInDegree(): Int = if (directed) idToBackwardAdjacencies[getId()]?.cardinality() ?: 0 else getDegree()

    fun V.adjacencies(): Iterator<V> {
        val adjacencies = idToAdjacencies[this.getId()] ?: BitSet()
        return this@BitSetAdjacencyBasedGraph.BitSetBasedVertexIterator(adjacencies)
    }

    fun V.backwardAdjacencies(): Iterator<V> {
        return if (directed) {
            val backwardAdjacencies = idToBackwardAdjacencies[this.getId()] ?: BitSet()
            this@BitSetAdjacencyBasedGraph.BitSetBasedVertexIterator(backwardAdjacencies)
        } else {
            adjacencies()
        }
    }

    private inner class BitSetBasedVertexIterator(val vertexIdSet: BitSet) : Iterator<V> {
        private var nextProbePosition = 0
        private var nextSetBit = -1
        private var lastDeliveredBit = -1

        override fun hasNext(): Boolean {
            if (nextProbePosition < 0) {
                // handles overflows
                return false
            }
            internalDetermineNextSetBit()
            return nextProbePosition >= 0 && nextSetBit >= 0
        }

        private fun internalDetermineNextSetBit() {
            if (nextSetBit == -1) {
                nextSetBit = vertexIdSet.nextSetBit(nextProbePosition)
            }
        }

        override fun next(): V {
            internalDetermineNextSetBit()
            if (nextSetBit < 0) {
                throw NoSuchElementException()
            }
            nextProbePosition = nextSetBit + 1
            lastDeliveredBit = nextSetBit
            nextSetBit = -1
            return idToVertexMap[lastDeliveredBit] ?: throw NoSuchElementException()
        }
    }

    fun V.countAdjecencies(): Int = idToAdjacencies[this.getId()]?.cardinality() ?: 0

    fun V.isolate() {
        this.adjacencies().forEach { this.disconnect(it) }
        this.backwardAdjacencies().forEach { it.disconnect(this) }
    }


    fun V.isConnectedWith(vertex: V): Boolean = idToAdjacencies[this.getId()]?.get(vertex.getId()) ?: false ||
            (!directed && idToAdjacencies[vertex.getId()]?.get(this.getId()) ?: false)

    fun V.isConnectedWithAll(vertices: Set<V>): Boolean = vertices.all { it.isConnectedWith(this) }


    abstract inner class SearchVisitor {
        open fun visitRoot(root: V) {
            // Default empty implementation
        }

        open fun leaveRoot(root: V) {
            // Default empty implementation
        }

        open fun visitVertex(vertex: V) {
            // Default empty implementation
        }

        open fun visitEdge(edge: Edge, from: V, to: V) {
            // Default empty implementation
        }

        open fun visitTreeEdge(edge: Edge, from: V, to: V) {
            // Default empty implementation
        }

        open fun visitFrond(edge: Edge, from: V, to: V) {
            // Default empty implementation
        }
        // TODO: add further functions
    }

    enum class SearchType {
        DFS, BFS
    }

    fun search(searchType: SearchType, visitor: SearchVisitor, firstRoot: V? = null, complete: Boolean = true) {
        val vertexIterator = vertexIterator()
        if (!vertexIterator.hasNext()) {
            return
        }
        val entered = PrimitiveIntSetB()
        val vertexBuffer = LinkedList<V>()
        var root = firstRoot ?: vertexIterator.next()

        visitor.visitRoot(root)
        visitor.visitVertex(root)

        entered.add(root.getId())
        vertexBuffer.add(root)
        while (vertexBuffer.isNotEmpty()) {

            val currentVertex = when (searchType) {
                SearchType.DFS -> vertexBuffer.removeLast()
                SearchType.BFS -> vertexBuffer.removeFirst()
            }

            currentVertex.adjacencies().forEach { adjacentVertex: V ->
                val currentEdge = Edge(currentVertex, adjacentVertex)

                visitor.visitEdge(currentEdge, currentVertex, adjacentVertex)

                if (!entered.contains(adjacentVertex.getId())) {
                    entered.add(adjacentVertex.getId())
                    vertexBuffer.add(adjacentVertex)

                    visitor.visitVertex(adjacentVertex)
                    visitor.visitTreeEdge(currentEdge, currentVertex, adjacentVertex)
                } else {
                    visitor.visitFrond(currentEdge, currentVertex, adjacentVertex)
                }
            }
            if (vertexBuffer.isEmpty() && complete) {
                // Empty stack = done with current weak component
                visitor.leaveRoot(root)
                while (vertexIterator.hasNext()) {
                    val nextPossibleRoot = vertexIterator.next()
                    if (!entered.contains(nextPossibleRoot.getId())) {
                        root = nextPossibleRoot

                        visitor.visitRoot(root)

                        entered.add(root.getId())
                        vertexBuffer.add(root)
                        break
                    }
                }
            }
        }
    }

    inner class WeakComponentVisitor : BitSetAdjacencyBasedGraph<V>.SearchVisitor() {
        private val internalResult = ArrayList<MutableList<V>>()
        val result: List<List<V>>
            get() = internalResult

        private lateinit var currentInnerList: MutableList<V>

        override fun visitRoot(root: V) {
            currentInnerList = ArrayList()
            currentInnerList.add(root)
        }

        override fun leaveRoot(root: V) {
            internalResult.add(currentInnerList)
        }

        override fun visitVertex(vertex: V) {
            currentInnerList.add(vertex)
        }
    }

    inner class DepthVisitor : BitSetAdjacencyBasedGraph<V>.SearchVisitor() {
        private val internalDepth = HashTableBasedMapBuilder.useIntKey().useIntValue().create()
        val depth: Map<Int, Int>
            get() = internalDepth

        override fun visitRoot(root: V) {
            internalDepth[root.getId()] = 0
        }

        override fun visitTreeEdge(edge: BitSetAdjacencyBasedGraph<V>.Edge, from: V, to: V) {
            val fromDepth: Int = internalDepth[from.getId()] ?: 0
            val currentDepth = internalDepth[to.getId()] ?: 0
            internalDepth[to.getId()] = currentDepth.coerceAtLeast(fromDepth + 1)
        }

    }

    /**
     * Edge representation. In undirected graphs the given [alpha] and [omega] are oriented by their id. The vertex with the lower id is always
     * [alpha] if the graph is undirected. In directed graphs the given [alpha] and [omega] are set as defined.
     */
    inner class Edge(alpha: V, omega: V) {
        val alpha: V
        val omega: V

        init {
            if (directed || alpha.getId() < omega.getId()) {
                this.alpha = alpha
                this.omega = omega
            } else {
                this.omega = alpha
                this.alpha = omega
            }
        }

        fun disconnect() = alpha.disconnect(omega)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BitSetAdjacencyBasedGraph<*>.Edge

            if (alpha != other.alpha) return false
            if (omega != other.omega) return false

            return true
        }

        override fun hashCode(): Int {
            var result = alpha?.hashCode() ?: 0
            result = 31 * result + (omega?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return if (directed) {
                "($alpha -> $omega)"
            } else {
                "($alpha -- $omega)"
            }
        }

    }
}