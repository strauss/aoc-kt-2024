package aoc_2023

import aoc_util.createPairs
import aoc_util.extractSchlong
import aoc_util.readInput2023
import kotlin.math.min

fun main() {
    val testInput = readInput2023("Day05_test")
    val testArrangement = parseInput(testInput)
    val testLocations = testArrangement.mapSeedsToLocations()
    println("Test result: ${testLocations.min()}")
    val moreSeedsTestResult = testArrangement.searchLocationsBackwardsAndFindMinimum()
    println("Test more result: $moreSeedsTestResult")

    val input = readInput2023("Day05")
    val arrangement = parseInput(input)
    val locations = arrangement.mapSeedsToLocations()
    println("Result: ${locations.min()}")
    val moreSeedsResult = arrangement.searchLocationsBackwardsAndFindMinimum()
    println("More result: $moreSeedsResult")
}

enum class ParsingState {
    INIT, S2S, S2F, F2W, W2L, L2T, T2H, H2L
}

private fun parseInput(input: List<String>): PuzzleArrangement {
    var seeds: List<Long> = emptyList()
    val seedToSoil: MutableList<MappingDescription> = mutableListOf()
    val soilToFertilizer: MutableList<MappingDescription> = mutableListOf()
    val fertilizerToWater: MutableList<MappingDescription> = mutableListOf()
    val waterToLight: MutableList<MappingDescription> = mutableListOf()
    val lightToTemperature: MutableList<MappingDescription> = mutableListOf()
    val temperatureToHumidity: MutableList<MappingDescription> = mutableListOf()
    val humidityToLocation: MutableList<MappingDescription> = mutableListOf()
    var state = ParsingState.INIT

    for (line in input) {
        if (line.trim().isEmpty()) {
            continue
        }
        if (state == ParsingState.INIT && line.startsWith("seeds: ")) {
            seeds = line.extractSchlong()
            continue
        }
        state = when {
            line.startsWith("seed-to") -> ParsingState.S2S
            line.startsWith("soil-to") -> ParsingState.S2F
            line.startsWith("fertilizer-to") -> ParsingState.F2W
            line.startsWith("water-to") -> ParsingState.W2L
            line.startsWith("light-to") -> ParsingState.L2T
            line.startsWith("temperature-to") -> ParsingState.T2H
            line.startsWith("humidity-to") -> ParsingState.H2L
            else -> state
        }

        val longs = line.extractSchlong()
        if (longs.size != 3) {
            continue
        }
        val mapping = MappingDescription(longs[0], longs[1], longs[2])
        when (state) {
            ParsingState.S2S -> seedToSoil.add(mapping)
            ParsingState.S2F -> soilToFertilizer.add(mapping)
            ParsingState.F2W -> fertilizerToWater.add(mapping)
            ParsingState.W2L -> waterToLight.add(mapping)
            ParsingState.L2T -> lightToTemperature.add(mapping)
            ParsingState.T2H -> temperatureToHumidity.add(mapping)
            ParsingState.H2L -> humidityToLocation.add(mapping)
            else -> {
                // nothing
            }
        }
    }
    return PuzzleArrangement(
        seeds,
        seedToSoil,
        soilToFertilizer,
        fertilizerToWater,
        waterToLight,
        lightToTemperature,
        temperatureToHumidity,
        humidityToLocation
    )
}

private data class PuzzleArrangement(
    val seeds: List<Long>,
    val seedToSoil: List<MappingDescription>,
    val soilToFertilizer: List<MappingDescription>,
    val fertilizerToWater: List<MappingDescription>,
    val waterToLight: List<MappingDescription>,
    val lightToTemperature: List<MappingDescription>,
    val temperatureToHumidity: List<MappingDescription>,
    val humidityToLocation: List<MappingDescription>
) {
    fun mapSeedsToLocations(): List<Long> = mapSeedsToLocations(seeds)

    fun mapSeedRangesToLocations(): Long {
        var minimumLocation = Long.MAX_VALUE
        val ranges = seeds.createPairs().map { it.first..<it.first + it.second }
        for (range in ranges) {
            for (seed in range) {
                val location = mapSeedToLocation(seed)
                minimumLocation = min(minimumLocation, location)
            }
        }
        return minimumLocation
    }

    fun searchLocationsBackwardsAndFindMinimum(): Long {
        val seedRanges = seeds.createPairs().map { it.first..<it.first + it.second }
        val humidityToLocationCopy = ArrayList(humidityToLocation)
        humidityToLocationCopy.sortBy { it.destination }
        val maxDestination = humidityToLocationCopy.last().destinationRange.last

        for (location in 0..maxDestination) {
            val seed = mapLocationToSeed(location)
            if (seedRanges.any { range -> range.contains(seed) }) {
                return location
            }
        }

        return Long.MAX_VALUE
    }

    private fun mapLocationToSeed(location: Long): Long = seedToSoil.mapBackwards(
        soilToFertilizer.mapBackwards(
            fertilizerToWater.mapBackwards(
                waterToLight.mapBackwards(
                    lightToTemperature.mapBackwards(
                        temperatureToHumidity.mapBackwards(
                            humidityToLocation.mapBackwards(location)
                        )
                    )
                )
            )
        )
    )

    private fun mapSeedToLocation(seed: Long): Long = humidityToLocation.mapForwards(
        temperatureToHumidity.mapForwards(
            lightToTemperature.mapForwards(
                waterToLight.mapForwards(
                    fertilizerToWater.mapForwards(
                        soilToFertilizer.mapForwards(
                            seedToSoil.mapForwards(seed)
                        )
                    )
                )
            )
        )
    )

    private fun mapSeedsToLocations(seeds: List<Long>): List<Long> = seeds.asSequence()
        .map { seedToSoil.mapForwards(it) }
        .map { soilToFertilizer.mapForwards(it) }
        .map { fertilizerToWater.mapForwards(it) }
        .map { waterToLight.mapForwards(it) }
        .map { lightToTemperature.mapForwards(it) }
        .map { temperatureToHumidity.mapForwards(it) }
        .map { humidityToLocation.mapForwards(it) }
        .toList()

    private fun List<MappingDescription>.mapForwards(value: Long): Long = asSequence()
        .map { it.mapForwards(value) }
        .firstOrNull { it != value } ?: value

    private fun List<MappingDescription>.mapBackwards(value: Long): Long = asSequence()
        .map { it.mapBackwards(value) }
        .firstOrNull { it != value } ?: value
}

private data class MappingDescription(val destination: Long, val source: Long, val size: Long) {
    val destinationRange
        get() = destination..<(destination + size)
    val sourceRange
        get() = source..<(source + size)

    fun mapForwards(value: Long): Long = if (value in sourceRange) destination + value - source else value

    fun mapBackwards(value: Long): Long = if (value in destinationRange) source + value - destination else value
}

private fun tryMapping() {
    val md = MappingDescription(50, 100, 16)
    println("5 -> ${md.mapForwards(5)} -> ${md.mapBackwards(md.mapForwards(5))}")
    println("100 -> ${md.mapForwards(100)} -> ${md.mapBackwards(md.mapForwards(100))}")
    println("105 -> ${md.mapForwards(105)} -> ${md.mapBackwards(md.mapForwards(105))}")
    println("115 -> ${md.mapForwards(115)} -> ${md.mapBackwards(md.mapForwards(115))}")
    println("116 -> ${md.mapForwards(116)} -> ${md.mapBackwards(md.mapForwards(116))}")
}
