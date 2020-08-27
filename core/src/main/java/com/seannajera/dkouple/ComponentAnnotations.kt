package com.seannajera.dkouple

import kotlin.reflect.KClass

/**
 * This annotation must be placed on every Component in order to correctly map it to its
 * associated ComponentView in ComponentAdapter.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DKoupleComponent(val layoutId: Int, val viewClass: KClass<out BaseView>)

/**
 * This annotation must be placed on a ComponentView when using the annotation processor module
 * in DKouple to auto generate the DKoupleComponentFactory
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DKoupleView

/**
 * This annotation must be placed in a ComponentView's View constructor paramter when using the
 * annotation processor module in DKouple to auto generate the DKoupleComponentFactory
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class FactoryView
