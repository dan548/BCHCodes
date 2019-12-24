package ru.omsu.imit.gsdecoder

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int

class Command : CliktCommand() {
    val k: Int by option(help="Message length").int().prompt("Message length")
    val n: Int by option(help="Block length").int().prompt("Block length")
    val t: Int by option(help="Number of erroneous symbols the code can detect").int().prompt("Erroneous symbol number")

    override fun run() {
        
    }
}

fun main(args : Array<String>) = Command().main(args)