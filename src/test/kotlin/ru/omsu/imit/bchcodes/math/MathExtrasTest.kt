package ru.omsu.imit.bchcodes.math

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MathExtrasTest {

    @Test
    fun testGcd() {
        assertEquals(16, MathExtras.gcd(32, 48))
    }

    @Test
    fun testGcdCoprime() {
        assertEquals(1, MathExtras.gcd(10, 441))
    }

    @Test
    fun testGcdDivisor() {
        assertEquals(54, MathExtras.gcd(54, 162))
    }

    @Test
    fun testGcd0Neg() {
        assertEquals(162, MathExtras.gcd(0, -162))
    }

    @Test
    fun testGcd0Pos() {
        assertEquals(162, MathExtras.gcd(0, 162))
    }

    @Test
    fun testGcdPosNeg() {
        assertEquals(6, MathExtras.gcd(84, -30))
    }

    @Test
    fun testGcdNeg0() {
        assertEquals(162, MathExtras.gcd(-162, 0))
    }

    @Test
    fun testGcdPos0() {
        assertEquals(162, MathExtras.gcd(162, 0))
    }

    @Test
    fun testGcdNegPos() {
        assertEquals(6, MathExtras.gcd(-84, 30))
    }

    @Test
    fun testGcdZeros() {
        assertEquals(0, MathExtras.gcd(0, 0))
    }

    @Test
    fun testGcdNegNeg() {
        assertEquals(2, MathExtras.gcd(-158, -6))
    }
}