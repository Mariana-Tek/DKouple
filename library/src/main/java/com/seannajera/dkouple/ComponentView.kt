@file:Suppress("unused")

package com.seannajera.dkouple

import android.view.View
import androidx.recyclerview.widget.RecyclerView

// This class should a sealed class. However, we can't use sealed classes because inherited class
// must exist in the same file, disallowing for extraction into library can make sealed class
// after this ticket is done https://youtrack.jetbrains.com/issue/KT-13495
abstract class ComponentView<Component : com.seannajera.dkouple.Component>(view: View) :
    RecyclerView.ViewHolder(view) {

    private var cachedComponent: Component? = null

    @Suppress("UNCHECKED_CAST")
    fun onBind(
        previous: com.seannajera.dkouple.Component?,
        current: com.seannajera.dkouple.Component
    ) {
        previous as Component?
        current as Component
        onViewUpdate(previous ?: cachedComponent, current)
        cachedComponent = current
    }

    abstract fun onViewUpdate(previous: Component?, current: Component)
}

open class StaticView(view: View) : ComponentView<Component>(view) {
    override fun onViewUpdate(previous: Component?, current: Component) {}
}