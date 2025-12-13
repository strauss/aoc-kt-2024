package aoc_2024

import aoc_util.PrimitiveMultiDimArray
import aoc_util.graph.Dijkstra
import aoc_util.graph.WeightedGraph
import aoc_util.parseInputAsMultiDimArray
import aoc_util.readInput2024
import de.dreamcube.hornet_queen.list.PrimitiveIntArrayList
import de.dreamcube.hornet_queen.map.HashTableBasedMapBuilder
import de.dreamcube.hornet_queen.set.PrimitiveIntSetB
import kotlin.math.min

private const val wall = '#'
private const val free = '.'
private const val start = 'S'
private const val target = 'E'
private const val marker = 'O'
private const val stepCost = 1.0
private const val turnCost = 1000.0


fun main() {
    val testInput = readInput2024("Day16_test")
    val testMaze = parseInputAsMultiDimArray(testInput)
    val testGraph = assembleGraph(testMaze)
    val testResult = findCheapestCost(testMaze, testGraph)
    println("Test result: $testResult")
    val testPathFields = findBestSeats(testMaze, testGraph)
    println("Test path fields: $testPathFields")

    val input = readInput2024("Day16")
    val maze = parseInputAsMultiDimArray(input)
    val graph = assembleGraph(maze)
    val result = findCheapestCost(maze, graph)
    println("Result: $result")
    val pathFields = findBestSeats(maze, graph)
    println("Path fields: $pathFields")
}

private fun findCheapestCost(maze: PrimitiveMultiDimArray<Char>, graph: WeightedGraph<Vertex>): Int {
    val (startRow, startCol) = searchFor(maze, start)
    val (targetRow, targetCol) = searchFor(maze, target)
    graph.run {
        val dijkstra = Dijkstra(graph)
        dijkstra.execute(Vertex(startRow, startCol, Direction.EAST))
        val target1 = Vertex(targetRow, targetCol, Direction.NORTH)
        val cost1 = dijkstra.distance[target1.getId()] ?: Double.POSITIVE_INFINITY
        val target2 = Vertex(targetRow, targetCol, Direction.EAST)
        val cost2 = dijkstra.distance[target2.getId()] ?: Double.POSITIVE_INFINITY
        return min(cost1, cost2).toInt()
    }
}

private fun findBestSeats(maze: PrimitiveMultiDimArray<Char>, graph: WeightedGraph<Vertex>): Int {
    val (startRow, startCol) = searchFor(maze, start)
    val (targetRow, targetCol) = searchFor(maze, target)
    return graph.run {
        val dijkstra = Dijkstra(graph)
        val visitor = DijkstraExtension(dijkstra)
        dijkstra.execute(Vertex(startRow, startCol, Direction.EAST), visitor)
        val target1 = Vertex(targetRow, targetCol, Direction.NORTH)
        val cost1 = dijkstra.distance[target1.getId()] ?: Double.POSITIVE_INFINITY
        val target2 = Vertex(targetRow, targetCol, Direction.EAST)
        val cost2 = dijkstra.distance[target2.getId()] ?: Double.POSITIVE_INFINITY

        // determine path
        val target = if (cost1 < cost2) target1 else target2
        val fieldsOfBestPaths =
            fieldsOfBestPaths(this, visitor.extendedParent, target, PrimitiveIntSetB(), setOf(Pair(startRow, startCol)))
        fieldsOfBestPaths.size
    }
}

private fun fieldsOfBestPaths(
    graph: WeightedGraph<Vertex>,
    parent: Map<Int, MutableList<Int>>,
    target: Vertex,
    alreadyVisited: MutableSet<Int>,
    fields: Set<Pair<Int, Int>>
): Set<Pair<Int, Int>> {
    return graph.run {
        alreadyVisited.add(target.getId())
        val currentParent = parent[target.getId()] ?: return fields
        val result = mutableSetOf<Pair<Int, Int>>()
        result.addAll(fields)
        result.add(Pair(target.row, target.col))
        for (nextParent in currentParent) {
            val nextVertex: Vertex = nextParent.getVertex() ?: continue
            if (!alreadyVisited.contains(nextVertex.getId())) {
                val fieldsOfParent = fieldsOfBestPaths(graph, parent, nextVertex, alreadyVisited, fields)
                result.addAll(fieldsOfParent)
            }
        }
        result
    }
}

private class DijkstraExtension<V>(dijkstra: Dijkstra<V>) : Dijkstra.DijkstraVisitor<V>(dijkstra) {
    val extendedParent: MutableMap<Int, MutableList<Int>> =
        HashTableBasedMapBuilder.useIntKey().useArbitraryTypeValue<MutableList<Int>>().create()

    override fun visitEdge(from: V, to: V, weight: Double) {
        dijkstra.graph.run {
            val newDistance: Double = (dijkstra.distance[from.getId()] ?: 0.0) + weight
            val adjacentDistance = (dijkstra.distance[to.getId()] ?: Double.POSITIVE_INFINITY)
            // if they are the same, we add this edge to the parent list
            when {
                adjacentDistance == newDistance -> {
                    extendedParent.getOrPut(to.getId()) { PrimitiveIntArrayList() }.add(from.getId())
                }

                adjacentDistance > newDistance -> {
                    val replacementList = PrimitiveIntArrayList()
                    replacementList.add(from.getId())
                    extendedParent[to.getId()] = replacementList
                }

                else -> {
                    // new segment is worse so we ignore it
                }
            }
        }
    }
}

private fun assembleGraph(maze: PrimitiveMultiDimArray<Char>): WeightedGraph<Vertex> {
    val graph = WeightedGraph<Vertex>(false)
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)

    graph.run {
        for (row in 0..<height) {
            for (col in 0..<width) {
                if (maze[row, col] != wall) {
                    // north
                    val toNorth = Vertex(row, col, Direction.NORTH)
                    introduceVertex(toNorth)
                    val topToNorth = Vertex(row - 1, col, Direction.NORTH)
                    if (topToNorth.isContained()) {
                        topToNorth.connect(toNorth, stepCost)
                    }

                    // east
                    val toEast = Vertex(row, col, Direction.EAST)
                    introduceVertex(toEast)
                    toEast.connect(toNorth, turnCost)
                    val prevToEast = Vertex(row, col - 1, Direction.EAST)
                    if (prevToEast.isContained()) {
                        prevToEast.connect(toEast, stepCost)
                    }

                    // south
                    val toSouth = Vertex(row, col, Direction.SOUTH)
                    introduceVertex(toSouth)
                    toSouth.connect(toEast, turnCost)
                    val topToSouth = Vertex(row - 1, col, Direction.SOUTH)
                    if (topToSouth.isContained()) {
                        topToSouth.connect(toSouth, stepCost)
                    }

                    // west
                    val toWest = Vertex(row, col, Direction.WEST)
                    introduceVertex(toWest)
                    toWest.connect(toSouth, turnCost)
                    toWest.connect(toNorth, turnCost)
                    val prevToWest = Vertex(row, col - 1, Direction.WEST)
                    if (prevToWest.isContained()) {
                        prevToWest.connect(toWest, turnCost)
                    }

                }
            }
        }
    }
    return graph
}

private data class Vertex(val row: Int, val col: Int, val direction: Direction)

private enum class Direction {
    NORTH, EAST, SOUTH, WEST
}
