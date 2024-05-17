package com.gildedrose.definitions

/**
 * Noun
 *
 * a word (other than a pronoun) used to identify any of a class of people, places, or things (common noun), or to name a particular one of these (proper noun).
 *
 * banana, car, manager
 *
 * Objects
 */
annotation class Noun

/**
 * Verb
 *
 * a word used to describe an action, state, or occurrence, and forming the main part of the predicate of a sentence, such as hear, become, happen.
 *
 * run, eat, save
 *
 * Methods, functions
 */
annotation class Verb

/**
 * Preposition
 *
 * a word governing, and usually preceding, a noun or pronoun and expressing a relation to another word or element in the clause, as in ‘the man on the platform’, ‘she arrived after dinner’, ‘what did you do it for ?’.
 *
 * Give direction, or more generally the relationship, between things
 *
 * for, from, of, to, as...
 */
annotation class Preposition

/**
 * a word used to connect clauses or sentences or to coordinate words in the same clause (e.g. and, but, if ).
 *
 * Join clauses and give structure to sentences
 *
 * and, or, so, since, for, because, as, but, yet, still, while
 *
 * Scope functions
 */
annotation class Conjunction


/**
 * a word that can function as a noun phrase used by itself and that refers either to the participants in the discourse (e.g. I, you ) or to someone or something mentioned elsewhere in the discourse (e.g. she, it, this ).
 *
 * Stands in for something bigger
 *
 * she, it, this
 *
 * Lambda parameter, receiver
 */
annotation class Pronoun

/**
 * a set of forms taken by a verb to indicate the time (and sometimes also the continuance or completeness) of the action in relation to the time of the utterance.
 * "the future tense"
 *
 * Do this
 *
 * This is how to do this
 */
annotation class Tense
