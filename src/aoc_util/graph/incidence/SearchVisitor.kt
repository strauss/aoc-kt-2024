package aoc_util.graph.incidence

abstract class SearchVisitor<V, E> {

    open fun visitRoot(root: Graph<V, E>.Vertex) {
        // Default empty implementation
    }

    open fun leaveRoot(root: Graph<V, E>.Vertex) {
        // Default empty implementation
    }

    open fun visitVertex(vertex: Graph<V, E>.Vertex) {
        // Default empty implementation
    }

    open fun leaveVertex(vertex: Graph<V, E>.Vertex) {
        // Default empty implementation
    }

    open fun visitEdge(edge: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun visitTreeEdge(treeEdge: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun leaveTreeEdge(treeEdge: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun visitFrond(frond: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun leaveEdge(edge: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun reset() {
        // Default empty implementation
    }

}

abstract class DfsVisitor<V, E> : SearchVisitor<V, E>() {

    open fun visitForwardArc(forwardArc: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun visitBackwardArc(backwardArc: Graph<V, E>.Edge) {
        // Default empty implementation
    }

    open fun visitCrossLink(crossLink: Graph<V, E>.Edge) {
        // Default empty implementation
    }

}

class AnalyseEdgesVisitor<V, E> : DfsVisitor<V, E>() {
    var treeEdges: Int = 0
        private set

    var fronds: Int = 0
        private set

    var forwardArcs: Int = 0
        private set

    var backwardArcs: Int = 0
        private set

    var crossLinks: Int = 0
        private set

    override fun visitTreeEdge(treeEdge: Graph<V, E>.Edge) {
        treeEdges += 1
    }

    override fun visitFrond(frond: Graph<V, E>.Edge) {
        fronds += 1
    }

    override fun visitForwardArc(forwardArc: Graph<V, E>.Edge) {
        forwardArcs += 1
    }

    override fun visitBackwardArc(backwardArc: Graph<V, E>.Edge) {
        backwardArcs += 1
    }

    override fun visitCrossLink(crossLink: Graph<V, E>.Edge) {
        crossLinks += 1
    }

    override fun reset() {
        treeEdges = 0
        fronds = 0
        forwardArcs = 0
        backwardArcs = 0
        crossLinks = 0
    }

    override fun toString(): String {
        return "AnalyseEdgesVisitor(treeEdges=$treeEdges, fronds=$fronds, forwardArcs=$forwardArcs, backwardArcs=$backwardArcs, crossLinks=$crossLinks)"
    }


}