package com.seannajera.dkouple

import android.view.View

/**
 * Because RecyclerView.Adapter needs to create ViewHolders at run time using getItemViewType,
 * ComponentFactory acts as the intermediary between Adapter and ViewHolder creation.
 *
 * The ComponentFactory.createView is meant to be called in RecyclerView.Adapter.onCreateViewHolder
 * where the Adapter will supply the layoutId (for looking up the ComponentView) and the inflated
 * view, used by the ComponentView for initializing itself.
 */
interface ComponentFactory {

    /**
     * @param layoutId: expected to be derived from the DKoupleComponent annotation,
     * and mapped to a ComponentView
     * @param view: The View that is inflated from and passed into by
     * ComponentAdapter.onCreateViewHolder, used to instantiate a ComponentView
     *
     * @return a ComponentView mapped to by the layoutId
     */
    fun createView(layoutId: Int, view: View): ComponentView<out Component>
}
