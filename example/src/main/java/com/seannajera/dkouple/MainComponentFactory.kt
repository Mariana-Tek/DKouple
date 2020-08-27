package com.seannajera.dkouple

import android.view.View

// This is an example of an implementation of a ComponentFactory
// Since we are using the annotation processor library module to generate this for us
// we don't need to use it.
class MainComponentFactory : ComponentFactory {
    override fun createView(layoutId: Int, view: View): ComponentView<out Component> {
        return when (layoutId) {
            R.layout.component_item -> ItemView(view)
            else -> throw IllegalArgumentException("Could not find layout resource with id: $layoutId")
        }
    }
}
