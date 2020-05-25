package ru.omsu.imit.bchcodes.code

import ru.omsu.imit.bchcodes.math.GF2Polynomial

class BinaryBchCode(n : Int, t : Int) : BchCode(n, t, CodeType.BINARY_BCH) {

    val generatorPolynomial : GF2Polynomial = super.generatorPolynomialTwoVariable.convertToOneVariable()

    override fun encode(message : ByteArray) : ByteArray {
        return polynomialArrayToByteArrayBinary(byteArrayToPolynomialArrayBinary(message, infoLength)
            .map { p -> p * generatorPolynomial }.toTypedArray(), codeLength)
    }

    override fun decode(codeword : ByteArray) : ByteArray {
        val polynomials = byteArrayToPolynomialArrayBinary(codeword, codeLength)
        return polynomialArrayToByteArrayBinary(polynomials.map { decodePGZ(field.convertOneToTwoVariable(it)).convertToOneVariable() }.toTypedArray(), infoLength)
    }

}