package com.seannajera.componentview

interface ComponentModel {
    val id: String
    val layout: ComponentLayout
    fun contentSameAs(otherItem: Any): Boolean
}