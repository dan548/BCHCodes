package ru.omsu.imit.bchcodes

import ru.omsu.imit.bchcodes.code.BinaryBchCode
import ru.omsu.imit.bchcodes.errorgen.ErrorGenerator

fun main() {
    val str = "AAaAaaaaaaaAaAAaAaAAA ass we can"
    val probability = 0.03
    val code = BinaryBchCode(31, 11)
    val encoded = code.encode(str.toByteArray(Charsets.UTF_8))
    val withError = ErrorGenerator.generateErrors(encoded, probability)
    val decodedAndCharset = code.decode(withError).toString(Charsets.UTF_8)
    println("probability = $probability")
    println(str)
    print(decodedAndCharset)
}