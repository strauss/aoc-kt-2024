package aoc_util

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB

class BitSetAdjacencyBasedGraph<V>(val directed: Boolean = false) {
    private var nextId = 0
    private val vertexToIdMap: MutableMap<V, Int> = HashMap()
    private val idToVertexMap: MutableMap<Int, V> = HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<V>().create()
    private val idToAdjacencies: MutableMap<Int, MutableSet<Int>> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<MutableSet<Int>>().create()

    fun createCopy(): BitSetAdjacencyBasedGraph<V> {
        val copy = BitSetAdjacencyBasedGraph<V>()
        copy.nextId = nextId
        copy.vertexToIdMap.putAll(vertexToIdMap)
        copy.idToVertexMap.putAll(idToVertexMap)
        idToAdjacencies.entries.forEach { entry ->
            val value = PrimitiveIntSetB()
            copy.idToAdjacencies[entry.key] = value
            value.addAll(entry.value)
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
        idToAdjacencies[id] = PrimitiveIntSetB()
        return id
    }

    fun V.getId(): Int = vertexToIdMap[this] ?: -1

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
            idToAdjacencies[vertexId]?.add(otherVertexId)
            if (!directed) {
                idToAdjacencies[otherVertexId]?.add(vertexId)
            }
        }
    }

    fun V.disconnect(vertex: V) {
        val vertexId = this.getId()
        val otherVertexId = vertex.getId()
        if (vertexId != -1 && otherVertexId != 1) {
            idToAdjacencies[vertexId]?.remove(otherVertexId)
            if (!directed) {
                idToAdjacencies[otherVertexId]?.remove(vertexId)
            }
        }
    }

    fun vertexIterator(): Iterator<V> = vertexToIdMap.keys.iterator()

    fun getEdges(): List<Pair<V, V>> {
        val edges: MutableList<Pair<V, V>> = ArrayList()
        val vertexIterator = vertexIterator()
        while (vertexIterator.hasNext()) {
            val vertex = vertexIterator.next()
            val adjacencies = vertex.adjacencies()
            while (adjacencies.hasNext()) {
                val adjacency = adjacencies.next()
                if (directed || adjacency.getId() > vertex.getId()) {
                    edges.add(Pair(vertex, adjacency))
                }
            }
        }
        return edges
    }

    fun V.adjacencies(): Iterator<V> {
        val adjacentIndexes: Set<Int> = idToAdjacencies[this.getId()] ?: emptySet()
        return object : Iterator<V> {
            val internalIterator = adjacentIndexes.iterator()
            override fun hasNext(): Boolean = internalIterator.hasNext()
            override fun next(): V = idToVertexMap[internalIterator.next()] ?: throw NoSuchElementException()
        }
    }

    fun V.isConnectedWith(vertex: V): Boolean = idToAdjacencies[this.getId()]?.contains(vertex.getId()) ?: false ||
            (!directed && idToAdjacencies[vertex.getId()]?.contains(this.getId()) ?: false)

}

fun blackMagic(vertex: String, g: BitSetAdjacencyBasedGraph<String>) {
    g.run {
        println(vertex.getId())
    }
}
