package com.seannajera.dkouple

import android.view.View
import android.widget.TextView

@DKoupleComponent(R.layout.component_item)
data class ItemComponent(override val id: String, val name: String) : Component {

    override fun contentSameAs(otherComponent: Any): Boolean {
        return this == (otherComponent as? ItemComponent)
    }
}

class ItemView(view: View) : ComponentView<ItemComponent>(view) {

    private val nameView: TextView by lazy { view.findViewById<TextView>(R.id.item_component_name) }

    override fun onViewUpdate(previous: ItemComponent?, current: ItemComponent) {
        if (previous?.name != current.name) {
            nameView.text = current.name
        }
    }
}
