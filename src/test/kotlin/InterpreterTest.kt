package org.example

import kotlin.test.*

class InterpreterTest {

    @BeforeTest
    fun setup() {
        hadError = false
        hadRuntimeError = false
    }

    @Test
    fun testEvaluateExpression() {
        val tokens = Scanner("5 + 3 * 2;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = Interpreter().evaluate(statement.expression)

        assertEquals(11.0, result)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testInvalidSubtraction() {
        val tokens = Scanner("\"Hello\" - 5;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val error = assertFailsWith<RuntimeError> {
            val interpreter = Interpreter()
            interpreter.evaluate(statement.expression)
        }

        assertFalse(hadError)

        assertEquals("Operands must be numbers.", error.message)
    }

    @Test
    fun testRuntimeErrorReporting() {
        val tokens = Scanner("\"Hello\" - 5;").scanTokens()
        val expression = Parser(tokens).parse()

        assertNotNull(expression)

        val interpreter = Interpreter()
        interpreter.interpret(expression)

        assertTrue(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testStringConcatenation() {
        val tokens = Scanner("\"Hello\" + \" World\";").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = Interpreter().evaluate(statement.expression)

        assertFalse(hadError)
        assertFalse(hadRuntimeError)

        assertEquals("Hello World", result)
    }

    @Test
    fun testEquality() {
        val tokens = Scanner("5 == 5;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = Interpreter().evaluate(statement.expression)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)

        assertEquals(true, result)
    }

    @Test
    fun testTruthiness() {
        val tokens = Scanner("!nil;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = Interpreter().evaluate(statement.expression)

        assertFalse(hadError)
        assertFalse(hadRuntimeError)

        assertEquals(true, result)
    }

    @Test
    fun testGrouping() {
        val tokens = Scanner("(5 + 3) * 2;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = Interpreter().evaluate(statement.expression)

        assertFalse(hadError)
        assertFalse(hadRuntimeError)

        assertEquals(16.0, result)
    }

    @Test
    fun testInvalidAddition() {
        val tokens = Scanner("\"Hello\" + 5;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val error = assertFailsWith<RuntimeError> {
            val interpreter = Interpreter()
            interpreter.evaluate(statement.expression)
        }

        assertFalse(hadRuntimeError)

        assertEquals("Operands must be two numbers or two strings.", error.message)
    }
}