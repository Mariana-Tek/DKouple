package com.seannajera.dkouple

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val tag = "Dkouple_MainActivity"

    private val componentFactory: ComponentFactory = MainComponentFactory()

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

        Handler(Looper.getMainLooper()).postDelayed({

            val firstItemUpdated = componentAdapter.updateComponent(
                ItemComponent("1", "Still First Item")
            )

            val nonExistentItemUpdated = componentAdapter.updateComponent(
                ItemComponent("2", "I am not in the components")
            )

            Log.i(tag, "First item was updated: $firstItemUpdated") // "First item was updated: true"
            Log.i(tag, "Non existent item was updated: $nonExistentItemUpdated") // "Non existent item was updated: false"

        }, 5000)
    }
}
