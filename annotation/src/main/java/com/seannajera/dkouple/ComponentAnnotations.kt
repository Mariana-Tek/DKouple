package com.seannajera.dkouple

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DKoupleComponent(val layoutId: Int)

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class DKoupleView(val layoutId: Int)
