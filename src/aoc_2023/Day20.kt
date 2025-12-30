package aoc_2023

import aoc_util.readInput2023
import aoc_util.solve

fun main() {
    val testSimpleLines = readInput2023("Day20_test_simple")
    val testSimpleInput = parseInput(testSimpleLines)
    solve("Test result (simple)", testSimpleInput, ::simulate)
    val testComplexLines = readInput2023("Day20_test_complex")
    val testComplexInput = parseInput(testComplexLines)
    solve("Test result (complex)", testComplexInput, ::simulate)

    val lines = readInput2023("Day20")
    val input = parseInput(lines)
    solve("Result", input, ::simulate)
    val input2 = parseInput(lines)
    solve("Result", input2) {
        simulate2(it, 3769)
    }

}

private fun simulate(modules: Map<String, Module>, buttonPresses: Int = 1000): Long {
    val counter = SignalCounter()
    for (i in 1..buttonPresses) {
        simulateButtonPress(modules, counter)
    }
    return counter.result
}

private fun simulate2(modules: Map<String, Module>, buttonPresses: Int = 1000): Long {
    val counter = SignalCounter()

    val conjuctionModules: List<ConjunctionModule> = modules.values.asSequence()
        .filter { it is ConjunctionModule }
        .map { it as ConjunctionModule }
        .toList()

    fun printConjunctionModules() {
        for (conjunctionModule in conjuctionModules) {
            println("${conjunctionModule.label}: ${conjunctionModule.storedInputSignals}")
        }
    }

    println("Initial")
    printConjunctionModules()
    println("-----------")
    for (i in 1..buttonPresses) {
        println(i)
        simulateButtonPress(modules, counter)
        printConjunctionModules()
        println("-----------")
    }

    return counter.result
}

private fun simulateButtonPress(modules: Map<String, Module>, counter: SignalCounter) {
    counter.onLowLignal() // the button press triggers a low signal to the broadcaster
    val workList = ArrayDeque<Pair<String, Map<String, Signal>>>()
    val broadcaster = modules["broadcaster"]!!
    val outputSignals = broadcaster.createOutputSignals("", Signal.LOW) // the parameters actually don't matter atm
    workList.addLast(broadcaster.label to outputSignals)
    while (workList.isNotEmpty()) {
        val currentWorkItem: Pair<String, Map<String, Signal>> = workList.removeFirst()
        val (sourceLabel, currentSignals) = currentWorkItem
        for ((targetLabel: String, signal: Signal) in currentSignals) {
            val targetModule = modules[targetLabel]!!
            if (signal == Signal.LOW) {
                counter.onLowLignal()
            } else if (signal == Signal.HIGH) {
                counter.onHighSignal()
            }
            val nextSignals = targetModule.createOutputSignals(sourceLabel, signal)
            if (nextSignals.isNotEmpty()) {
                workList.addLast(targetLabel to nextSignals)
            }
        }
    }
}

private class SignalCounter {
    var highSignals = 0L
        private set
    var lowSignals = 0L
        private set

    val result: Long
        get() = highSignals * lowSignals

    fun onHighSignal() {
        highSignals += 1
    }

    fun onLowLignal() {
        lowSignals += 1
    }
}

private fun parseInput(lines: List<String>): Map<String, Module> {
    val out = HashMap<String, Module>()

    val splitPattern = " -> ".toRegex()
    val listSplitLabel = ", ".toRegex()

    // parse the modules
    for (line in lines) {
        val split = splitPattern.split(line)
        val typeAndLabel = split[0]
        val outputIdsAsString = split[1]
        val outputIdsAsList: List<String> = listSplitLabel.split(outputIdsAsString)
        val outputIdsAsSet = LinkedHashSet<String>()
        outputIdsAsList.forEach { outputIdsAsSet.add(it) }
        if (typeAndLabel == "broadcaster") {
            val label = typeAndLabel
            out[typeAndLabel] = BroadcastModule(label, outputIdsAsSet)
            continue
        }
        val type: Char = typeAndLabel[0]
        val label: String = typeAndLabel.substring(1..typeAndLabel.lastIndex)
        val module: Module = when (type) {
            '%' -> FlipFlopModule(label, outputIdsAsSet)
            '&' -> ConjunctionModule(label, outputIdsAsSet)
            else -> throw IllegalStateException()
        }
        out[label] = module
    }

    // register all modules for conjunctions
    val debugModules = ArrayList<DebugModule>()
    for (module: Module in out.values) {
        val label = module.label
        for (outLabel: String in module.outputModuleLabels) {
            val targetModule = out[outLabel]
            if (targetModule == null) {
                debugModules.add(DebugModule(outLabel, out))
                continue
            }
            if (targetModule is ConjunctionModule) {
                targetModule.registerInputModule(label)
            }
        }
    }
    debugModules.forEach { out[it.label] = it }

    return out
}

private enum class Signal {
    HIGH, LOW
}

private abstract class Module(val label: String, val outputModuleLabels: Set<String>) {
    abstract fun createOutputSignals(inputId: String, inputSignal: Signal): Map<String, Signal>
}

private class BroadcastModule(label: String, outputModuleIds: Set<String>) : Module(label, outputModuleIds) {

    override fun createOutputSignals(inputId: String, inputSignal: Signal): Map<String, Signal> = buildMap {
        // all receivers get a low signal
        for (outLabel in outputModuleLabels) {
            put(outLabel, Signal.LOW)
        }
    }

}

private class FlipFlopModule(label: String, outputModuleIds: Set<String>) :
    Module(label, outputModuleIds) {
    private var internalState: Signal = Signal.LOW

    override fun createOutputSignals(inputId: String, inputSignal: Signal): Map<String, Signal> {
        val result: Map<String, Signal>
        if (inputSignal == Signal.LOW) {
            internalState = if (internalState == Signal.LOW) Signal.HIGH else Signal.LOW
            result = buildMap {
                for (outLabel in outputModuleLabels) {
                    put(outLabel, internalState)
                }
            }
        } else {
            result = mapOf() // if high, then nothing happens
        }
        return result
    }
}

private class ConjunctionModule(label: String, outputModuleIds: Set<String>) :
    Module(label, outputModuleIds) {

    val storedInputSignals: MutableMap<String, Signal> = HashMap()

    fun registerInputModule(id: String) {
        storedInputSignals[id] = Signal.LOW
    }

    override fun createOutputSignals(inputId: String, inputSignal: Signal): Map<String, Signal> {
        storedInputSignals[inputId] = inputSignal
        val result = if (storedInputSignals.values.all { it == Signal.HIGH }) Signal.LOW else Signal.HIGH
        return buildMap {
            for (outLabel in outputModuleLabels) {
                put(outLabel, result)
            }
        }
    }

}

private class DebugModule(label: String, val otherModules: Map<String, Module>) : Module(label, setOf()) {
    override fun createOutputSignals(
        inputLabel: String,
        inputSignal: Signal
    ): Map<String, Signal> {
//        if (label == "rx") {
//            println("Received $inputSignal from $inputLabel")
//            val sourceModule = otherModules[inputLabel]
//            if (sourceModule is ConjunctionModule) {
//                println(sourceModule.storedInputSignals)
//            }
//        }
        return mapOf()
    }

    companion object {

        /**
         *    1 = lz.qf, vc.pc, gr.sg, db.gt
         *    2 = lz.vr, vc.jq
         *    4 = lz.xz, vc.fk
         *    8 = lz.ds,        gr.ld, db.xv
         *   16 =        vc.kz, gr.hx, db.qm
         *   32 = lz.xl,               db.zf
         *   64 = lz.gj,        gr.qq
         *  128 = lz.br,               db.vn
         *  256 = lz.qh, vc.rv, gr.bc
         *  512 = lz.ms, vc.rj, gr.cd, db.kc
         * 1024 = lz.bd, vc.jp, gr.gg, db.cq
         * 2048 = lz.vm, vc.cf, gr.rq, db.hf
         *
         *
         * Weird numbers: 4079, 3863, 3929, 3769
         *
         * Just randomly multiply them together and hope it is the correct answer (which it actually was ... but why?)
         *
         * Explanation from Reddit:
         *
         * About one of your "stunned" tidbit: Note that there is one output connection from the counter conjunction
         * module to the lowest bit (1 bit) of the 12-bit counter. For all other 11 bits, there is either an output to
         * the conjunction module (meaning 1 is expected in those bits when the conjunction module fires), or the
         * conjunction module has an output to that bit (meaning 0 is expected in those bits), but not both; the lowest
         * bit have both. Using your 4079 as example, this would be that the among the 12 bits in the counter, all
         * except the 16 bit have output to the conjunction module, and the conjunction module has output to the 16 bit
         * and the 1 bit. When the counter hits 4079, the conjunction module fires, its two outputs to 16 bit and 1 bit
         * effectively adds 17 to the counter, and the counter overflows to 0 because 4079+17=4096=212 . This is why
         * you see they are all 0 bit after signal settled: the signal for reaching the value is fired and in addition
         * to supplying the "master" conjunction module a signal, it also resets its counter.
         */

        @JvmStatic
        fun main(args: Array<String>) {
            println(4079L * 3863L * 3929L * 3769L)
        }
    }
}