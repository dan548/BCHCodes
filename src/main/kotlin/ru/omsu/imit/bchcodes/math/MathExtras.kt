package ru.omsu.imit.bchcodes.math

import kotlin.math.abs

object MathExtras {

    fun gcd(n1 : Int, n2 : Int) : Int {
        if (n2 == 0) return abs(n1)
        return abs(gcd(n2, n1 % n2))
    }

}