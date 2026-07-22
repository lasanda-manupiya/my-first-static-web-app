package com.routewake.app.model

import java.util.Calendar

/** Gender captured at onboarding — only used to pick a friendly avatar. */
enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    UNSPECIFIED("Prefer not to say");

    companion object {
        fun fromName(name: String?): Gender =
            entries.firstOrNull { it.name == name } ?: UNSPECIFIED
    }
}

/** Coarse life-stage buckets, derived from age, used to tint the avatar. */
enum class AgeGroup { CHILD, TEEN, ADULT, SENIOR, UNKNOWN }

/**
 * The person using RouteWake. Captured once during onboarding and persisted in
 * DataStore. Used to personalize greetings, choose an avatar, and send a
 * birthday notification.
 */
data class UserProfile(
    val name: String = "",
    val birthYear: Int = 0,
    val birthMonth: Int = 0, // 1-12
    val birthDay: Int = 0,   // 1-31
    val gender: Gender = Gender.UNSPECIFIED,
    val onboarded: Boolean = false
) {
    /** True once a valid day/month has been captured. */
    val hasBirthday: Boolean
        get() = birthMonth in 1..12 && birthDay in 1..31

    /** First name only, for friendly greetings. */
    val firstName: String
        get() = name.trim().substringBefore(' ').ifBlank { name.trim() }

    /** Uppercase first initial, for the avatar fallback. */
    val initial: String
        get() = firstName.firstOrNull()?.uppercase() ?: "?"

    /** True if the given calendar day is this user's birthday (month + day match). */
    fun isBirthday(calendar: Calendar): Boolean =
        hasBirthday &&
            calendar.get(Calendar.MONTH) + 1 == birthMonth &&
            calendar.get(Calendar.DAY_OF_MONTH) == birthDay

    /** Age turning this [year], or null if the birth year is unknown. */
    fun turningAge(year: Int): Int? =
        if (birthYear in 1900..year) year - birthYear else null

    /** Age as of [year] (has this year's birthday passed doesn't matter for tinting). */
    fun ageGroup(year: Int): AgeGroup {
        val age = turningAge(year) ?: return AgeGroup.UNKNOWN
        return when {
            age < 13 -> AgeGroup.CHILD
            age < 20 -> AgeGroup.TEEN
            age < 60 -> AgeGroup.ADULT
            else -> AgeGroup.SENIOR
        }
    }
}
