package ru.hse.cppr.application

import picocli.CommandLine
import java.io.PrintWriter


fun main(args: Array<String>): Unit {
    val cli = CommandLine(ITCommandLineApplication())
    cli.setOut(PrintWriter(System.out))
    cli.setErr(PrintWriter(System.err))
    cli.execute(* args)
}