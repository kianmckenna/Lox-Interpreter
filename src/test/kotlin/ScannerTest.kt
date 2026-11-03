package org.example

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class ScannerTest {

    @BeforeTest
    fun setup() {
        hadError = false
    }

    @Test
    fun testNumber() {
        val scanner = Scanner("123")
        val tokens = scanner.scanTokens()

        assertEquals(TokenType.NUMBER, tokens[0].type)
        assertEquals("123", tokens[0].lexeme)
        assertEquals(123.0, tokens[0].literal)
        assertEquals(TokenType.EOF, tokens[1].type)
    }

    @Test
    fun testKeyword() {
        val scanner = Scanner("var variety")
        val tokens = scanner.scanTokens()

        assertEquals(3, tokens.size)

        assertEquals(TokenType.VAR, tokens[0].type)
        assertEquals("var", tokens[0].lexeme)
        assertNull(tokens[0].literal)

        assertEquals(TokenType.IDENTIFIER, tokens[1].type)
        assertEquals("variety", tokens[1].lexeme)
        assertNull(tokens[1].literal)

        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun testUnterminatedString() {
        val scanner = Scanner("\"Hello")
        scanner.scanTokens()

        assertTrue(hadError)
    }

    @Test
    fun testValidString() {
        val scanner = Scanner("\"Hello\"")
        val tokens = scanner.scanTokens()

        assertFalse(hadError)
        assertEquals(TokenType.STRING, tokens[0].type)
        assertEquals("Hello", tokens[0].literal)
    }

    @Test
    fun testMultilineString() {
        val scanner = Scanner("\"Hello\nWorld\"")
        val tokens = scanner.scanTokens()

        assertEquals(2, tokens.size)

        assertEquals(TokenType.STRING, tokens[0].type)
        assertEquals("\"Hello\nWorld\"", tokens[0].lexeme)
        assertEquals("Hello\nWorld", tokens[0].literal)
        assertEquals(2, tokens[0].line)
        assertEquals(TokenType.EOF, tokens[1].type)
        assertEquals(2, tokens[1].line)

        assertFalse(hadError)
    }

    @Test
    fun testComment() {
        val scanner = Scanner("// @#$%^&*! This is a comment\n123")
        val tokens = scanner.scanTokens()

        // only 2 tokens, number and eof
        assertEquals(2, tokens.size)
        assertEquals(TokenType.NUMBER, tokens[0].type)
        assertEquals("123", tokens[0].lexeme)
        assertEquals(123.0, tokens[0].literal)
        assertEquals(2, tokens[0].line)
        assertEquals(TokenType.EOF, tokens[1].type)

        assertFalse(hadError)
    }

    @Test
    fun testCommentAtEOF() {
        val scanner = Scanner("// This comment has no newline")
        val tokens = scanner.scanTokens()

        assertEquals(1, tokens.size)
        assertEquals(TokenType.EOF, tokens[0].type)
        assertFalse(hadError)
    }

    @Test
    fun testOperators() {
        val scanner = Scanner("! != = == < <= > >=")
        val tokens = scanner.scanTokens()

        val expected = listOf(
            TokenType.BANG,
            TokenType.BANG_EQUAL,
            TokenType.EQUAL,
            TokenType.EQUAL_EQUAL,
            TokenType.LESS,
            TokenType.LESS_EQUAL,
            TokenType.GREATER,
            TokenType.GREATER_EQUAL,
            TokenType.EOF
        )

        val actual = tokens.map { it.type }

        assertEquals(expected, actual)
        assertFalse(hadError)
    }

    @Test
    fun testInvalidCharacters() {
        val scanner = Scanner("123 @ 456 @ 789")
        val tokens = scanner.scanTokens()

        assertTrue(hadError)
        assertEquals(4, tokens.size)

        assertEquals(TokenType.NUMBER, tokens[0].type)
        assertEquals(123.0, tokens[0].literal)

        assertEquals(TokenType.NUMBER, tokens[1].type)
        assertEquals(456.0, tokens[1].literal)

        assertEquals(TokenType.NUMBER, tokens[2].type)
        assertEquals(789.0, tokens[2].literal)
    }

    @Test
    fun testEmptyInput() {
        val scanner = Scanner("")
        val tokens = scanner.scanTokens()

        assertFalse(hadError)
        assertEquals(1, tokens[0].line)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.EOF, tokens[0].type)
    }

    @Test
    fun testTrailingDecimal() {
        val scanner = Scanner("123.")
        val tokens = scanner.scanTokens()

        assertFalse(hadError)
        assertEquals(3, tokens.size)

        assertEquals(TokenType.NUMBER, tokens[0].type)
        assertEquals(123.0, tokens[0].literal)
        assertEquals(TokenType.DOT, tokens[1].type)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun testIdentifier() {
        val scanner = Scanner("counter123")
        val tokens = scanner.scanTokens()

        assertFalse(hadError)
        assertEquals(2, tokens.size)
        assertEquals(TokenType.IDENTIFIER, tokens[0].type)
        assertEquals("counter123", tokens[0].lexeme)
        assertEquals(TokenType.EOF, tokens[1].type)
    }

    @Test
    fun testMaximalMunch() {
        val scanner = Scanner("!==")
        val tokens = scanner.scanTokens()

        assertFalse(hadError)
        assertEquals(3, tokens.size)
        assertEquals(TokenType.BANG_EQUAL, tokens[0].type)
        assertEquals(TokenType.EQUAL, tokens[1].type)
        assertEquals(TokenType.EOF, tokens[2].type)
    }

    @Test
    fun testCompleteProgram() {
        val source = """
        // Create two variables
        var x = 10;
        var y = 20.5;

        if (x < y) {
            print "Hello";
        }
    """.trimIndent()

        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()

        assertFalse(hadError)

        val expected = listOf(
            TokenType.VAR,
            TokenType.IDENTIFIER,
            TokenType.EQUAL,
            TokenType.NUMBER,
            TokenType.SEMICOLON,
            TokenType.VAR,
            TokenType.IDENTIFIER,
            TokenType.EQUAL,
            TokenType.NUMBER,
            TokenType.SEMICOLON,
            TokenType.IF,
            TokenType.LEFT_PAREN,
            TokenType.IDENTIFIER,
            TokenType.LESS,
            TokenType.IDENTIFIER,
            TokenType.RIGHT_PAREN,
            TokenType.LEFT_BRACE,
            TokenType.PRINT,
            TokenType.STRING,
            TokenType.SEMICOLON,
            TokenType.RIGHT_BRACE,
            TokenType.EOF
        )

        assertEquals(10.0, tokens[3].literal)
        assertEquals(20.5, tokens[8].literal)

        assertEquals("Hello", tokens[18].literal)

        assertEquals(2, tokens[0].line)
        assertEquals(3, tokens[5].line)
        assertEquals(6, tokens[17].line)

        val actual = tokens.map { it.type }
        assertEquals(expected, actual)
    }

    @Test
    fun testSingleQuoteUnterminated() {
        val scanner = Scanner("\"")
        val tokens = scanner.scanTokens()

        assertTrue(hadError)
        assertEquals(1, tokens.size)
        assertEquals(TokenType.EOF, tokens[0].type)
    }

}