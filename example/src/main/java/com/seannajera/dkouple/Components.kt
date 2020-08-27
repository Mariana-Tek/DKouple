package com.seannajera.dkouple

import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

// This is an example of an implemented Component class. Since this Component is a data class, as
// most all Components should be, it does not need to override contentSameAs
@DKoupleComponent(R.layout.component_item, ItemView::class)
data class ItemComponent(override val id: String, val name: String) : Component

// This is an example of an implemented ComponentView class. Since we are using the annotation
// processor library module to generate the ComponentFactory, we must also supply the annotations
// on the class and the view constructor parameter
@DKoupleView
class ItemView(@FactoryView view: View) : ComponentView<ItemComponent>(view) {

    // We use the view constructor parameter here to inflate any subviews
    private val nameView: TextView by lazy { view.findViewById<TextView>(R.id.item_component_name) }

    // We implement the onViewUpdate in order to bind the Component state to the ComponentView
    // Note that we can also use the previous Component state to check if we need to re-bind
    // a specific Component field. This is useful when we have many Component fields and don't
    // want to re-bind all of them.
    override fun onViewUpdate(previous: ItemComponent?, current: ItemComponent) {
        if (previous?.name != current.name) {
            nameView.text = current.name
        }

        if (previous?.id != current.id) {
            ObjectAnimator.ofFloat(nameView, View.ROTATION, 0f, 360f)
                .setDuration(300)
                .start()
        }
    }
}

@DKoupleComponent(R.layout.component_icon, IconView::class)
data class IconComponent(override val id: String, val name: String, val iconUrl: String) : Component

// The annotation processor will generate any constructor dependencies, also.
@DKoupleView
class IconView(@FactoryView view: View, private val picasso: Picasso) : ComponentView<IconComponent>(view) {

    private val nameView: TextView by lazy { view.findViewById<TextView>(R.id.component_icon_name) }
    private val iconView: ImageView by lazy { view.findViewById<ImageView>(R.id.component_icon_view) }

    override fun onViewUpdate(previous: IconComponent?, current: IconComponent) {
        if (previous?.name != current.name) {
            nameView.text = current.name
        }

        picasso.load(current.iconUrl).into(iconView)
    }
}
