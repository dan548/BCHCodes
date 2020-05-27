package ru.omsu.imit.bchcodes.errorgen

import kotlin.math.floor

object ErrorGenerator {

    fun generateErrors(input : ByteArray, p : Double) : ByteArray {
        val res = mutableListOf<Byte>()

        for (byte in input) {
            if (Math.random() < p) {
                val badByte = floor(Math.random() * 256 - 128).toByte()
                res.add(badByte)
            } else {
                res.add(byte)
            }
        }

        return res.toByteArray()
    }

}