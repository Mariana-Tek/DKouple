package com.marianatek.dkouple.viewbinding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.marianatek.dkouple.viewbinding.databinding.ActivityMainBinding
import com.seannajera.dkouple.ComponentAdapter
import com.seannajera.dkouple.ComponentFactory
import com.seannajera.dkouple.DKoupleComponentFactory

class MainActivity : AppCompatActivity() {

    private val componentFactory: ComponentFactory = DKoupleComponentFactory()
    private val adapter = ComponentAdapter(componentFactory)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerview.adapter = adapter

        val componentsList = mutableListOf<ItemTextComponent>()
        for (i in 0..1000) {
            componentsList.add(ItemTextComponent("$i", "Text Component #$i"))
        }

        adapter.applyComponents(componentsList)
    }
}