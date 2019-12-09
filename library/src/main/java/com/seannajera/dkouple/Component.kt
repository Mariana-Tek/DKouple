package com.seannajera.dkouple

interface Component {
    val id: String
    val layout: ComponentLayout
    fun contentSameAs(otherComponent: Any): Boolean
}