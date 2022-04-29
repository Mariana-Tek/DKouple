package com.seannajera.dkouple

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private val tag = "Dkouple_MainActivity"
    private val picasso = Picasso.get()

    // When importing kapt "com.github.Mariana-Tek.DKouple:processor:5.+" as a dependency
    // DKouple will generate the ComponentFactory for you, named DKoupleComponentFactory.
    // You can also create your own ComponentFactory if you choose.
    private val componentFactory: ComponentFactory = DKoupleComponentFactory(picasso)
    private val componentAdapter = ComponentAdapter(componentFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_main)

        // Set the ComponentAdapter on any inflated RecyclerView which uses a LinearLayoutManager
        recyclerView.adapter = componentAdapter

        // Now you can update the ComponentAdapter with any list of Components and the RecyclerView
        // will render their corresponding ComponentViews

        // Note that every Component.id is different in this ComponentAdapter even though all
        // the example components are the same
        componentAdapter.applyComponents(
            listOf(
                ItemComponent("0", "First Item"),
                ItemComponent("1", "Second Item")
            )
        )

        Handler(Looper.getMainLooper()).postDelayed({

            // Here is an example of updating a component already in the RecyclerView
            val firstItemUpdated = componentAdapter.updateComponent(
                ItemComponent("0", "Still First Item")
            )

            // Here is an example of updating a component that is NOT in the RecyclerView
            val nonExistentItemUpdated = componentAdapter.updateComponent(
                ItemComponent("2", "I am not in the components")
            )

            // We can also check if the updated items were in the list already
            Log.i(tag, "First item was updated: $firstItemUpdated") // "First item was updated: true"
            Log.i(tag, "Non existent item was updated: $nonExistentItemUpdated") // "Non existent item was updated: false"

        }, 5000)

        Handler(Looper.getMainLooper()).postDelayed({

            // We can also append a new list of items to the current list of Components
            componentAdapter.appendComponents(
                listOf(
                    ItemComponent("2", "But I am in the components now"),
                    ItemComponent("3", "I'm in the components too!"),
                    IconComponent("4", "Bunny", "https://cdn.iconscout.com/icon/premium/png-256-thumb/easter-bunny-6-679670.png")
                )
            )

        }, 10000)
    }
}