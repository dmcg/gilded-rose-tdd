package com.gildedrose.theory

/**
 * Immutable data.
 */
@Target(AnnotationTarget.CLASS)
annotation class Data

/**
 * Code that will always return the same output
 * for the same input.
 * Does not depend on when or whether or how many
 * times we run it.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class Calculation

/**
 * All code that is not a [Calculation].
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class Action
