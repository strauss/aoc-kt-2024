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
    solve("Test result", testInput, ::solveNormalCrucible)
    solve("Test 2 result", testInput, ::solveUltraCrucible)

    val lines = readInput2023("Day17")
    val input = Primitive2DCharArray.parseFromLines(lines)
    solve("Result", input, ::solveNormalCrucible)
    solve("Result 2", input, ::solveUltraCrucible)

}

private fun solveUltraCrucible(array: Primitive2DCharArray): Int? {
    val graph = makeMeGraph(array, 4, 10)
    val start = LavaSearchState(0, 0, 0, Direction.EAST)
    val goal = Coordinate(array.height - 1, array.width - 1)
    return solveWithDijkstra(graph, start, goal)
}

private fun solveNormalCrucible(array: Primitive2DCharArray): Int? {
    val graph = makeMeGraph(array)
    val start = LavaSearchState(0, 0, 0, Direction.EAST)
    val goal = Coordinate(array.height - 1, array.width - 1)
    return solveWithDijkstra(graph, start, goal)
}

private fun solveWithDijkstra(
    graph: WeightedGraph<LavaSearchState>,
    start: LavaSearchState,
    goal: Coordinate
): Int? {
    val dijkstra = Dijkstra(graph)
    val goalStatesVisitor = CollectGoalStatesVisitor(dijkstra) { it.row == goal.row && it.col == goal.col }
    dijkstra.execute(start, goalStatesVisitor)
    val costs: Map<Int, Double> = dijkstra.distance
    graph.run {
        return goalStatesVisitor.goalStates.minOfOrNull { costs[it.getId()]!! }?.toInt()
    }
}

private fun makeMeGraph(
    array: Primitive2DCharArray,
    minStraight: Int = 0,
    maxStraight: Int = 3
): WeightedGraph<LavaSearchState> {
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
                if (s < minStraight && op != Operation.FORWARD) {
                    continue
                }
                if (s >= maxStraight && op == Operation.FORWARD) {
                    continue
                }
                val (nextDir, nextStraight) = nextDirAndStraight(dir, s, op)
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