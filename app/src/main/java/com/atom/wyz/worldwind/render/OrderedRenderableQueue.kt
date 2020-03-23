package com.atom.wyz.worldwind.render

import java.util.*

class OrderedRenderableQueue {

    companion object {
        class Entry(var or: OrderedRenderable, var eyeDistance: Double, var ordinal: Int)
    }

    var renderables : ArrayList<Entry>

    var mustSortRenderables = false

    private var last = -1

    constructor(){
        renderables = ArrayList()
    }

    constructor( initialCapacity :Int){
        renderables = ArrayList(initialCapacity)
    }

    var frontToBackComparator: Comparator<Entry> = Comparator<Entry> { lhs, rhs ->
        if (lhs.eyeDistance < rhs.eyeDistance) {
            -1
        } else if (lhs.eyeDistance > rhs.eyeDistance) {
            1
        } else {
            (rhs.ordinal - lhs.ordinal).toInt()
        }
    }

    fun offerRenderable(renderable: OrderedRenderable, depth: Double) {
        renderables.add(Entry(renderable, depth, ++this.last))
        mustSortRenderables = true
    }
    fun peekRenderable(): OrderedRenderable? {
        this.sortIfNeeded()
        return if (this.last <0) null else renderables[last].or
    }

    fun pollRenderable(): OrderedRenderable? {
        this.sortIfNeeded()
        if (last < 0) {
            return null
        }
        val or = renderables[last].or
        renderables.removeAt(last--)
        return or
    }



    fun clearRenderables() {
        renderables.clear()
        mustSortRenderables = false
        last = -1
    }


    private fun sortIfNeeded() {
        if (this.mustSortRenderables) {
            Collections.sort(renderables, frontToBackComparator)
            mustSortRenderables = false
        }
    }
}