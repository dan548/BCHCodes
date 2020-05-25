package ru.omsu.imit.bchcodes.math

import ru.omsu.imit.bchcodes.exception.GaloisFieldException
import ru.omsu.imit.bchcodes.exception.PolynomialException
import java.math.BigInteger
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow

class Char2GaloisField(k: Int) {

    private val pow = k
    val order = (2.0).pow(k).toInt()
    lateinit var primitivePowersTable : ArrayList<GF2Polynomial>

    inner class SquarePolynomialMatrix {
        private val matrix : Array<GF2Polynomial>
        val size : Int

        constructor(n : Int) {
            size = n
            matrix = Array(n*n) { GF2Polynomial() }
            for (i in 0 until n) {
                for (j in 0 until n) {
                    matrix[i*n+j] = GF2Polynomial()
                }
            }
        }

        constructor(mtr : SquarePolynomialMatrix) {
            matrix = mtr.matrix.copyOf()
            size = mtr.size
        }

        operator fun set(i : Int, j : Int, value : GF2Polynomial) {
            matrix[i*size+j] = value
        }

        operator fun get(i : Int, j : Int) : GF2Polynomial {
            return matrix[i*size+j]
        }

        fun determinant() : GF2Polynomial {
            val copy = SquarePolynomialMatrix(this)

            var res = GF2Polynomial(0)
            var tempElem : GF2Polynomial
            var temp : Int

            for (k in 0 until size-1) {
                if (copy[k, k] == GF2Polynomial()) {
                    temp = k + 1
                    while (copy[temp, k] == GF2Polynomial()) {
                        temp++
                        if (temp > k + 1) {
                            return GF2Polynomial()
                        }
                    }
                    for (j in 0 until size) {
                        tempElem = copy[k, j]
                        copy[k, j] = copy[temp, j]
                        copy[temp, j] = tempElem
                    }
                }
                for (l in k+1 until size) {
                    tempElem = multiplyPolynomials(copy[l, k], raiseToThePower(copy[k, k], -1))
                    copy[l, k] = GF2Polynomial()
                    for (m in k+1 until size) {
                        copy[l, m] = sumPolynomials(copy[l, m], multiplyPolynomials(copy[k, m], tempElem))
                    }
                }
            }
            for (i in 0 until size) {
                res = multiplyPolynomials(res, copy[i, i])
            }
            return res
        }
    }

    inner class TwoVariableGF2Polynomial {

        var xDegree : Int
        val powerMap : SortedMap<Int, GF2Polynomial>

        constructor(powers: Map<Int, GF2Polynomial>) {
            if (powers.keys.any { it < 0 }) throw PolynomialException("Negative powers don't exist!")
            powerMap = powers.toSortedMap(Comparator.reverseOrder())
            xDegree = if (powerMap.keys.isEmpty()) {
                -1
            } else {
                powerMap.keys.first()
            }
        }

        constructor(listPolynomials : List<GF2Polynomial>) {
            powerMap = listPolynomials.dropLastWhile { it.degree < 0 }.mapIndexed { index, polynomial -> index to polynomial }.toMap().toSortedMap(Comparator.reverseOrder())
            xDegree = if (powerMap.keys.isEmpty()) {
                -1
            } else {
                powerMap.keys.first()
            }
        }

        constructor(clone : TwoVariableGF2Polynomial) {
            xDegree = clone.xDegree
            val map = TreeMap<Int, GF2Polynomial>(Comparator.reverseOrder())
            map.putAll(clone.powerMap)
            powerMap = map
        }

        operator fun times(multiplier : TwoVariableGF2Polynomial) : TwoVariableGF2Polynomial {
            val map : MutableMap<Int, GF2Polynomial> = HashMap()
            for (entry in powerMap) {
                for (mEntry in multiplier.powerMap) {
                    val key = entry.key + mEntry.key
                    if (map[key] == null) {
                        map[key] = multiplyPolynomials(entry.value, mEntry.value)
                    } else {
                        map[key] = sumPolynomials(map[key]!!, multiplyPolynomials(entry.value, mEntry.value))
                    }
                }
            }
            return TwoVariableGF2Polynomial(map)
        }

        operator fun div(divisor : TwoVariableGF2Polynomial) : TwoVariableGF2Polynomial {
            val divDeg = divisor.xDegree
            var tempXDeg = xDegree
            var result = TwoVariableGF2Polynomial(mapOf(Pair(0,
                GF2Polynomial()
            )))
            var src = TwoVariableGF2Polynomial(this)
            val divAlphaMaxDeg = divisor.powerMap[divDeg]
            while (tempXDeg >= divDeg) {
                val quotient = TwoVariableGF2Polynomial(mapOf(Pair(tempXDeg - divDeg, multiplyPolynomials(src.powerMap[tempXDeg]!!, raiseToThePower(divAlphaMaxDeg!!, -1)))))
                result += quotient
                src += quotient * divisor
                tempXDeg = src.xDegree
            }
            return result
        }

        operator fun plus(multiplier : TwoVariableGF2Polynomial) : TwoVariableGF2Polynomial {
            val deg = multiplier.xDegree.coerceAtLeast(xDegree)
            val map : MutableMap<Int, GF2Polynomial> = TreeMap()
            for (i in 0..deg) {
                val aPol = powerMap[i]
                val bPol = multiplier.powerMap[i]
                if (aPol == null) {
                    if (bPol == null) {
                        continue
                    }
                    map[i] = bPol
                } else {
                    if (bPol == null) {
                        map[i] = aPol
                    } else {
                        val sum = sumPolynomials(aPol, bPol)
                        if (sum != GF2Polynomial()) map[i] = sum else map.remove(i)
                    }
                }
            }
            return TwoVariableGF2Polynomial(map)
        }

        fun convertToOneVariable() : GF2Polynomial {
            if (!powerMap.values.map { it.degree }.any { it > 0 }) {
                val powerList : MutableList<Int> = mutableListOf()
                for (key in powerMap.keys) {
                    if (powerMap[key]!!.degree == 0) {
                        powerList.add(key)
                    }
                }
                return GF2Polynomial(powerList)
            }
            throw PolynomialException("Can't convert")
        }

        fun evalPolynomial(arg : GF2Polynomial) : GF2Polynomial {
            var res = GF2Polynomial()
            for (i in 0..xDegree) {
                val zCoeff = powerMap[i]
                if (zCoeff != null) {
                    res += multiplyPolynomials(zCoeff, raiseToThePower(arg, i))
                }
            }
            return res
        }
    }

    companion object {
        private var sequence = listOf(BigInteger.ONE)
        private val irreduciblePolynomials : MutableMap<Int, GF2Polynomial> = mutableMapOf()

        private fun findIrreducible(degree : Int) : GF2Polynomial {
            var list = getSequence(degree - 1)
            list = list.stream()
                .map { num -> num.shiftLeft(1).setBit(0).setBit(degree) }
                .collect(Collectors.toList())
            for (attempt in list) {
                if (isIrreducible(
                        GF2Polynomial(attempt)
                    )
                ) {
                    val res = GF2Polynomial(attempt)
                    irreduciblePolynomials[degree] = res
                    return res
                }
            }
            return GF2Polynomial()
        }

        private fun getSequence(n : Int) : List<BigInteger> {
            val requestedSize = (2.0).pow(n - 1).toInt()
            if (sequence.size >= requestedSize) {
                return sequence.subList(0, requestedSize)
            }
            val list : MutableList<BigInteger> = getSequence(
                n - 1
            ).toMutableList()
            var cur = BigInteger.ZERO
            val number = (BigInteger.ZERO).setBit(n-1)
            while (cur < number) {
                if (cur !in list) {
                    list.add(cur + number)
                }
                cur = ++cur
            }
            sequence = list
            return list
        }

        private fun isIrreducible(polynomial : GF2Polynomial) : Boolean {
            val g = polynomial.derivative()
            val pow = polynomial.degree
            if (g.degree == -1) {
                return false
            }
            val res = GF2Polynomial.gcd(polynomial, g)
            if (res != GF2Polynomial(0)) {
                return false
            }
            val phi = SquareBinaryMatrix(pow)
            for (i in 0 until pow) {
                val modPolynomial = GF2Polynomial(2 * i).divide(polynomial).second
                for (j in 0 until pow) {
                    phi[i, j] = modPolynomial.getPowerCoeff(j)
                }
            }
            phi += SquareBinaryMatrix.identityMatrix(pow)
            return pow - phi.getRank() == 1
        }

    }

    init {
        irreduciblePolynomials[2] =
            GF2Polynomial(2, 1, 0)
        irreduciblePolynomials[3] =
            GF2Polynomial(3, 1, 0)
        irreduciblePolynomials[4] =
            GF2Polynomial(4, 1, 0)
        var checkPrimitive = BigInteger.TWO
        var polynomial = GF2Polynomial(checkPrimitive)
        var isFound = false
        while (!isFound) {
            isFound = true
            var curPolynomial = polynomial
            primitivePowersTable = arrayListOf(curPolynomial)
            for (i in 2 until order-1) {
                curPolynomial *= polynomial
                if (curPolynomial.degree >= k) {
                    curPolynomial = curPolynomial.divide(irreduciblePolynomials.getOrElse(k){
                        findIrreducible(
                            k
                        )
                    }).second
                }
                if (curPolynomial != GF2Polynomial(0)) {
                    primitivePowersTable.add(curPolynomial)
                    continue
                }
                isFound = false
                checkPrimitive = checkPrimitive.add(BigInteger.ONE)
                polynomial = GF2Polynomial(checkPrimitive)
                break
            }
        }
    }

    fun sumPolynomials(first : GF2Polynomial, second : GF2Polynomial) : GF2Polynomial {
        if (first.degree < pow && second.degree < pow) {
            return (first + second)
        }
        throw GaloisFieldException("Unknown field elements.")
    }

    fun multiplyPolynomials(first : GF2Polynomial, second : GF2Polynomial) : GF2Polynomial {
        if (first.degree < pow && second.degree < pow) {
            if (first.degree == -1 || second.degree == -1) {
                return GF2Polynomial()
            }
            val firstPrimitivePower = primitivePowersTable.indexOf(first) + 1
            val secondPrimitivePower = primitivePowersTable.indexOf(second) + 1
            val primitivePower = Math.floorMod((firstPrimitivePower + secondPrimitivePower), (order - 1))
            if (primitivePower == 0) {
                return GF2Polynomial(0)
            }
            return primitivePowersTable[primitivePower - 1]
        }
        throw GaloisFieldException("Unknown field elements.")
    }

    fun convertOneToTwoVariable(pol: GF2Polynomial) : TwoVariableGF2Polynomial {
        val map = mutableMapOf<Int, GF2Polynomial>()
        for (i in 0..pol.degree) {
            if (pol.getPowerCoeff(i)) map[i] = GF2Polynomial(0)
        }
        return TwoVariableGF2Polynomial(map)
    }

    fun raiseToThePower(polynomial : GF2Polynomial, power : Int): GF2Polynomial {
        if (polynomial.degree < pow) {
            if (power == 1 || polynomial.degree == -1) return polynomial
            if (power == 0) return GF2Polynomial(0)
            val primitivePower = primitivePowersTable.indexOf(polynomial) + 1
            val calculatedPower = Math.floorMod((primitivePower * power), (order - 1))
            if (calculatedPower == 0) {
                return GF2Polynomial(0)
            }
            return primitivePowersTable[calculatedPower - 1]
        }
        throw GaloisFieldException("Unknown field element.")
    }

    fun getPrimitiveElement() : GF2Polynomial = primitivePowersTable[0]

    fun getPrimitiveDegree(pol : GF2Polynomial) : Int? {
        if (pol == GF2Polynomial()) return null
        return primitivePowersTable.indexOf(pol) + 1
    }

    fun evalPolynomial(polynomial : GF2Polynomial, arg : GF2Polynomial) : GF2Polynomial {
        var res = GF2Polynomial()
        for (i in 0..polynomial.degree) {
            if (polynomial.getPowerCoeff(i)) {
                res += raiseToThePower(arg, i)
            }
        }
        return res
    }
}