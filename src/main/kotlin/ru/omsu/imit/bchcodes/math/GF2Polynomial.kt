package ru.omsu.imit.bchcodes.math

import ru.omsu.imit.bchcodes.exception.PolynomialException
import java.math.BigInteger

class GF2Polynomial {

    companion object {
        fun gcd(first : GF2Polynomial, second : GF2Polynomial) : GF2Polynomial {
            var f : GF2Polynomial
            var g : GF2Polynomial
            if (first.degree < second.degree) {
                f = second
                g = first
            } else {
                f = first
                g = second
            }
            var divResult = f.divide(g)
            while (divResult.second.degree != -1) {
                f = g
                g = divResult.second
                divResult = f.divide(g)
            }
            return g
        }
    }

    private var polynomial : BigInteger
        set(value) {
            field = value
            degree = value.bitLength() - 1
        }
    var degree : Int

    constructor(vararg powers : Int) {
        polynomial = BigInteger("0")
        if (powers.any { it < 0 }) throw PolynomialException("Negative powers don't exist!")
        for (power in powers) {
            if (polynomial.testBit(power)) throw PolynomialException("Repeated powers!") else polynomial = polynomial.setBit(power)
        }
        degree = polynomial.bitLength() - 1
    }

    constructor(powers : List<Int>) {
        polynomial = BigInteger("0")
        if (powers.any { it < 0 }) throw PolynomialException("Negative powers don't exist!")
        for (power in powers) {
            if (polynomial.testBit(power)) throw PolynomialException("Repeated powers!") else polynomial = polynomial.setBit(power)
        }
        degree = polynomial.bitLength() - 1
    }

    constructor(number : BigInteger) {
        polynomial = number
        degree = polynomial.bitLength() - 1
    }

    operator fun plus(increment : GF2Polynomial) : GF2Polynomial {
        return GF2Polynomial(polynomial.xor(increment.polynomial))
    }

    operator fun times(multiplier : GF2Polynomial) : GF2Polynomial {
        var res = BigInteger(0, ByteArray(1))
        for (i in 0..multiplier.degree) {
            if (multiplier.getPowerCoeff(i)) {
                res = res.xor(polynomial.shiftLeft(i))
            }
        }
        return GF2Polynomial(res)
    }

    fun divide(divisor : GF2Polynomial) : Pair<GF2Polynomial, GF2Polynomial> {
        if (divisor.degree == 0) return Pair(this, GF2Polynomial())
        val quotient = GF2Polynomial()
        val curDividend = this
        var quotientDegree = degree - divisor.degree
        while (quotientDegree >= 0) {
            curDividend.polynomial = curDividend.polynomial.xor(divisor.polynomial.shiftLeft(quotientDegree))
            quotient.polynomial = quotient.polynomial.setBit(quotientDegree)
            quotientDegree = curDividend.degree - divisor.degree
        }
        return Pair(quotient, curDividend)
    }

    fun derivative() : GF2Polynomial {
        val list = mutableListOf<Int>()
        for (i in 1..degree) {
            if (i % 2 == 1 && getPowerCoeff(i)) {
                list.add(i-1)
            }
        }
        return GF2Polynomial(list)
    }

    fun printPolynomial() {
        val list = ArrayList<String>()
        for (i in degree downTo 2) {
            if (getPowerCoeff(i)) {
                list.add("x^$i")
            }
        }
        if (getPowerCoeff(1)) {
            list.add("x")
        }
        if (getPowerCoeff(0)) {
            list.add("1")
        }
        if (list.isEmpty()) {
            print("0")
        } else {
            print(list.joinToString(" + "))
        }
    }

    fun getPowerCoeff(power : Int) = polynomial.testBit(power)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GF2Polynomial

        if (polynomial != other.polynomial) return false

        return true
    }

    override fun hashCode(): Int {
        return polynomial.hashCode()
    }

}