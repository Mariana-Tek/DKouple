package com.seannajera.componentview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ComponentAdapter(private val componentManager: ComponentManager) :
    ListAdapter<ComponentModel, ComponentView<*>>(componentDiffer) {

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
            val modelState = payloads[0] as Pair<ComponentModel, ComponentModel>
            componentManager.bindView(modelState.first, modelState.second, componentView)
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).layout.id

    override fun onCurrentListChanged(
        previousComponent: MutableList<ComponentModel>,
        currentComponent: MutableList<ComponentModel>
    ) {
        componentLookup.clear()
        currentList.forEach {
            componentLookup.put(it.layout.id, it.layout)
        }
    }

    fun setListModels(newModels: ArrayList<out ComponentModel>) = submitList(newModels)

    companion object {
        val componentDiffer = object : DiffUtil.ItemCallback<ComponentModel>() {
            override fun areItemsTheSame(
                oldModel: ComponentModel,
                newModel: ComponentModel
            ): Boolean {
                return oldModel.id == newModel.id
            }

            override fun areContentsTheSame(
                oldModel: ComponentModel,
                newModel: ComponentModel
            ): Boolean {
                return oldModel.contentSameAs(newModel)
            }
        }
    }
}
