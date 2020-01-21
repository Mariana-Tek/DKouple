package com.seannajera.dkouple

import android.view.View

interface ComponentFactory {
    fun createView(layoutId: Int, view: View): ComponentView<out Component>
}
