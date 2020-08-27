@file:Suppress("unused")

package com.seannajera.dkouple

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * ComponentView is a type of ViewHolder that uses an associated, strongly typed, Component generic
 * parameter as its state representation and can only be associated with that one type of
 * Component.
 *
 * The ComponentView is created by the ComponentAdapter.onBindViewHolder method, where the
 * ComponentFactory uses the ComponentView's associated Component's annotated layoutId (via
 * the DkoupleComponent annotation) to lookup the correct ComponentView
 *
 * ComponentView receives its updated Component state from the ComponentAdapter.onBindViewHolder
 * method.
 *
 * When using DKouple's annotation processor to generate the ComponentFactory, please ensure that
 * ComponentView contains the DKoupleView class annotation in your source code as well as the
 * FactoryView annotation on the view in the constructor.
 */
abstract class ComponentView<Component : com.seannajera.dkouple.Component>(view: View) :
    RecyclerView.ViewHolder(view), BaseView {

    private var cachedComponent: Component? = null

    /**
     * Caches the current Component state of the ComponentView before calling the public
     * onViewUpdate
     */
    @Suppress("UNCHECKED_CAST")
    internal fun onBind(
        previous: com.seannajera.dkouple.Component?,
        current: com.seannajera.dkouple.Component
    ) {
        previous as Component?
        current as Component
        onViewUpdate(previous ?: cachedComponent, current)
        cachedComponent = current
    }

    /**
     * This method is implemented in order to bind the Component state to the ComponentView
     */
    abstract fun onViewUpdate(previous: Component?, current: Component)
}

/**
 * This static ComponentView can be instantiated/implemented when the view has not Component state
 * for view binding.
 */
open class StaticView(view: View) : ComponentView<Component>(view) {
    /**
     * The onViewUpdate is a no-op for this stateless ComponentView
     */
    override fun onViewUpdate(previous: Component?, current: Component) {}
}