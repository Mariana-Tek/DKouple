package com.seannajera.dkouple

import android.view.View

interface ComponentManager {
    fun createView(layout: ComponentLayout, view: View): ComponentView<*>
    fun bindView(previous: Component?, current: Component, componentView: ComponentView<*>)
}