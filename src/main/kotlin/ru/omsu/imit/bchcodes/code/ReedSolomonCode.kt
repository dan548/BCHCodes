package ru.omsu.imit.bchcodes.code

class ReedSolomonCode(m : Int, t : Int) : BchCode(m, t, CodeType.REED_SOLOMON) {

    override fun encode(message: ByteArray): ByteArray {
        return polynomialArrayToByteArray(byteArrayToPolynomialArray(message, infoLength)
            .map { p -> p * generatorPolynomialTwoVariable }.toTypedArray(), codeLength)
    }

    override fun decode(codeword : ByteArray) : ByteArray {
        val polynomials = byteArrayToPolynomialArray(codeword, codeLength)
        return polynomialArrayToByteArray(polynomials.map { decodePGZ(it) }.toTypedArray(), infoLength)
    }
}