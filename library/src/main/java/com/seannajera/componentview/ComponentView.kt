package com.seannajera.componentview

import android.view.View
import androidx.recyclerview.widget.RecyclerView

// This class should a sealed class. However, we can't use sealed classes because inherited class
// must exist in the same file, disallowing for extraction into library can make sealed class
// after this ticket is done https://youtrack.jetbrains.com/issue/KT-13495
abstract class ComponentView<Model : ComponentModel>(view: View) :
    RecyclerView.ViewHolder(view) {
    abstract fun onBind(previousModel: Model?, currentModel: Model)
}

abstract class ComponentLayout {
    abstract val id: Int
}

class StaticView<Model: ComponentModel>(view: View): ComponentView<Model>(view) {
    override fun onBind(previousModel: Model?, currentModel: Model) {}
}