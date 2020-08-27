package com.seannajera.dkouple

/**
 * Used in order to represent the state of ComponentViews in a RecyclerView.
 */
interface Component {
    /**
     * This id is used by the DiffUtil.ItemCallback<Component> in ComponentAdapter.componentDiffer
     * to identify a component for efficient re-use or initialization in a RecyclerView. It must be
     * unique to all other Components in any one ComponentAdapter list of Components
     */
    val id: String

    /**
     * This method is used to by the DiffUtil.ItemCallback<Component> in
     * ComponentAdapter.componentDiffer to determine whether a re-usable ComponentView has had its
     * Component state updated. If false, the ComponentAdapter will call onBindViewHolder to update
     * the Component state in it's corresponding ComponentView
     */
    fun contentSameAs(otherComponent: Any): Boolean {
        return otherComponent == this
    }
}