package ru.omsu.imit.bchcodes.math

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import ru.omsu.imit.bchcodes.exception.MatrixException

class SquareBinaryMatrixTest {

    private lateinit var matrix : SquareBinaryMatrix

    private val rankTestData = listOf(
            arrayOf(arrayOf(1, 0, 1), arrayOf(1, 1, 1), arrayOf(1, 0, 1)) to 2,
            arrayOf(arrayOf(1, 1), arrayOf(1, 1)) to 1,
            arrayOf(arrayOf(0, 1, 1, 1, 1), arrayOf(1, 0, 0, 0, 0), arrayOf(1, 1, 0, 1, 0), arrayOf(0, 1, 1, 0, 0), arrayOf(0, 1, 0, 1, 1)) to 5,
            arrayOf(arrayOf(1, 0, 1, 0), arrayOf(0, 1, 1, 1), arrayOf(1, 0, 1, 1), arrayOf(0, 1, 0, 1)) to 4
    )

    @BeforeEach
    fun before() {
        matrix = SquareBinaryMatrix(10)
    }

    @TestFactory
    fun testRank() = rankTestData
            .map { (input, expected) ->
                DynamicTest.dynamicTest("rank $input") {
                    assertEquals(expected, SquareBinaryMatrix(input).getRank())
                }
            }

    @Test
    fun testIdentityMatrixZeroSize() {
        assertThrows<MatrixException> { SquareBinaryMatrix.identityMatrix(0) }
    }

    @Test
    fun testIdentityMatrixNegativeSize() {
        assertThrows<MatrixException> { SquareBinaryMatrix.identityMatrix(-7) }
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 8, 10, 11, 15, 28])
    fun testIdentityMatrix(input : Int) {
        val matr = SquareBinaryMatrix.identityMatrix(input)
        for (i in 0 until input) {
            for (j in 0 until input) {
                if (i == j) assertTrue(matr[i, j])
                else assertFalse(matr[i, j])
            }
        }
    }

    @ParameterizedTest
    @MethodSource("nonSquareMatrices")
    fun testNotSquare(input : Array<Array<Int>>) {
        assertThrows<MatrixException> { SquareBinaryMatrix(input) }
    }

    @ParameterizedTest
    @MethodSource("nonBinaryMatrices")
    fun testNotBinary(input : Array<Array<Int>>) {
        assertThrows<MatrixException> { SquareBinaryMatrix(input) }
    }

    companion object {
        @JvmStatic
        fun goodMatrices() = listOf(
                arrayOf(arrayOf(1, 0, 1), arrayOf(1, 1, 1), arrayOf(1, 0, 1)),
                arrayOf(arrayOf(1, 1), arrayOf(1, 1)),
                arrayOf(arrayOf(1, 0, 1, 0), arrayOf(0, 1, 1, 1), arrayOf(1, 0, 1, 1), arrayOf(0, 1, 0, 1)),
                arrayOf(arrayOf(0, 1, 1, 1, 1), arrayOf(1, 0, 0, 0, 0), arrayOf(1, 1, 0, 1, 0), arrayOf(0, 1, 1, 0, 0), arrayOf(0, 1, 0, 1, 1))
        )
        @JvmStatic
        fun nonSquareMatrices() = listOf(
            arrayOf(arrayOf(1, 0, 1), arrayOf(1, 1, 2)),
            arrayOf(arrayOf(1, 0, 1), arrayOf(1, 1), arrayOf(1, 1)),
            arrayOf(arrayOf(1, 0, 1), arrayOf(1, 1, 1), arrayOf(0, 1, 1), arrayOf(1, 0, 1)),
            arrayOf(arrayOf(0, 1), arrayOf(1, 1, 0), arrayOf(1, 1))
        )
        @JvmStatic
        fun nonBinaryMatrices() = listOf(
            arrayOf(arrayOf(-1, 0, 1), arrayOf(1, 1, 2), arrayOf(-1, 3, 1)),
            arrayOf(arrayOf(1, 0, 1), arrayOf(1, 1, 2), arrayOf(1, 3, 1)),
            arrayOf(arrayOf(-1, 0, 1), arrayOf(1, 1, 1), arrayOf(-1, 0, 1))
        )
        @JvmStatic
        fun wrongIndices() = listOf(
            Arguments.of(-1, 1),
            Arguments.of(35, 4),
            Arguments.of(-3, 1),
            Arguments.of(0, 28),
            Arguments.of(5, 27),
            Arguments.of(-1, -1)
        )
    }

    @ParameterizedTest
    @MethodSource("wrongIndices")
    fun testGetWrongIndices(i: Int, j : Int) {
        assertThrows<MatrixException> { matrix[i, j] }
    }

    @Test
    fun testSet() {
        matrix[0, 0] = true
        assertTrue(matrix[0, 0])
    }

    @ParameterizedTest
    @MethodSource("wrongIndices")
    fun testSetWrongIndices(i: Int, j: Int) {
        assertThrows<MatrixException> { matrix[i, j] = true }
    }

    @Test
    fun testPlusAssign() {
        val matr2 = SquareBinaryMatrix.identityMatrix(10)
        matrix += matr2
        assertEquals(matr2, matrix)
    }

    @Test
    fun testPlusAssignDifferentSizes() {
        val matr2 = SquareBinaryMatrix.identityMatrix(5)
        assertThrows<MatrixException> { matrix += matr2 }
    }

    @ParameterizedTest
    @MethodSource("goodMatrices")
    fun testSumRows(input : Array<Array<Int>>) {
        val inputMatrix = SquareBinaryMatrix(input)
        inputMatrix.sumRows(0, 1)
        for (j in 0 until inputMatrix.size) {
            assertEquals(input[0][j].xor(input[1][j]), if (inputMatrix[0, j]) 1 else 0)
        }
    }

    @ParameterizedTest
    @MethodSource("goodMatrices")
    fun testSumRowsWrongIndices(input : Array<Array<Int>>) {
        val inputMatrix = SquareBinaryMatrix(input)
        assertThrows<MatrixException> { inputMatrix.sumRows(1, -1) }
    }

    @ParameterizedTest
    @MethodSource("goodMatrices")
    fun testSwapRows(input : Array<Array<Int>>) {
        val m = SquareBinaryMatrix(input)
        m.swapRows(1, 0)
        for (j in 0 until m.size) {
            assertEquals(input[1][j], if (m[0, j]) 1 else 0)
            assertEquals(input[0][j], if (m[1, j]) 1 else 0)
        }
    }

    @ParameterizedTest
    @MethodSource("goodMatrices")
    fun testSwapColumns(input : Array<Array<Int>>) {
        val m = SquareBinaryMatrix(input)
        m.swapColumns(1, 0)
        for (j in 0 until m.size) {
            assertEquals(input[j][1], if (m[j, 0]) 1 else 0)
            assertEquals(input[j][0], if (m[j, 1]) 1 else 0)
        }
    }

    @ParameterizedTest
    @MethodSource("goodMatrices")
    fun testSwapRowsWrongIndices(input : Array<Array<Int>>) {
        val m = SquareBinaryMatrix(input)
        assertThrows<MatrixException> { m.swapRows(1, -2) }
    }

    @ParameterizedTest
    @MethodSource("goodMatrices")
    fun testSwapColumnsWrongIndices(input : Array<Array<Int>>) {
        val m = SquareBinaryMatrix(input)
        assertThrows<MatrixException> { m.swapColumns(1, -1) }
    }

}