package aoc_util

import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder

class WeightedGraph<V>(private val directed: Boolean = false) : Iterable<V> {
    private var nextId = 0
    private val vertexToIdMap: MutableMap<V, Int> = HashMap()
    private val idToVertexMap: MutableMap<Int, V> = HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<V>().create()
    private val idToWeightedAdjacencies: MutableMap<Int, MutableMap<Int, Double>> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<MutableMap<Int, Double>>().create()

    fun createCopy(): WeightedGraph<V> {
        val copy = WeightedGraph<V>(directed)
        copy.nextId = nextId
        copy.vertexToIdMap.putAll(vertexToIdMap)
        copy.idToVertexMap.putAll(idToVertexMap)
        idToWeightedAdjacencies.entries.forEach { entry ->
            val value = HashTableBasedMapBuilder.useIntKey().useDoubleValue().create()
            copy.idToWeightedAdjacencies[entry.key] = value
            value.putAll(entry.value)
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
        idToWeightedAdjacencies[id] = HashTableBasedMapBuilder.useIntKey().useDoubleValue().create()
        return id
    }

    fun V.getId(): Int = vertexToIdMap[this] ?: -1

    fun V.isContained() = getId() >= 0

    fun V.getWeight(vertex: V) = (idToWeightedAdjacencies[this.getId()] ?: emptyMap())[vertex.getId()] ?: 0.0

    fun Int.getVertex(): V? = idToVertexMap[this]

    fun V.connect(vertex: V, cost: Double = 1.0) {
        // ensure both are contained
        if (!vertexToIdMap.containsKey(vertex)) {
            introduceVertex(this)
        }
        if (!vertexToIdMap.containsKey(vertex)) {
            introduceVertex(vertex)
        }
        val vertexId = this.getId()
        val otherVertexId = vertex.getId()
        if (vertexId != -1 && otherVertexId != -1) {
            idToWeightedAdjacencies[vertexId]?.put(otherVertexId, cost)
            if (!directed) {
                idToWeightedAdjacencies[otherVertexId]?.put(vertexId, cost)
            }
        }
    }

    fun V.disconnect(vertex: V) {
        val vertexId = this.getId()
        val otherVertexId = vertex.getId()
        if (vertexId != -1 && otherVertexId != -1) {
            idToWeightedAdjacencies[vertexId]?.remove(otherVertexId)
            if (!directed) {
                idToWeightedAdjacencies[otherVertexId]?.remove(vertexId)
            }
        }
    }

    fun vertexIterator(): Iterator<V> = vertexToIdMap.keys.iterator()

    override fun iterator() = vertexIterator()

    fun V.adjacencies(): Iterator<Pair<V, Double>> {
        val adjacentIds: Set<Map.Entry<Int, Double>> = idToWeightedAdjacencies[this.getId()]?.entries ?: emptySet()
        return object : Iterator<Pair<V, Double>> {
            val internalIterator = adjacentIds.iterator()
            override fun hasNext(): Boolean = internalIterator.hasNext()
            override fun next(): Pair<V, Double> {
                val (id, cost) = internalIterator.next()
                val vertex = idToVertexMap[id] ?: throw NoSuchElementException()
                return Pair(vertex, cost)
            }
        }
    }

    fun V.isolate() = this.adjacencies().forEach { disconnect(it.first) }

    fun V.isConnectedWith(vertex: V): Boolean = idToWeightedAdjacencies[this.getId()]?.contains(vertex.getId()) ?: false ||
            (!directed && idToWeightedAdjacencies[vertex.getId()]?.contains(this.getId()) ?: false)


}