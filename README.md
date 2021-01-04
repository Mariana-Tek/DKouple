<h1 align="center">
    DKouple
</h1>

DKouple is an efficient Component based view system for android RecyclerView's 
* Easily add a component-ized RecyclerView item
* Automatically add your component-ized views with animation
* Reduces boilerplate by eliminating the need for a RecyclerView.Adapter

## Guide

### Components
A `Component` can be considered the _state_ of a view. The `Component` holds the data in a stateless
fashion to be consumed by a `ComponentView`

A `ComponentView` is a [ViewHolder](https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder?authuser=1)
based view that has a 1-1 relationship with a defined `Component`. The `ComponentView` 
renders it's sub-views in a stateless fashion using the supplied `Component` state.

```kt
// This is an example of an implemented Component class.

// Components are annotated with a special annotation that  contains the associated ComponentView's
// layout resource id. 
@DKoupleComponent(R.layout.component_item, ItemView::class)
data class ItemComponent(override val id: String, val name: String) : Component

// This is an example of an implemented ComponentView class.
class ItemView(view: View) : ComponentView<ItemComponent>(view) {

    // We use the view constructor parameter here to inflate any subviews
    private val nameView: TextView by lazy { view.findViewById<TextView>(R.id.item_component_name) }

    // We implement the onViewUpdate in order to bind the Component state to the ComponentView

    // Note that we can also get the previous Component state to check if we need to re-bind
    // a specific Component field.
 
    // This is useful when we have many Component fields and don't
    // want to re-bind all of them.

    // We can also use the previous `Component` state to execute animations
    override fun onViewUpdate(previous: ItemComponent?, current: ItemComponent) {
        if (previous?.name != current.name) {
            nameView.text = current.name
        }

        if (previous?.id != current.id) {
            // execute a fun little 360 rotation animation whenever a new component is bound to this
            // ComponentView
            ObjectAnimator.ofFloat(nameView, View.ROTATION, 0f, 360f)
                .setDuration(300)
                .start()
        }
    }
}
```

### ComponentFactory
A `ComponentFactory` must be created in order to map the `Component` to its `ComponentView`.
Simply implement a `ComponentFactory` and supply the mapping of all your `Component`'s annotated
`layoutId`'s to their corresponding `ComponentView`s

```kt
// This class implements a ComponentFactory, which simply maps our ItemComponent's annotated
// layoutId to its associated ItemView.
class MyComponentFactory : ComponentFactory {
    override fun createView(layoutId: Int, view: View): ComponentView<out Component> {
        return when (layoutId) {
            R.layout.component_item -> ItemView(view)
            else -> throw IllegalArgumentException("Could not find layout resource with id: $layoutId")
        }
    }
}
```

### Reduce Boiler Plate by auto generating the ComponentFactory
Maintaining the `ComponentFactory` every time you add or remove a `Component` from your app can
get tedious and error prone. You can eliminate the need to write the `ComponentFactory` yourself by
using the DKouple library's annotation processor to generate a `DKoupleComponentFactory`.

Taking our previous example, simply add the `@DKoupleView` annotation to the `ItemView` class
definition and the `@FactoryView` annotation to the `view: View`. And by adding these annotations
to all subsequent `Component`s the library can generate a `ComponentFactory` for you.
```kt

@DKoupleComponent(R.layout.component_item, ItemView::class)
data class ItemComponent(override val id: String, val name: String) : Component

// Add these annotations to ItemView to auto generate a ComponentFactory
@DKoupleView
class ItemView(@FactoryView view: View) : ComponentView<ItemComponent>(view) {

    private val nameView: TextView by lazy { view.findViewById<TextView>(R.id.item_component_name) }

    override fun onViewUpdate(previous: ItemComponent?, current: ItemComponent) {
        if (previous?.name != current.name) {
            nameView.text = current.name
        }

        if (previous?.id != current.id) {
            ObjectAnimator.ofFloat(nameView, View.ROTATION, 0f, 360f)
                .setDuration(300)
                .start()
        }
    }
}
```

Let's say you have a constructor dependency on a `ComponentView`. The Library will also auto generate
a `ComponentAdapter` with the required constructor dependencies
```kt

@DKoupleComponent(R.layout.component_icon, IconView::class)
data class IconComponent(override val id: String, val name: String, val iconUrl: String) : Component

// The annotation processor will generate any constructor dependencies, also.
@DKoupleView
class IconView(@FactoryView view: View, private val picasso: Picasso) : ComponentView<IconComponent>(view) {

    private val nameView: TextView by lazy { view.findViewById<TextView>(R.id.component_icon_name) }
    private val iconView: ImageView by lazy { view.findViewById<ImageView>(R.id.component_icon_view) }

    override fun onViewUpdate(previous: IconComponent?, current: IconComponent) {
        if (previous?.name != current.name) {
            nameView.text = current.name
        }

        picasso.load(current.iconUrl).fit().centerCrop().into(iconView)
        
    }
}

// The library auto generates any constructor dependencies for you
class DKoupleComponentFactory(picasso: Picasso) : ComponentFactory {
    override fun createView(layoutId: Int, view: View): ComponentView<out Component> {
        return when (layoutId) {
            R.layout.component_icon -> IconView(view, picasso)
            else -> throw IllegalArgumentException("Could not find layout resource with id: $layoutId")
        }
    }
}
```

### ComponentAdapter
Once we have created all our `Comopnent`s and the `ComponentFactory`, we can instantiate a `ComponentAdapter`
and set it to any `RecyclerView` in our app.

Once our `ComponentAdapter` is set to a RecyclerView, we can add, append, remove, and update
our components to the RecyclerView, where they will be rendered to the screen.

*Note*: The RecyclerView must use a LinearLayoutManager to layout its views

```kt
class MyActivity : AppCompatActivity() {

    private val tag = "MyActivity"

    // We can also use the DKoupleComponentFactory() if we use the annotation processor module
    private val componentFactory: ComponentFactory = MyComponentFactory() 
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
                    ItemComponent("3", "I'm in the components too!")
                )
            )

        }, 10000)
    }
}
```

## Dependencies
You can add DKouple to your android app/build.gradle dependency block

```
dependencies {
    // DKouple Component Library
    implementation 'com.github.Mariana-Tek.DKouple:library:5.0.0'
    implementation 'com.github.Mariana-Tek.DKouple:core:5.0.0'

    // If you wish to use the annotation processing engine
    // Add this kapt dependency to generate the ComponentFactory
    kapt 'com.github.Mariana-Tek.DKouple:processor:5.0.0'
}
    
```

and add the JitPack maven url to your project build.gradle
```
buildscript {
    repositories {
        ...
    }
    dependencies {
        ...
    }
}

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
