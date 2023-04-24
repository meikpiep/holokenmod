package com.holokenmod

interface Randomizer {
    fun discard()
    fun nextInt(maximumNumber: Int): Int
    fun nextDouble(): Double
}