package ru.omsu.imit.bchcodes.math

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class GF2PolynomialTest {

    private val gcdTestData = listOf(
            Pair(GF2Polynomial(2, 0), GF2Polynomial(1, 0)) to GF2Polynomial(1, 0),
            Pair(GF2Polynomial(3, 2, 0), GF2Polynomial(2, 1, 0)) to GF2Polynomial(0),
            Pair(GF2Polynomial(2, 0), GF2Polynomial(3, 2, 1, 0)) to GF2Polynomial(2, 0),
            Pair(GF2Polynomial(2, 1), GF2Polynomial(4, 3, 2, 0)) to GF2Polynomial(1, 0))

    private val sumTestData = listOf(
            Pair(GF2Polynomial(4, 3, 1), GF2Polynomial(16, 5, 7, 4)) to GF2Polynomial(16, 5, 7, 3, 1),
            Pair(GF2Polynomial(1, 11, 0, 5), GF2Polynomial(3, 1, 0)) to GF2Polynomial(11, 5, 3),
            Pair(GF2Polynomial(10, 4, 8, 1, 0), GF2Polynomial(3, 2, 1, 0)) to GF2Polynomial(10, 8, 4, 3, 2),
            Pair(GF2Polynomial(3, 2, 1), GF2Polynomial(10, 6, 5, 3, 2, 0)) to GF2Polynomial(10, 6, 5, 1, 0),
            Pair(GF2Polynomial(3, 2, 1), GF2Polynomial.ZERO) to GF2Polynomial(3, 2, 1)
    )

    private val multiplicationTestData = listOf(
            Pair(GF2Polynomial(4, 3, 1), GF2Polynomial(16, 5, 7, 4)) to GF2Polynomial(20, 19, 17, 11, 10, 9, 8, 7, 6, 5),
            Pair(GF2Polynomial(1, 11, 0, 5), GF2Polynomial(3, 1, 0)) to GF2Polynomial(14, 8, 4, 3, 12, 6, 2, 11, 5, 0),
            Pair(GF2Polynomial(10, 4, 1), GF2Polynomial(3, 2, 1, 0)) to GF2Polynomial(13, 7, 12, 6, 3, 11, 5, 2, 10, 1),
            Pair(GF2Polynomial(3, 2, 1), GF2Polynomial(10, 3, 2, 0)) to GF2Polynomial(13, 6, 12, 2, 11, 1),
            Pair(GF2Polynomial(3, 2, 1), GF2Polynomial.ONE) to GF2Polynomial(3, 2, 1),
            Pair(GF2Polynomial(3, 2, 1), GF2Polynomial.ZERO) to GF2Polynomial.ZERO
    )

    @TestFactory
    fun testGcd() = gcdTestData
            .map { (input, expected) ->
                DynamicTest.dynamicTest("when I calculate gcd(${input.first}, ${input.second}) then I get $expected") {
                    Assertions.assertEquals(expected, GF2Polynomial.gcd(input.first, input.second))
                }
            }

    @TestFactory
    fun testSum() = sumTestData
            .map { (input, expected) ->
                DynamicTest.dynamicTest("when I calculate ${input.first} + ${input.second} then I get $expected") {
                    Assertions.assertEquals(expected, input.first + input.second)
                }
            }

    @TestFactory
    fun testMul() = multiplicationTestData
            .map { (input, expected) ->
                DynamicTest.dynamicTest("when I calculate ${input.first} + ${input.second} then I get $expected") {
                    Assertions.assertEquals(expected, input.first * input.second)
                }
            }

    @ParameterizedTest
    @MethodSource("derivatives")
    fun testDerivatives(input: GF2Polynomial, expected: GF2Polynomial) {
        Assertions.assertEquals(expected, input.derivative())
    }

    companion object {
        @JvmStatic
        fun derivatives() = listOf(
                Arguments.of(GF2Polynomial.ONE, GF2Polynomial.ZERO),
                Arguments.of(GF2Polynomial(1, 2, 7, 5, 6), GF2Polynomial(0, 4, 6))
        )
    }
}