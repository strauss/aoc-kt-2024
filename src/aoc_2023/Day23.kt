package aoc_2023

import aoc_util.Coordinate
import aoc_util.graph.BitSetAdjacencyBasedGraph
import aoc_util.readInput2023
import aoc_util.solve
import java.util.*

fun main() {
    val testLines = readInput2023("Day23_test")
    val testInput = parseInput(testLines)
    val testGraph = mazeToGraph(testInput)
    val testNoSlopesGraph = mazeToGraph(testInput, ignoreSlopes = true)

    solve("Test result", testGraph) {
        searchLongestPath(it, testInput.start, testInput.goal)
    }
    solve("Test 2 result", testNoSlopesGraph) {
        searchLongestPath(it, testInput.start, testInput.goal)
    }
    solve("Test 2 result", testNoSlopesGraph) {
        searchLongestPathWithClustering(it, testInput.start, testInput.goal)
    }

    val lines = readInput2023("Day23")
    val input = parseInput(lines)
    val graph = mazeToGraph(input)
    val noSlopesGraph = mazeToGraph(input, ignoreSlopes = true)

    solve("Result", graph) {
        searchLongestPath(it, input.start, input.goal)
    }
    solve("Result", noSlopesGraph) {
        searchLongestPathWithClustering(it, input.start, input.goal)
    }

}

private fun searchLongestPathWithClustering(
    graph: BitSetAdjacencyBasedGraph<Coordinate>,
    start: Coordinate,
    goal: Coordinate
): Int {
    val clustering = Clustering(graph)
    clustering.calculateClusterGraph(start)
    val startCluster = clustering.getClusterByCoordinate(start)
    val goalCluster = clustering.getClusterByCoordinate(goal)
    return clusterSearch(clustering.clusterGraph, startCluster!!, goalCluster!!) - 1 // the start vertex does not count
}

private fun clusterSearch(
    clusterGraph: BitSetAdjacencyBasedGraph<BitSet>,
    startCluster: BitSet,
    goalCluster: BitSet,
    lengthSoFar: Int = 0,
    visitedOnPath: BitSet = BitSet()
): Int {
    if (startCluster == goalCluster) {
        return lengthSoFar + goalCluster.cardinality()
    }
    val results = ArrayList<Int>()
    clusterGraph.run {
        val aIterator = startCluster.adjacencies()
        while (aIterator.hasNext()) {
            val omega = aIterator.next()
            val omegaId = omega.getId()
            val newVisitedOnPath = visitedOnPath.clone() as BitSet
            newVisitedOnPath[startCluster.getId()] = true
            if (!visitedOnPath[omegaId]) {
                val res = clusterSearch(
                    clusterGraph,
                    omega,
                    goalCluster,
                    lengthSoFar + startCluster.cardinality(),
                    newVisitedOnPath
                )
                if (res >= 0) {
                    results.add(res)
                }
            }
        }
    }
    return results.maxOrNull() ?: -1
}

private class Clustering(val graph: BitSetAdjacencyBasedGraph<Coordinate>) {
    private val detectedJunctions: BitSet = BitSet()
    private val processedJunctions: BitSet = BitSet()
    private val processedVertices: BitSet = BitSet()

    val clusterGraph = BitSetAdjacencyBasedGraph<BitSet>(directed = false)

    fun getClusterByCoordinate(coordinate: Coordinate?): BitSet? {
        if (coordinate == null) {
            return null
        }
        val vIterator = clusterGraph.vertexIterator()
        while (vIterator.hasNext()) {
            val cluster = vIterator.next()
            graph.run {
                if (cluster[coordinate.getId()]) {
                    return cluster
                }
            }
        }
        return null
    }

    fun calculateClusterGraph(start: Coordinate) {
        // calculate first cluster and initialize the higher order search
        val (startCluster, firstJunctionCluster) = detectCluster(start)
        clusterGraph.introduceVertex(startCluster)

        // Stores the source cluster and the next junction to process
        val workBuffer = ArrayDeque<Pair<BitSet, Int>>()

        fun processJunction(sourceCluster: BitSet, junction: BitSet) {
            val junctionId = junction.nextSetBit(0)
            graph.run {
                val junctionVertex = get(junctionId)!!
                val aIterator = junctionVertex.adjacencies()
                while (aIterator.hasNext()) {
                    val next = aIterator.next()
                    if (!processedVertices[next.getId()]) {
                        workBuffer.addLast(junction to next.getId())
                    }
                }
                processedJunctions[junctionVertex.getId()] = true
                processedVertices[junctionVertex.getId()] = true
            }
        }

        if (firstJunctionCluster != null) {
            processJunction(startCluster, firstJunctionCluster)
            clusterGraph.run {
                startCluster.connect(firstJunctionCluster)
            }
        }

        // perform the search and build the cluster graph while doing so
        while (workBuffer.isNotEmpty()) {
            val (sourceJunctionCluster: BitSet, nextStartId: Int) = workBuffer.removeFirst()
            if (processedVertices[nextStartId]) {
                val nextStartCoordinate: Coordinate? = graph[nextStartId]
                val nextCluster: BitSet? = getClusterByCoordinate(nextStartCoordinate)
                if (nextCluster != null) {
                    clusterGraph.run {
                        sourceJunctionCluster.connect(nextCluster)
                    }
                }
                continue
            }
            val (nextCluster, nextJunctionCluster) = detectCluster(graph[nextStartId]!!)
            clusterGraph.run {
                sourceJunctionCluster.connect(nextCluster)
            }
            if (nextJunctionCluster != null) {
                processJunction(nextCluster, nextJunctionCluster)
                clusterGraph.run {
//                    sourceCluster.connect(nextJunction)
                    nextCluster.connect(nextJunctionCluster)
                }
            }
        }
    }

    private fun detectCluster(clusterStart: Coordinate): Pair<BitSet, BitSet?> {
        val currentCluster = BitSet()
        var nextJunction: BitSet? = null
        graph.run {
            var currentCoordinate = clusterStart
            while (true) {
                val relevantAdjacencies = ArrayList<Coordinate>()
                val aIterator = currentCoordinate.adjacencies()
                while (aIterator.hasNext()) {
                    val nextCoordinate = aIterator.next()
                    if (!processedVertices[nextCoordinate.getId()]) {
                        relevantAdjacencies.add(nextCoordinate)
                    }
                }
                if (relevantAdjacencies.isEmpty()) {
                    // this would imply a dead end
                    processedVertices[currentCoordinate.getId()] = true
                    currentCluster[currentCoordinate.getId()] = true
                    break
                }
                if (relevantAdjacencies.size == 1) {
                    processedVertices[currentCoordinate.getId()] = true
                    currentCluster[currentCoordinate.getId()] = true
                    currentCoordinate = relevantAdjacencies[0]
                } else if (relevantAdjacencies.size > 1) {
                    nextJunction = BitSet()
                    nextJunction[currentCoordinate.getId()] = true
                    detectedJunctions[currentCoordinate.getId()] = true
                    break
                }
            }

        }
        return currentCluster to nextJunction
    }
}

private fun searchLongestPath(
    graph: BitSetAdjacencyBasedGraph<Coordinate>,
    start: Coordinate,
    goal: Coordinate,
    depth: Int = 0,
    visitedOnPath: BitSet = BitSet()
): Int {
    if (start == goal) {
        return depth
    }
    val results = ArrayList<Int>()
    graph.run {
        val aIterator = start.adjacencies()
        while (aIterator.hasNext()) {
            val omega = aIterator.next()
            val omegaId = omega.getId()
            val newVisitedOnPath = visitedOnPath.clone() as BitSet
            newVisitedOnPath[start.getId()] = true
            if (!visitedOnPath[omegaId]) {
                val res = searchLongestPath(graph, omega, goal, depth + 1, newVisitedOnPath)
                if (res >= 0) {
                    results.add(res)
                }
            }
        }
    }
    return results.maxOrNull() ?: -1
}


private fun mazeToGraph(maze: RawMaze, ignoreSlopes: Boolean = false): BitSetAdjacencyBasedGraph<Coordinate> {
    val graph = BitSetAdjacencyBasedGraph<Coordinate>(directed = true)
    // first, the vertices ... they are the easy part
    val pathPositions = maze.pathPositions
    for (coordinate in pathPositions.keys) {
        graph.introduceVertex(coordinate)
    }

    // now the edges
    graph.run {
        val iterator = vertexIterator()
        fun connectIfOmegaExists(currentAlpha: Coordinate, nextOmega: Coordinate) {
            if (nextOmega.getId() >= 0) {
                currentAlpha.connect(nextOmega)
            }
        }

        while (iterator.hasNext()) {
            val currentAlpha: Coordinate = iterator.next()
            val slope = pathPositions[currentAlpha] ?: Slope.NONE
            if (ignoreSlopes || slope == Slope.NONE || slope == Slope.NORTH) {
                val nextOmega = currentAlpha.getNorth()
                connectIfOmegaExists(currentAlpha, nextOmega)
            }
            if (ignoreSlopes || slope == Slope.NONE || slope == Slope.EAST) {
                val nextOmega = currentAlpha.getEast()
                connectIfOmegaExists(currentAlpha, nextOmega)
            }
            if (ignoreSlopes || slope == Slope.NONE || slope == Slope.SOUTH) {
                val nextOmega = currentAlpha.getSouth()
                connectIfOmegaExists(currentAlpha, nextOmega)
            }
            if (ignoreSlopes || slope == Slope.NONE || slope == Slope.WEST) {
                val nextOmega = currentAlpha.getWest()
                connectIfOmegaExists(currentAlpha, nextOmega)
            }
        }
    }

    return graph
}

private fun parseInput(lines: List<String>): RawMaze {
    val height = lines.size
    var width = Integer.MIN_VALUE
    val pathChars = setOf('.', '^', '>', '<', 'v')
    val pathPositions = LinkedHashMap<Coordinate, Slope>()
    var biggestRow = Integer.MIN_VALUE
    var biggestCol = Integer.MIN_VALUE
    for (row in lines.indices) {
        val line = lines[row]
        width = width.coerceAtLeast(line.length)
        for (col in line.indices) {
            val char = line[col]
            if (char in pathChars) {
                biggestRow = biggestRow.coerceAtLeast(row)
                biggestCol = biggestCol.coerceAtLeast(col)
                val current = Coordinate(row, col)
                pathPositions[current] = Slope.fromChar(char)
            }
        }
    }
    return RawMaze(pathPositions, Coordinate(0, 1), Coordinate(biggestRow, biggestCol), height, width)
}

private class RawMaze(
    val pathPositions: Map<Coordinate, Slope>,
    val start: Coordinate,
    val goal: Coordinate,
    val height: Int,
    val width: Int
)

private enum class Slope {
    NONE, NORTH, EAST, SOUTH, WEST;

    companion object {
        fun fromChar(char: Char): Slope = when (char) {
            '^' -> NORTH
            '>' -> EAST
            'v' -> SOUTH
            '<' -> WEST
            else -> NONE
        }
    }
}