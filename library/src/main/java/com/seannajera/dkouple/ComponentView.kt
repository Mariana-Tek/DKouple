package com.seannajera.dkouple

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

// This class should a sealed class. However, we can't use sealed classes because inherited class
// must exist in the same file, disallowing for extraction into library can make sealed class
// after this ticket is done https://youtrack.jetbrains.com/issue/KT-13495
abstract class ComponentView<Component : com.seannajera.dkouple.Component>(view: View) :
    RecyclerView.ViewHolder(view) {
    abstract fun onBind(previous: Component?, current: Component)
}

abstract class ComponentLayout {
    @LayoutRes abstract fun layoutId(): Int
}

open class StaticView<StaticComponent : Component>(view: View) : ComponentView<StaticComponent>(view) {
    override fun onBind(previous: StaticComponent?, current: StaticComponent) {}
}