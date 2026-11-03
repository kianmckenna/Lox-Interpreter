package org.example

class Scanner(private val source: String) {
    private val tokens = mutableListOf<Token>()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun match(expected: Char): Boolean {
        if (current >= source.length) return false
        if (source[current] != expected) return false

        current++
        return true
    }

    private fun scanToken() {
        val c = advance()

        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            ' ', '\t', '\r' -> {}
            '\n'-> line++
            '"' -> string()

            '/' -> {
                if (match('/')) {
                    // Ignore everything until the next newline.
                    while (!isAtEnd() && source[current] != '\n') {
                        advance()
                    }
                } else {
                    addToken(TokenType.SLASH)
                }
            }

            '!' -> addToken(
                if (match('=')) TokenType.BANG_EQUAL
                else TokenType.BANG
            )

            '=' -> addToken(
                if (match('=')) TokenType.EQUAL_EQUAL
                else TokenType.EQUAL
            )

            '<' -> addToken(
                if (match('=')) TokenType.LESS_EQUAL
                else TokenType.LESS
            )

            '>' -> addToken(
                if (match('=')) TokenType.GREATER_EQUAL
                else TokenType.GREATER
            )

            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun advance() : Char {
        return source[current++]
    }

    private fun addToken(type: TokenType, literal: Any? = null) {
        val lexeme = source.substring(start, current)

        tokens.add(Token(type, lexeme, literal, line))
    }

    private fun isAlpha(c: Char): Boolean {
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun isDigit (c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun identifier() {
        while(!isAtEnd() && isAlphaNumeric(source[current])) {
            advance()
        }

        val text = source.substring(start, current)
        val type = keywords[text] ?: TokenType.IDENTIFIER

        addToken(type)
    }

    private val keywords = mapOf(
        "and" to TokenType.AND,
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "for" to TokenType.FOR,
        "fun" to TokenType.FUN,
        "if" to TokenType.IF,
        "nil" to TokenType.NIL,
        "or" to TokenType.OR,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "while" to TokenType.WHILE
    )

    private fun number() {
        while (!isAtEnd() && isDigit(source[current])) {
            advance()
        }

        if (!isAtEnd() &&
            source[current] == '.' &&
            current + 1 < source.length &&
            isDigit(source[current + 1])) {

            advance()

            while (!isAtEnd() && isDigit(source[current])) {
                advance()
            }
        }

        val value = source.substring(start, current).toDouble()

        addToken(TokenType.NUMBER, value)
    }

    private fun string() {
        while (!isAtEnd() && source[current] != '"') {
            if (source[current] == '\n') {
                line++
            }
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }
}