package com.seannajera.dkouple

interface Component {
    val id: String
    fun contentSameAs(otherComponent: Any): Boolean
}