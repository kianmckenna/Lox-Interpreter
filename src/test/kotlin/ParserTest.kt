package org.example

import kotlin.test.*

class ParserTest {

    @BeforeTest
    fun setup() {
        hadError = false
    }

    @Test
    fun testOperatorPrecedence() {
        val tokens = Scanner("1 + 2 * 3;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = AstPrinter().print(statement.expression)

        assertEquals("(+ 1.0 (* 2.0 3.0))", result)
    }

    @Test
    fun testUnary() {
        val tokens = Scanner("--5;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = AstPrinter().print(statement.expression)

        assertEquals("(- (- 5.0))", result)
        assertFalse(hadError)
    }

    @Test
    fun testLeftAssociativity() {
        val tokens = Scanner("10 - 5 - 2;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = AstPrinter().print(statement.expression)

        assertEquals("(- (- 10.0 5.0) 2.0)", result)
        assertFalse(hadError)
    }

    @Test
    fun testSyntaxErrors() {
        val tokens = Scanner("1 + * 2;").scanTokens()

        assertFalse(hadError)

        val statements = Parser(tokens).parse()

        assertTrue(statements.isEmpty())
        assertTrue(hadError)
    }

    @Test
    fun testMissingClosingParen() {
        val tokens = Scanner("(1 + 2;").scanTokens()

        assertFalse(hadError)

        val statements = Parser(tokens).parse()

        assertTrue(statements.isEmpty())
        assertTrue(hadError)
    }

    @Test
    fun testEmptyInput() {
        val tokens = Scanner("").scanTokens()

        assertEquals(1, tokens.size)
        assertEquals(TokenType.EOF, tokens[0].type)
        assertFalse(hadError)

        val statements = Parser(tokens).parse()

        assertTrue(statements.isEmpty())
        assertFalse(hadError)
    }

    @Test
    fun testEqualityAssociativity() {
        val tokens = Scanner("1 == 2 != 3;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)
        assertEquals(1, statements.size)

        val statement = statements[0]
        assertIs<Stmt.Expression>(statement)

        val result = AstPrinter().print(statement.expression)

        assertEquals("(!= (== 1.0 2.0) 3.0)", result)
        assertFalse(hadError)
    }

    @Test
    fun testErrorRecovery() {
        val tokens = Scanner(
            """
        var x = ;
        print 5;
        """.trimIndent()
        ).scanTokens()

        assertFalse(hadError)

        val statements = Parser(tokens).parse()

        assertTrue(hadError)
        assertEquals(1, statements.size)
        assertIs<Stmt.Print>(statements[0])
    }
}