package com.seannajera.dkouple

import android.view.View

class MainComponentFactory : ComponentFactory {
    override fun createView(layoutId: Int, view: View): ComponentView<out Component> {
        return when (layoutId) {
            R.layout.component_item -> ItemView(view)
            else -> throw IllegalArgumentException("Could not find layout resource with id: $layoutId")
        }
    }
}
