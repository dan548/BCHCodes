package ru.omsu.imit.bchcodes.math

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SquarePolynomialMatrixTest {

    private val field = Char2GaloisField(4)
    private lateinit var matrix : Char2GaloisField.SquarePolynomialMatrix

    @BeforeEach
    fun init() {
        matrix = field.SquarePolynomialMatrix(5)
    }

    @Test
    fun testSet() {

    }

    @Test
    fun testGet() {

    }

    @Test
    fun testDeterminant() {

    }

    @Test
    fun testCopy() {

    }

    @Test
    fun testCreate() {

    }

}