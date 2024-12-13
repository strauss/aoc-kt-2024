package aoc_util

import de.dreamcube.hornet_queen.list.PrimitiveLongArrayList
import java.util.*
import kotlin.math.round

fun performRun(
    populationSize: Int,
    genomeSize: Int,
    genomeMaxValue: Long,
    generations: Int,
    elite: Int,
    mutationSigma: Double,
    mutationProbability: Double = 1.0 / genomeSize.toDouble(),
    seed: Long = System.currentTimeMillis(),
    fitnessFunction: (List<Long>) -> Double
): Population<Long> {
    val rng = Random(seed)
    val population: Population<Long> = Population(populationSize, genomeSize, rng, fitnessFunction) { size, innerRng ->
        val out: MutableList<Long> = PrimitiveLongArrayList(size)
        for (i in 0..<size) {
            out.add(round(innerRng.nextDouble() * genomeMaxValue.toDouble()).toLong())
        }
        out
    }
    var currentGeneration = 0
    while (currentGeneration < generations) {
//        println("Generation $currentGeneration - Best fitness: ${population.bestFitness}")
        createNewGenerationInPlace(population, elite, mutationSigma, mutationProbability)
        currentGeneration += 1
    }
    println("Final best fitness: ${population.bestFitness}")
    return population
}

fun createNewGenerationInPlace(
    population: Population<Long>,
    elite: Int,
    sigma: Double,
    mutationProbability: Double = 1.0 / population.genomeSize.toDouble()
) {
    val rng = population.rng
    // create new individuals
    val newIndividuals: MutableList<List<Long>> = ArrayList()
    while (newIndividuals.size < population.populationSize) {
        val partners = population.selectPartnersRandomly(2)
        if (partners.size == 2) {
            val (new1, new2) = onePointCrossover(partners[0], partners[1], rng) { PrimitiveLongArrayList() }
            newIndividuals.add(singleGenomeMutation(new1, sigma, mutationProbability, rng) { PrimitiveLongArrayList() })
            newIndividuals.add(singleGenomeMutation(new2, sigma, mutationProbability) { PrimitiveLongArrayList() })
        }
    }

    val theBest = population.getBest(elite, true)
    population.clear()

    population.addEntries(theBest)
    population.addEntries(newIndividuals)
    population.fitToSize()
}

class Population<G>(
    val populationSize: Int,
    val genomeSize: Int,
    val rng: Random,
    private val fitnessFunction: (List<G>) -> Double,
    private val genomeGenerator: (Int, Random) -> List<G>
) {
    private val entries: MutableList<Entry<G>> = ArrayList()

    // this is a minimizer
    var bestFitness = Double.POSITIVE_INFINITY
        private set
    var worstFitness = Double.NEGATIVE_INFINITY
        private set

    init {
        for (i in 1..populationSize) {
            entries.add(Entry(genomeGenerator(genomeSize, rng), Double.NaN))
        }
        evaluateFitness()
    }

    private fun evaluateFitness(sort: Boolean = true, enforce: Boolean = false) {
        bestFitness = Double.POSITIVE_INFINITY
        worstFitness = Double.NEGATIVE_INFINITY
        entries.forEach { entry ->
            if (enforce || entry.fitness.isNaN()) {
                entry.fitness = fitnessFunction(entry.genome)
            }
            if (entry.fitness < bestFitness) {
                bestFitness = entry.fitness
            }
            if (entry.fitness > worstFitness) {
                worstFitness = entry.fitness
            }
        }
        if (sort) {
            entries.sortBy { it.fitness }
        }
    }

    fun addEntries(entries: List<List<G>>, evaluateFitness: Boolean = false) {
        val laList: List<Entry<G>> = entries.map { Entry(it, Double.NaN) }
        this.entries.addAll(laList)
        if (evaluateFitness) {
            evaluateFitness()
        }
    }

    fun fitToSize() {
        while (entries.size < populationSize) {
            entries.add(Entry(genomeGenerator(genomeSize, rng), Double.NaN))
        }
        evaluateFitness()
        while (entries.size > genomeSize) {
            entries.removeLast()
        }
    }

    fun getBest(amount: Int = 1, evaluateFitness: Boolean = false): List<List<G>> {
        if (amount <= 0) {
            return emptyList()
        }
        if (amount >= entries.size) {
            return entries.map { it.genome }
        }
        val out: MutableList<List<G>> = ArrayList()
        if (evaluateFitness) {
            evaluateFitness()
        }
        for (i in 1..amount) {
            out.add(entries[i].genome)
        }
        return out
    }

    fun selectPartnersRandomly(amount: Int = 2, from: Int = 10): List<List<G>> {
        val pool: MutableSet<Entry<G>> = HashSet()
        for (i in 1..from) {
            pool.add(entries[rng.nextInt(entries.size)])
        }
        val poolList = ArrayList(pool)
        poolList.sortBy { it.fitness }
        val outList = ArrayList<List<G>>()
        var i = 0
        val poolIterator = poolList.iterator()
        while (poolIterator.hasNext() && i < amount) {
            outList.add(poolIterator.next().genome)
            i += 1
        }
        return outList
    }

    fun clear() = entries.clear()

    data class Entry<G>(val genome: List<G>, var fitness: Double = Double.NaN)
}

fun singleGenomeMutation(
    genome: List<Long>,
    sigma: Double,
    probability: Double,
    rng: Random = Random(),
    genomeConstructor: () -> MutableList<Long> = { PrimitiveLongArrayList() }
): List<Long> {
    val result: MutableList<Long> = genomeConstructor()
    for (long in genome) {
        val mutate: Boolean = rng.nextDouble() < probability
        val delta: Long
        if (mutate) {
            val gaussian: Double = rng.nextGaussian()
            val derivation: Double = gaussian * sigma
            delta = round(long.toDouble() + derivation).toLong()
        } else {
            delta = 0L
        }
        result.add(long + delta)
    }
    return result
}


fun <G> onePointCrossover(
    first: List<G>,
    second: List<G>,
    rng: Random = Random(),
    genomeConstructor: () -> MutableList<G> = { ArrayList() }
): Pair<List<G>, List<G>> {
    if (first.size != second.size) {
        throw IllegalArgumentException("Both lists require the same size.")
    }
    val size = first.size
    val firstResult: MutableList<G> = genomeConstructor()
    val secondResult: MutableList<G> = genomeConstructor()
    val crossoverPoint = rng.nextInt(size)
    for (i in 0..<size) {
        if (i < crossoverPoint) {
            firstResult.add(first[i])
            secondResult.add(second[i])
        } else {
            firstResult.add(second[i])
            secondResult.add(first[i])
        }
    }
    return Pair(firstResult, secondResult)
}

fun main() {
    val first = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).map(Int::toLong)
    val second = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0).map(Int::toLong)
    val (third, fourth) = onePointCrossover(first, second)
    val fifth = singleGenomeMutation(third, 5.0, 0.5)
    val sixth = singleGenomeMutation(fourth, 500.0, 0.5)
    println(first)
    println(second)
    println()
    println(third)
    println(fifth)
    println()
    println(fourth)
    println(sixth)
}