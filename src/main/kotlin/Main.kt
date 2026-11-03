package org.example
import java.io.File

var hadError = false
var hadRuntimeError = false

fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error$where: $message")
    hadError = true
}

fun main(args: Array<String>) {
    when (args.size) {
        0 -> runPrompt()
        1 -> runFile(args[0])
        else -> println("Usage: klox [script]")
    }
}

fun runFile(path: String) {
    val source = File(path).readText()
    run(source)
    if (hadError) System.exit(65)
    if (hadRuntimeError) System.exit(70)
}

fun runPrompt() {
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: break
        run(line)
        hadError = false
        hadRuntimeError = false
    }
}

private val interpreter = Interpreter()

fun run(source: String) {
    val tokens = Scanner(source).scanTokens()
    if (hadError) return

    val statements = Parser(tokens).parse()
    if (hadError) return

    val resolver = Resolver(interpreter)
    resolver.resolve(statements)

    // Stop if there was a resolution error
    if (hadError) return

    interpreter.interpret(statements)
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun error(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}

fun runtimeError(error: RuntimeError) {
    System.err.println(error.message)
    System.err.println("[line ${error.token.line}]")
    hadRuntimeError = true
}
