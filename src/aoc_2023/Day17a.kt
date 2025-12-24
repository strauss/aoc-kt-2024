package aoc_2023

import aoc_util.Coordinate
import aoc_util.Primitive2DCharArray
import aoc_util.graph.Dijkstra
import aoc_util.graph.WeightedGraph
import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day17_test")
    val testInput = Primitive2DCharArray.parseFromLines(testLines)
    solve("Test result", testInput, ::solveWithDijkstra)

    val graph = makeMeGraph(testInput)

    val lines = readInput2023("Day17")
    val input = Primitive2DCharArray.parseFromLines(lines)
    solve("Result", input, ::solveWithDijkstra)

}

// TODO: write an alternative solution
/*
 * - BFS from starting point with allowed operations in mind (this is the hard part)
 * - Create the graph while searching
 * - vertices with "row, col, dir"
 * - edges with "op, cost"
 */

private fun solveWithDijkstra(
    array: Primitive2DCharArray,
    start: LavaSearchState = LavaSearchState(0, 0, 0, Direction.EAST),
    goal: Coordinate = Coordinate(array.height - 1, array.width - 1)
): Int? {
    val graph = makeMeGraph(array)
    val dijkstra = Dijkstra(graph)
    val goalStatesVisitor = CollectGoalStatesVisitor(dijkstra) { it.row == goal.row && it.col == goal.col }
    dijkstra.execute(start, goalStatesVisitor)
    val costs: Map<Int, Double> = dijkstra.distance
    graph.run {
        return goalStatesVisitor.goalStates.minOfOrNull { costs[it.getId()]!! }?.toInt()
    }
}

private fun makeMeGraph(array: Primitive2DCharArray, maxStraight: Int = 3): WeightedGraph<LavaSearchState> {
    val graph = WeightedGraph<LavaSearchState>(directed = true)

    // create all vertices
    for (row in 0..<array.height) {
        for (col in 0..<array.width) {
            for (s in 0..maxStraight) {
                for (dir in Direction.entries) {
                    val vertex = LavaSearchState(row, col, s, dir)
                    graph.introduceVertex(vertex)
                }
            }
        }
    }

    // create all edges
    graph.run {
        for (vertex in vertexIterator()) {
            val (row, col, s, dir) = vertex
            for (op in Operation.entries) {
                val (nextDir, nextStraight) = nextDirAndStraight(dir, s, op)
                if (nextStraight > maxStraight) {
                    continue
                }
                val (dRow, dCol) = nextDir.delta
                val nextRow = row + dRow
                val nextCol = col + dCol
                if (nextRow in 0..<array.height && nextCol in 0..<array.width) {
                    val adjacentVertex = LavaSearchState(nextRow, nextCol, nextStraight, nextDir)
                    val adjacentVertexId = adjacentVertex.getId()
                    if (adjacentVertexId >= 0) {
                        val cost = array[nextRow, nextCol].digitToInt().toDouble()
                        vertex.connect(adjacentVertex, cost)
                    }
                }
            }
        }
    }

    return graph
}

private class CollectGoalStatesVisitor(
    dijkstra: Dijkstra<LavaSearchState>,
    private val isGoal: (LavaSearchState) -> Boolean
) :
    Dijkstra.DijkstraVisitor<LavaSearchState>(dijkstra) {
    val goalStates: MutableList<LavaSearchState> = ArrayList(16)

    override fun visitVertex(vertex: LavaSearchState) {
        super.visitVertex(vertex)
        if (isGoal(vertex)) {
            goalStates.add(vertex)
        }
    }
}