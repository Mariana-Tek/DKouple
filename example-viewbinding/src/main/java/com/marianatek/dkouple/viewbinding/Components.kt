package com.marianatek.dkouple.viewbinding

import android.annotation.SuppressLint
import android.view.View
import com.marianatek.dkouple.viewbinding.databinding.ComponentItemTextBinding
import com.seannajera.dkouple.ComponentViewBinding
import com.seannajera.dkouple.Component
import com.seannajera.dkouple.DKoupleComponent
import com.seannajera.dkouple.DKoupleView
import com.seannajera.dkouple.FactoryView

@SuppressLint("NonConstantResourceId")
@DKoupleComponent(R.layout.component_item_text, ItemTextView::class)
data class ItemTextComponent(
    override val id: String,
    val text: String
) : Component

@DKoupleView
class ItemTextView(
    @FactoryView view: View
) : ComponentViewBinding<ItemTextComponent, ComponentItemTextBinding>(view) {
    override val binding: ComponentItemTextBinding by lazy {
        ComponentItemTextBinding.bind(view)
    }

    override fun onViewUpdate(previous: ItemTextComponent?, current: ItemTextComponent) {
        if (previous != current) {
            binding.textView.text = current.text
        }
    }
}

