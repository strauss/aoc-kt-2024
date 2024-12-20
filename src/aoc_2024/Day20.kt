package aoc_2024

import aoc_util.*

fun main() {
    val testInput = readInput2024("Day20_test")
    val testMaze = parseInputAsMultiDimArray(testInput)
    val testResult = countPossibleCheats(testMaze)
    println("Test result: $testResult")
//    val testPathFields = findBestSeats(testMaze, testGraph)
//    println("Test path fields: $testPathFields")
    println()
    val input = readInput2024("Day20")
    val maze = parseInputAsMultiDimArray(input)
    val result = countPossibleCheats(maze, 100.0)
    println("Result: $result")
//    val pathFields = findBestSeats(maze, graph)
//    println("Path fields: $pathFields")
}

private fun countPossibleCheats(maze: PrimitiveMultiDimArray<Char>, atLeast: Double = 20.0): Int {
    val (start, end) = searchStartAndEnd(maze)
    val graph = assembleGraph(maze)
    val (path, distance) = reduceGraph(graph, start, end)
    println("Distance: $distance")
    val possibleCheats = getCheatEdges(graph, path)
    println("Possible cheats: ${possibleCheats.size}")
    val feasibleCheats = getFeasibleCheats(graph, possibleCheats, start, end, distance)
    println("Feasible cheats: ${feasibleCheats.size}")

    return feasibleCheats.asSequence().filter { (distance - it.second) >= atLeast }.count()
}

private fun getFeasibleCheats(
    graph: WeightedGraph<Coordinate>,
    possibleCheats: Set<Pair<Coordinate, Coordinate>>,
    start: Coordinate,
    end: Coordinate,
    regularDistance: Double
): List<Pair<Pair<Coordinate, Coordinate>, Double>> {
    val out = mutableListOf<Pair<Pair<Coordinate, Coordinate>, Double>>()
    for (cheat in possibleCheats) {
        graph.run {
            // activate cheat
            val (c1, c2) = cheat
            c1.connect(c2)

            // perform search
            val dijkstra = Dijkstra(graph)
            dijkstra.execute(start)
            val newDistance: Double = dijkstra.distance[end.getId()] ?: Double.POSITIVE_INFINITY
            if (newDistance < regularDistance) {
                out.add(cheat to newDistance)
            }

            // deactivate cheat for the next run
            c1.disconnect(c2)
        }
    }
    return out
}


private fun getCheatEdges(graph: WeightedGraph<Coordinate>, path: List<Coordinate>): Set<Pair<Coordinate, Coordinate>> {
    val result = mutableSetOf<Pair<Coordinate, Coordinate>>()
    graph.run {
        for (coord in path) {
            val north = coord.getNorth()
            if (!north.isContained()) {
                val northOfNorth = north.getNorth()
                if (northOfNorth.isContained()) {
                    val cheat = coord to northOfNorth
                    if (!result.contains(cheat.getInverse())) {
                        result.add(cheat)
                    }
                }
            }

            val east = coord.getEast()
            if (!east.isContained()) {
                val eastOfEast = east.getEast()
                if (eastOfEast.isContained()) {
                    val cheat = coord to eastOfEast
                    if (!result.contains(cheat.getInverse())) {
                        result.add(cheat)
                    }
                }
            }

            val south = coord.getSouth()
            if (!south.isContained()) {
                val southOfSouth = south.getSouth()
                if (southOfSouth.isContained()) {
                    val cheat = coord to southOfSouth
                    if (!result.contains(cheat.getInverse())) {
                        result.add(cheat)
                    }
                }
            }

            val west = coord.getWest()
            if (!west.isContained()) {
                val westOfWest = west.getWest()
                if (westOfWest.isContained()) {
                    val cheat = coord to westOfWest
                    if (!result.contains(cheat.getInverse())) {
                        result.add(cheat)
                    }
                }
            }
        }
    }
    return result
}

private fun reduceGraph(graph: WeightedGraph<Coordinate>, start: Coordinate, end: Coordinate): Pair<List<Coordinate>, Double> {
    val dijkstra = Dijkstra(graph)
    dijkstra.execute(start)
    val distance = graph.run {
        val maxCost = dijkstra.distance[end.getId()] ?: Double.POSITIVE_INFINITY
        for (coordinate in vertexIterator()) {
            if ((dijkstra.distance[coordinate.getId()] ?: Double.POSITIVE_INFINITY) > maxCost) {
                coordinate.isolate()
            }
        }
        maxCost
    }
    return extractPath(dijkstra, end) to distance
}

private fun extractPath(dijkstra: Dijkstra<Coordinate>, end: Coordinate): List<Coordinate> {
    val result = mutableListOf<Coordinate>()
    dijkstra.graph.run {
        var current: Coordinate? = end
        while (current != null) {
            result.add(current)
            val parentId = dijkstra.parent[current.getId()]
            current = parentId?.getVertex()
        }
    }
    return result.reversed()
}

private fun searchStartAndEnd(maze: PrimitiveMultiDimArray<Char>): Pair<Coordinate, Coordinate> {
    val (srow, scol) = searchFor(maze, 'S')
    val (erow, ecol) = searchFor(maze, 'E')
    return Pair(Coordinate(srow, scol), Coordinate(erow, ecol))
}

private fun assembleGraph(maze: PrimitiveMultiDimArray<Char>): WeightedGraph<Coordinate> {
    val height = maze.getDimensionSize(0)
    val width = maze.getDimensionSize(1)
    val graph = WeightedGraph<Coordinate>()
    graph.run {
        for (row in 0..<height) {
            for (col in 0..<width) {
                if (maze[row, col] != '#') {
                    val current = Coordinate(row, col)
                    introduceVertex(current)
                    val north = current.getNorth()
                    if (row > 0 && maze[north.row, north.col] != '#') {
                        current.connect(north)
                    }
                    val west = current.getWest()
                    if (col > 0 && maze[west.row, west.col] != '#') {
                        current.connect(west)
                    }
                }
            }
        }
    }
    return graph
}

