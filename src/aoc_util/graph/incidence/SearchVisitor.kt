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