package aoc_2023

import aoc_util.readInput2023
import aoc_util.size
import aoc_util.solve

fun main() {
    val testLines = readInput2023("Day22_test")
    val testInput = parseInput(testLines)
    solve("Test result", testInput, ::countSaveRemovals)
    solve("Test 2 result", testInput, ::countChainReactions)

    val lines = readInput2023("Day22")
    val input = parseInput(lines)
    solve("Result", input, ::countSaveRemovals)
}

private fun countSaveRemovals(bricks: List<Brick>): Int {
    val space = createBrickSpace(bricks)
    var result = 0
    for (brick in space.bricks) {
        if (space.canBeDisintegrated(brick)) {
            result += 1
        }
    }
    return result
}

private fun countChainReactions(bricks: List<Brick>): Int {
    val space = createBrickSpace(bricks)
    var result = 0
    for (brick in bricks) {
        result += space.getChainReaction(brick).size
    }
    return result
}

private fun createBrickSpace(bricks: List<Brick>): BrickSpace {
    val sortedBricks: List<Brick> = bricks.sortedBy { brick -> brick.zRange.start }
    val space = BrickSpace(0)
    for (brick in sortedBricks) {
        space.dropBrick(brick)
    }
    return space
}

private class BrickSpace(private val groundLevel: Int = 0) {
    private var highestOccupiedLevel = groundLevel

    private val supports: MutableMap<Brick, MutableSet<Brick>> = HashMap()
    private val supportedBy: MutableMap<Brick, MutableSet<Brick>> = HashMap()
    val bricks: MutableSet<Brick> = HashSet()
    private val occupiedCoordinates: MutableMap<Coordinate3D, Brick> = HashMap()
    private val chainReactionCache: MutableMap<Brick, Set<Brick>> = HashMap()

    fun getChainReaction(brick: Brick): Set<Brick> {
        val allSupported: Set<Brick>? = supports[brick]
        if (allSupported.isNullOrEmpty() || canBeDisintegrated(brick)) {
            return emptySet()
        }
        val result: MutableSet<Brick> = HashSet()
        for (brick in allSupported) {
            if ((supportedBy[brick] ?: emptySet()).size == 1) {
                val brickResult = chainReactionCache.computeIfAbsent(brick) { getChainReaction(it) }
                result.addAll(brickResult)
                result.add(brick)
            }
        }
        return result
    }

    /**
     * Checks, if the given brick is supporting others. If so, it also checks, if those bricks are supported by other
     * bricks.
     */
    fun canBeDisintegrated(brick: Brick): Boolean {
        val supported = supports[brick] ?: emptySet()
        for (supportedBrick in supported) {
            val by = supportedBy[supportedBrick] ?: emptySet()
            assert(brick in by)
            if (by.size <= 1) {
                return false
            }
        }
        return true
    }

    fun dropBrick(brick: Brick) {
        var currentLowestZ = brick.zRange.start
        if (currentLowestZ < groundLevel) {
            throw IllegalStateException("Operation not allowed, the bricks needs to start above the ground.")
        }
        if (currentLowestZ == highestOccupiedLevel) {
            for (x in brick.xRange) {
                for (y in brick.yRange) {
                    val checkCoordinate = Coordinate3D(x, y, currentLowestZ)
                    if (checkCoordinate in occupiedCoordinates.keys) {
                        throw IllegalStateException("Operation not allowed, the position $checkCoordinate is already occupied.")
                    }
                }
            }
        }

        currentLowestZ = highestOccupiedLevel
        val collisionBricks: MutableList<Brick> = ArrayList()
        while (currentLowestZ > groundLevel) {
            for (x in brick.xRange) {
                for (y in brick.yRange) {
                    val checkCoordinate = Coordinate3D(x, y, currentLowestZ)
                    val collisionBrick = occupiedCoordinates[checkCoordinate]
                    if (collisionBrick != null) {
                        // collision detected
                        collisionBricks.add(collisionBrick)
                    }
                }
            }
            if (collisionBricks.isNotEmpty()) {
                // on collision, we stop falling down
                break
            }
            currentLowestZ -= 1
        }
        currentLowestZ += 1
        val zRangeSize = brick.zRange.size()
        val droppedBrick = brick.copy(zRange = currentLowestZ..<(currentLowestZ + zRangeSize))
        bricks.add(droppedBrick)
        droppedBrick.getAllCoordinates().forEach {
            if (occupiedCoordinates.containsKey(it)) {
                throw IllegalStateException()
            }
            occupiedCoordinates[it] = droppedBrick
        }
        collisionBricks.forEach {
            val supportSet = supports.computeIfAbsent(it) { HashSet() }
            supportSet.add(droppedBrick)
            val supportedBySet = supportedBy.computeIfAbsent(droppedBrick) { HashSet() }
            supportedBySet.add(it)
        }
        highestOccupiedLevel = highestOccupiedLevel.coerceAtLeast(droppedBrick.zRange.endInclusive)
    }
}

private fun parseInput(lines: List<String>): List<Brick> {
    val splitAtTilde = "~".toRegex()
    val splitAtComma = ",".toRegex()

    val out = ArrayList<Brick>()
    for (line in lines) {
        val split = splitAtTilde.split(line)
        val left = splitAtComma.split(split[0])
        val right = splitAtComma.split(split[1])
        fun createRange(left: Int, right: Int): IntRange {
            return if (left <= right) left..right else right..left
        }

        val brick = Brick(
            createRange(left[0].toInt(), right[0].toInt()),
            createRange(left[1].toInt(), right[1].toInt()),
            createRange(left[2].toInt(), right[2].toInt())
        )
        out.add(brick)
    }
    return out
}

private data class Brick(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    fun getAllCoordinates(): Set<Coordinate3D> = buildSet {
        for (x in xRange) {
            for (y in yRange) {
                for (z in zRange) {
                    add(Coordinate3D(x, y, z))
                }
            }
        }
    }
}

private data class Coordinate3D(val x: Int, val y: Int, val z: Int)