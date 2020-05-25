package ru.omsu.imit.bchcodes.code

import ru.omsu.imit.bchcodes.math.Char2GaloisField
import ru.omsu.imit.bchcodes.math.GF2Polynomial
import ru.omsu.imit.bchcodes.math.MathExtras
import ru.omsu.imit.bchcodes.exception.CodeException
import ru.omsu.imit.bchcodes.exception.MatrixException
import java.util.*

abstract class BchCode(first: Int, t: Int, type: CodeType) {

    protected val field : Char2GaloisField
    val codeLength : Int // n
    val infoLength : Int // k
    val errorCorrectNumber : Int // t
    val fieldOrderPowerOfTwo : Int // m
    private val fieldOrder : Int // 2^m
    val generatorPolynomialTwoVariable : Char2GaloisField.TwoVariableGF2Polynomial
    val isErrorOne : Boolean // являются ли ошибки равными 1

    init {
        when (type) {
            CodeType.REED_SOLOMON -> {
                errorCorrectNumber = t
                fieldOrderPowerOfTwo = first
                if (fieldOrderPowerOfTwo <= 1) {
                    throw CodeException("The code length must be more than 1")
                }
                fieldOrder = 1.shl(fieldOrderPowerOfTwo)
                codeLength = fieldOrder - 1
                infoLength = codeLength - 2*errorCorrectNumber
                if (infoLength < 1) {
                    throw CodeException("Too much errors to correct")
                }
                field = Char2GaloisField(fieldOrderPowerOfTwo)

                val alpha = field.getPrimitiveElement()
                var tempGen = field.TwoVariableGF2Polynomial(mapOf(Pair(1,
                    GF2Polynomial(0)
                ), Pair(0, alpha)))

                for (i in 2..2*errorCorrectNumber) {
                    val alphaPowered = field.raiseToThePower(alpha, i)
                    val temp = field.TwoVariableGF2Polynomial(mapOf(Pair(1,
                        GF2Polynomial(0)
                    ), Pair(0, alphaPowered)))
                    tempGen *= temp
                }

                generatorPolynomialTwoVariable = tempGen
                isErrorOne = false
            }
            CodeType.BINARY_BCH -> {
                codeLength = first

                if (codeLength - 2*t <= 0) {
                    throw CodeException("Too much errors to correct")
                }

                if (codeLength % 2 == 0) {
                    throw CodeException("The code length must be odd!")
                }

                var mersenneNumber = 3
                var m = 2
                while (mersenneNumber % codeLength != 0) {
                    mersenneNumber = mersenneNumber * 2 + 1
                    m++
                }
                fieldOrderPowerOfTwo = m
                fieldOrder = 2.shl(fieldOrderPowerOfTwo)
                field = Char2GaloisField(fieldOrderPowerOfTwo)
                errorCorrectNumber = t

                val alphaDegree = (fieldOrder - 1) / codeLength
                val beta = field.raiseToThePower(field.getPrimitiveElement(), alphaDegree)

                val setMinimal = mutableSetOf<GF2Polynomial>()

                for (i in 1..2*errorCorrectNumber step 2) {
                    val elem = field.raiseToThePower(beta, i)
                    val gcd = MathExtras.gcd(
                        alphaDegree * i % (field.order - 1),
                        field.order - 1
                    )
                    val mod = (field.order - 1) / gcd

                    var minPolynomial = field.TwoVariableGF2Polynomial(
                        mapOf(
                            Pair(1, GF2Polynomial(0)),
                            Pair(0, elem)
                        )
                    )
                    var r = 1
                    var powerOfTwo = 2

                    while (powerOfTwo % mod != 1) {
                        val temp = field.TwoVariableGF2Polynomial(
                            mapOf(
                                Pair(1, GF2Polynomial(0)),
                                Pair(0, field.raiseToThePower(elem, powerOfTwo))
                            )
                        )
                        minPolynomial *= temp
                        r++
                        powerOfTwo *= 2
                    }

                    val minimalPolynomialOneVariable = minPolynomial.convertToOneVariable()
                    setMinimal.add(minimalPolynomialOneVariable)
                }

                val generatorPolynomial = setMinimal.reduce { acc, nextPolynomial -> acc * nextPolynomial }

                infoLength = codeLength - generatorPolynomial.degree
                generatorPolynomialTwoVariable = field.convertOneToTwoVariable(generatorPolynomial)
                isErrorOne = true
            }
        }
    }

    abstract fun encode(message : ByteArray) : ByteArray

    abstract fun decode(codeword : ByteArray) : ByteArray

    protected fun decodePGZ(encoded : Char2GaloisField.TwoVariableGF2Polynomial) : Char2GaloisField.TwoVariableGF2Polynomial {
        val syndrom : MutableList<GF2Polynomial> = mutableListOf()

        val alpha = field.getPrimitiveElement()
        for (j in 1..2*errorCorrectNumber) {
            syndrom.add(encoded.evalPolynomial(field.raiseToThePower(alpha, j)))
        }
        var nu = errorCorrectNumber
        var isDetZero = true
        var m : Char2GaloisField.SquarePolynomialMatrix = field.SquarePolynomialMatrix(nu)
        while (isDetZero) {
            m = field.SquarePolynomialMatrix(nu)
            for (row in 0 until nu) {
                for (col in 0 until nu) {
                    m[row, col] = syndrom[row+col]
                }
            }
            val det = m.determinant()
            if (det == GF2Polynomial()) {
                nu--
            } else {
                isDetZero = false
            }
        }
        val b : Array<GF2Polynomial> = syndrom.subList(nu, 2*nu).toTypedArray()

        val x = solveSystem(m, b)

        val lambdaPolynomial : Char2GaloisField.TwoVariableGF2Polynomial
        val map : MutableMap<Int, GF2Polynomial> = mutableMapOf()
        map[0] = GF2Polynomial(0)
        for (t in 1..nu) {
            map[t] = x[nu-t]
        }
        lambdaPolynomial = field.TwoVariableGF2Polynomial(map)
        var locatorFound = 0
        var pow = 0
        val positions : MutableList<Int> = mutableListOf()
        while (locatorFound < nu) {
            if (lambdaPolynomial.evalPolynomial(field.raiseToThePower(alpha, pow)) == GF2Polynomial()) {
                positions.add((codeLength - pow) % 15)
                locatorFound++
            }
            pow++
        }
        if (isErrorOne) {
            val errorPolynomial = GF2Polynomial(positions)
            val codePolynomial = encoded.convertToOneVariable() + errorPolynomial
            val decodedInfoPolynomial = codePolynomial.divide(generatorPolynomialTwoVariable.convertToOneVariable()).first
            return field.convertOneToTwoVariable(decodedInfoPolynomial)
        }

        val xMatrix = field.SquarePolynomialMatrix(nu)
        for (row in 0 until nu) {
            for (col in 0 until nu) {
                xMatrix[row, col] = field.raiseToThePower(alpha, positions[col] * (row + 1))
            }
        }

        val s : Array<GF2Polynomial> = syndrom.subList(0, nu).toTypedArray()
        val y = solveSystem(xMatrix, s)

        val mapErrorPolynomial = mutableMapOf<Int, GF2Polynomial>()
        for ((i, pos) in positions.withIndex()) {
            mapErrorPolynomial[pos] = y[i]
        }

        val errPolynomial = field.TwoVariableGF2Polynomial(mapErrorPolynomial)
        val trueCodePolynomial = encoded + errPolynomial
        return trueCodePolynomial / generatorPolynomialTwoVariable
    }

    protected fun byteArrayToPolynomialArray(data : ByteArray, blockLength : Int) : Array<Char2GaloisField.TwoVariableGF2Polynomial> {
        val storage = mutableListOf<Char2GaloisField.TwoVariableGF2Polynomial>()
        var listPolynomials : MutableList<GF2Polynomial>
        var listPowers : MutableList<Int>
        var curPol : GF2Polynomial
        for (i in 0 until data.size * 8 step blockLength * fieldOrderPowerOfTwo) {
            listPolynomials = mutableListOf()
            for (s in 0 until blockLength) {
                listPowers = mutableListOf()
                for (t in 0 until fieldOrderPowerOfTwo) {
                    if ((i + (s * fieldOrderPowerOfTwo + t) < 8 * data.size) && (data[data.size - (i + (s * fieldOrderPowerOfTwo + t)) / 8 - 1].toInt().and((1 shl ((i + (s * fieldOrderPowerOfTwo + t)) % 8))) > 0)) {
                        listPowers.add(t)
                    }
                }
                curPol = GF2Polynomial(listPowers)
                listPolynomials.add(curPol)
            }
            val polynomial = field.TwoVariableGF2Polynomial(listPolynomials)
            storage.add(polynomial)
        }
        return storage.toTypedArray()
    }

    protected fun polynomialArrayToByteArray(polynomials : Array<Char2GaloisField.TwoVariableGF2Polynomial>, blockLength: Int) : ByteArray {
        val storage = BitSet()
        var subPol : GF2Polynomial?
        for ((i, polynomial) in polynomials.withIndex()) {
            for (j in 0 until blockLength) {
                subPol = polynomial.powerMap[j]
                for (power in 0 until fieldOrderPowerOfTwo) {
                    if (subPol != null && subPol.getPowerCoeff(power)) storage.set((i * blockLength + j) * fieldOrderPowerOfTwo + power)
                }
            }
        }
        return storage.toByteArray().reversedArray()
    }

    protected fun byteArrayToPolynomialArrayBinary(data : ByteArray, blockLength : Int) : Array<GF2Polynomial> {
        val storage = mutableListOf<GF2Polynomial>()
        var list : MutableList<Int>
        for (i in 0 until data.size * 8 step blockLength) {
            list = mutableListOf()
            for (s in 0 until blockLength) {
                if ((i + s < 8 * data.size) && (data[data.size - (i + s) / 8 - 1].toInt().and((1 shl ((i + s) % 8))) > 0)) {
                    list.add(s)
                }
            }
            val polynomial = GF2Polynomial(list)
            storage.add(polynomial)
        }
        return storage.toTypedArray()
    }

    protected fun polynomialArrayToByteArrayBinary(polynomials : Array<GF2Polynomial>, blockLength: Int) : ByteArray {
        val storage = BitSet()
        for ((i, polynomial) in polynomials.withIndex()) {
            for (power in 0 until blockLength) {
                if (polynomial.getPowerCoeff(power)) storage.set(i * blockLength + power)
            }
        }
        return storage.toByteArray().reversedArray()
    }

    private fun solveSystem(matrixA : Char2GaloisField.SquarePolynomialMatrix, b : Array<GF2Polynomial>) : Array<GF2Polynomial> {
        if (matrixA.size == b.size) {
            val size = b.size
            for (p in 0 until size) {
                var max = p
                for (row in p+1 until size) {
                    if (field.getPrimitiveDegree(matrixA[row, p]) ?: -1 > field.getPrimitiveDegree(matrixA[max, p]) ?: -1) {
                        max = row
                    }
                }
                var temp : GF2Polynomial
                for (t in 0 until size) {
                    temp = matrixA[p, t]
                    matrixA[p, t] = matrixA[max, t]
                    matrixA[max, t] = temp
                }
                val t = b[p]
                b[p] = b[max]
                b[max] = t

                if (field.getPrimitiveDegree(matrixA[p, p]) == null) {
                    throw CodeException("Bad decoding")
                }

                for (row in p+1 until size) {
                    val mltplr = field.multiplyPolynomials(matrixA[row, p], field.raiseToThePower(matrixA[p, p], -1))
                    b[row] = field.sumPolynomials(b[row], field.multiplyPolynomials(mltplr, b[p]))
                    for (j in p until size) {
                        matrixA[row, j] = field.sumPolynomials(matrixA[row, j], field.multiplyPolynomials(mltplr, matrixA[p, j]))
                    }
                }
            }

            val x : Array<GF2Polynomial> = Array(size) { GF2Polynomial() }
            for (row in size-1 downTo 0) {
                var sum = GF2Polynomial()
                for (j in row+1 until size) {
                    sum = field.sumPolynomials(sum, field.multiplyPolynomials(matrixA[row, j], x[j]))
                }
                x[row] = field.multiplyPolynomials(field.sumPolynomials(b[row], sum), field.raiseToThePower(matrixA[row, row], -1))
            }
            return x
        }
        throw MatrixException("Bad sizes")
    }

}