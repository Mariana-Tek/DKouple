package com.seannajera.dkouple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val componentFactory: ComponentFactory = ComponentFactory_DKouple_Impl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_main)

        val componentAdapter = ComponentAdapter(componentFactory)
        recyclerView.adapter = componentAdapter

        componentAdapter.applyComponents(
            listOf(
                ItemComponent("1", "First Item")
            )
        )
    }
}
