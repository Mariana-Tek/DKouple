@file:Suppress("unused")

package com.seannajera.dkouple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import java.lang.IllegalStateException

/**
 * Used as the base ListAdapter to render all ComponentViews from the supplied list of Components.
 * This ComponentAdapter can be used with any RecyclerView that uses a LinearLayoutManager
 */
class ComponentAdapter(private val componentFactory: ComponentFactory) :
    ListAdapter<Component, ComponentView<out Component>>(componentDiffer) {

    private val componentLayoutIds: ArrayList<Int> = arrayListOf()

    private var actionWhenComponentsUpdate: ((List<Component>, List<Component>) -> Unit)? = null

    /**
     * @param layoutId: This layout resource id is supplied by the ComponentAdapter.getItemViewType
     * which returns the layout resource id stored in the ComponentAdapter.componentLayoutIds. The
     * layoutId is used to create the ComponentView from the ComponentFactory using the layoutId
     * as a lookup value.
     */
    override fun onCreateViewHolder(parent: ViewGroup, layoutId: Int): ComponentView<*> {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        return componentFactory.createView(layoutId, view)
    }

    /**
     * This version of the ListAdapter.onBindViewHolder is used to bind Component
     * data to newly created ComponentViews which do not have a previous Component state.
     */
    override fun onBindViewHolder(componentView: ComponentView<out Component>, position: Int) {
        componentView.onBind(previous = null, current = getItem(position))
    }

    /**
     * This version of the ListAdapter.onBindViewHolder is used to bind the previous
     * and current Component state to an existing ComponentView. This is achieved by using the
     * DiffUtil.ItemCallback.getChangePayload method in ComponentAdapter.componentDiffer.
     */
    override fun onBindViewHolder(
        componentView: ComponentView<out Component>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(componentView, position)
        } else {
            @Suppress("UNCHECKED_CAST")
            val componentState = payloads[0] as Pair<Component, Component>
            componentView.onBind(previous = componentState.first, current = componentState.second)
        }
    }

    /**
     * Based on the component position in the ListAdapter.currentList, this will return the
     * Component's annotated DKoupleComponent.layoutId in order to map to and instantiate its
     * associated ComponentView.
     */
    override fun getItemViewType(position: Int): Int = componentLayoutIds[position]

    /**
     * ComponentAdapter overrides the base ListAdapter.onCurrentListChanged in order to update the
     * ComponentAdapter.componentLayoutIds list, used to determine Item View Type and subsequently
     * which ComponentView to instantiate and render.
     *
     * This method is also used to trigger the lambda supplied in ComponentAdapter.onComponentsUpdated.
     */
    override fun onCurrentListChanged(
        previousComponent: MutableList<Component>,
        currentComponent: MutableList<Component>
    ) {
        componentLayoutIds.clear()
        currentList.forEach {
            val layoutId = it::class.annotations
                .filterIsInstance<DKoupleComponent>()
                .firstOrNull()
                ?.layoutId ?: throw IllegalStateException("Component: $it is not annotated with @DKoupleComponent")
            componentLayoutIds.add(layoutId)
        }

        actionWhenComponentsUpdate?.invoke(previousComponent, currentComponent)
    }

    /**
     * A lambda can be supplied to the ComponentAdapter to allow the client to receive a callback
     * after the RecyclerView has been updated with any new components.
     */
    fun onComponentsUpdated(actionOnUpdate: ((List<Component>, List<Component>) -> Unit)?) {
        this.actionWhenComponentsUpdate = actionOnUpdate
    }

    /**
     * Supplies a new list of Components to be rendered in a RecyclerView. This will also invalidate
     * any current list of components in the RecyclerView. The invalidation of views and the rendering
     * of any new, updated views will be animated. See ComponentAdapter.componentDiffer.
     */
    fun applyComponents(components: List<Component>) = submitList(components)

    /**
     * Updates a single component that may or may not be in the Recyclerview with a new Component
     * of the same id. If no component is found in the current component list with that matching
     * id, then no action is taken on the RecyclerView.
     *
     * @return a value of true if the supplied component was found in the current component list.
     * false if the component was not found in the list.
     */
    fun updateComponent(component: Component): Boolean {
        var isComponentInList = false

        val updatedComponentList = currentList.map {
            if (it.id == component.id) {
                isComponentInList = true
                return@map component
            } else {
                return@map it
            }
        }

        if (isComponentInList) submitList(updatedComponentList)

        return isComponentInList
    }

    /**
     * Appends a list of new components to any current list of components in the Recyclerview.
     */
    fun appendComponents(components: List<Component>) {
        val updatedComponents: List<Component> = ArrayList(currentList).also { it.addAll(components) }

        submitList(updatedComponents)
    }

    companion object {
        /**
         * The RecyclerView library comes with a DiffUtil helper class that will analyze
         * a list of new components and determine which components need to have their views
         * instantiated or simply re-bound (with new component data).
         *
         * It also handles view animations of additions, deletions, and index changes of all
         * ComponentViews withing the RecyclerView.
         */
        private val componentDiffer = object : DiffUtil.ItemCallback<Component>() {
            override fun areItemsTheSame(
                old: Component,
                new: Component
            ): Boolean {
                return old.id == new.id
            }

            override fun areContentsTheSame(
                old: Component,
                new: Component
            ): Boolean {
                return old.contentSameAs(new)
            }

            override fun getChangePayload(oldItem: Component, newItem: Component): Any? {
                return Pair(oldItem, newItem)
            }
        }
    }
}
