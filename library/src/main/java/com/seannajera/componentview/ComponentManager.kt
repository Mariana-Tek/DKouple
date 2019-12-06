package com.seannajera.componentview

import android.view.View

interface ComponentManager {
    fun createView(layout: ComponentLayout, view: View): ComponentView<*>
    fun bindView(previousModel: ComponentModel?, currentModel: ComponentModel, componentView: ComponentView<*>)
}