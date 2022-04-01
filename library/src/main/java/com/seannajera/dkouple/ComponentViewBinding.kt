package com.seannajera.dkouple

import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * ComponentViewBinding expands upon ComponentView. It adds a ViewBinding generic parameter so
 * that the user can associate with the respective ViewBinding file generated by the XML layout.
 * This forces any class implementing the ComponentViewBinding to add the respective ViewBinding file.
 */
abstract class ComponentViewBinding<Component: com.seannajera.dkouple.Component, ViewBinding>(
    view: View
) : ComponentView<Component>(view) {

    /**
     * Instance of ViewBinding file generated by the XML Layout. It's initialization can be done
     * lazily using:
     *      val binding: ViewBinding by lazy {
     *          ViewBinding.bind(view)
     *      }
     */
    abstract val binding: ViewBinding
}