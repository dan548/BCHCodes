package ru.omsu.imit.bchcodes.math

import ru.omsu.imit.bchcodes.exception.MatrixException
import java.util.*

class SquareBinaryMatrix {

    private val matrix = BitSet()
    val size : Int

    companion object {
        fun identityMatrix(size : Int) : SquareBinaryMatrix {
            val res = SquareBinaryMatrix(size)
            for (i in 0 until size) {
                res.matrix.set(i * (size + 1))
            }
            return res
        }
    }

    constructor(size : Int) {
        if (size <= 0) throw MatrixException("Bad size")
        this.size = size
    }

    constructor(array : Array<Array<Int>>) {
        val rows = array.size
        size = rows
        if (rows != 0) {
            for (row in 0 until rows) {
                val line = array[row]
                if (line.size != rows) {
                    throw MatrixException("The matrix is not square!")
                }
                for (j in 0 until size) {
                    val elem = line[j]
                    if (elem == 1) {
                        matrix.set(row * size + j)
                    } else {
                        if (elem != 0) {
                            throw MatrixException("The matrix is not a binary one!")
                        }
                    }
                }
            }
        }
    }

    operator fun get(i : Int, j : Int) : Boolean {
        if (i < 0 || j < 0) {
            throw MatrixException("Invalid index.")
        }
        if (size <= i || size <= j) {
            throw MatrixException("Invalid index.")
        }
        return matrix[size * i + j]
    }

    operator fun set(i : Int, j : Int, value : Boolean) {
        if (i < 0 || j < 0) {
            throw MatrixException("Invalid index.")
        }
        if (size <= i || size <= j) {
            throw MatrixException("Invalid index.")
        }
        matrix[size * i + j] = value
    }

    operator fun plusAssign(increment : SquareBinaryMatrix) {
        if (size != increment.size) {
            throw MatrixException("The matrices have different sizes!")
        }
        matrix.xor(increment.matrix)
    }

    fun getRank() : Int {
        var rank = size
        var row = 0

        while (row < rank) {
            if (this[row, row]) {
                for (i in row+1 until size) {
                    if (this[i, row]) {
                        sumRows(i, row)
                    }
                }
                row++
            } else {
                var isNotZero = false
                for (i in row+1 until size) {
                    if (this[i, row]) {
                        isNotZero = true
                        swapRows(i, row)
                        break
                    }
                }
                if (!isNotZero) {
                    swapColumns(row, rank-1)
                    rank--
                }
            }
        }
        return rank
    }

    fun swapColumns(j : Int, k : Int) {
        for (t in 0 until size) {
            val temp = this[t, j]
            this[t, j] = this[t, k]
            this[t, k] = temp
        }
    }

    fun swapRows(i : Int, k : Int) {
        for (t in 0 until size) {
            val temp = this[i, t]
            this[i, t] = this[k, t]
            this[k, t] = temp
        }
    }

    fun sumRows(sumTo : Int, rowToAdd : Int) {
        for (t in 0 until size) {
            this[sumTo, t] = this[sumTo, t].xor(this[rowToAdd, t])
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SquareBinaryMatrix

        if (matrix != other.matrix) return false

        return true
    }

    override fun hashCode(): Int {
        return matrix.hashCode()
    }


}