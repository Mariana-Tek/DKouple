@file:Suppress("unused")

package com.seannajera.dkouple

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ComponentAdapter(private val componentFactory: ComponentFactory) :
    ListAdapter<Component, ComponentView<out Component>>(componentDiffer) {

    private val componentLayouts: ArrayList<Int> = arrayListOf()

    private var actionWhenComponentsUpdate: ((List<Component>, List<Component>) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, layoutId: Int): ComponentView<*> {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        return componentFactory.createView(layoutId, view)
    }

    override fun onBindViewHolder(componentView: ComponentView<out Component>, position: Int) {
        componentView.onBind(null, getItem(position))
    }

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
            componentView.onBind(componentState.first, componentState.second)
        }
    }

    override fun getItemViewType(position: Int): Int = componentLayouts[position]

    override fun onCurrentListChanged(
        previousComponent: MutableList<Component>,
        currentComponent: MutableList<Component>
    ) {
        componentLayouts.clear()
        currentList.forEach {
            componentLayouts.add(it::class.annotations.filterIsInstance<DKoupleComponent>().first().layoutId)
        }

        actionWhenComponentsUpdate?.invoke(previousComponent, currentComponent)
    }

    fun onComponentsUpdated(
        actionWhenComponentsUpdate: ((List<Component>, List<Component>) -> Unit)?
    ) {
        this.actionWhenComponentsUpdate = actionWhenComponentsUpdate
    }

    fun applyComponents(components: List<Component>) = submitList(components)

    companion object {
        val componentDiffer = object : DiffUtil.ItemCallback<Component>() {
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
