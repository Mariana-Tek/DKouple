package com.seannajera.dkouple

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ComponentAdapter(private val componentManager: ComponentManager) :
    ListAdapter<Component, ComponentView<*>>(componentDiffer) {

    private val componentLookup: SparseArray<ComponentLayout> = SparseArray()

    override fun onCreateViewHolder(parent: ViewGroup, layoutId: Int): ComponentView<*> {
        val view = LayoutInflater.from(parent.context)
            .inflate(layoutId, parent, false)

        return componentManager.createView(componentLookup.get(layoutId), view)
    }

    override fun onBindViewHolder(componentView: ComponentView<*>, position: Int) {
        componentManager.bindView(null, getItem(position), componentView)
    }

    override fun onBindViewHolder(
        componentView: ComponentView<*>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(componentView, position)
        } else {
            @Suppress("UNCHECKED_CAST")
            val componentState = payloads[0] as Pair<Component, Component>
            componentManager.bindView(componentState.first, componentState.second, componentView)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).layout.layoutId()

    override fun onCurrentListChanged(
        previousComponent: MutableList<Component>,
        currentComponent: MutableList<Component>
    ) {
        componentLookup.clear()
        currentList.forEach {
            componentLookup.put(it.layout.layoutId(), it.layout)
        }
    }

    fun applyComponents(components: ArrayList<out Component>) = submitList(components)

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
