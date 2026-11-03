package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class AstPrinterTest {
    @Test
    fun testNestedExpression() {
        val left = Expr.Unary(
            Token(TokenType.MINUS, "-", null, 1),
            Expr.Literal(123.0)
        )

        val right = Expr.Grouping(
            Expr.Literal(45.67)
        )

        val expression = Expr.Binary(
            left,
            Token(TokenType.STAR, "*", null, 1),
            right
        )

        val result = AstPrinter().print(expression)

        assertEquals(
            "(* (- 123.0) (group 45.67))",
            result
        )
    }

    @Test
    fun testLiteral() {
        val left = Expr.Literal(42.0)

        val result = AstPrinter().print(left)

        assertEquals("42.0", result)
    }

    @Test
    fun testNil() {
        val left = Expr.Literal(null)

        val result = AstPrinter().print(left)

        assertEquals("nil", result)
    }

    @Test
    fun testBinaryPrecedence() {
        val left = Expr.Literal(1.0)

        val left1 = Expr.Literal(2.0)

        val right = Expr.Literal(3.0)

        val right1 = Expr.Binary(
            left1,
            Token(TokenType.STAR, "*", null, 1),
            right
        )

        val final = Expr.Binary(
            left,
            Token(TokenType.PLUS, "+", null, 1),
            right1
        )

        val result = AstPrinter().print(final)

        assertEquals("(+ 1.0 (* 2.0 3.0))", result)
    }

    @Test
    fun testGrouping() {
        val left = Expr.Literal(1.0)

        val right = Expr.Literal(2.0)

        val first = Expr.Binary(
            left,
            Token(TokenType.PLUS, "+", null, 1),
            right
        )

        val group = Expr.Grouping(
            first
        )

        val last = Expr.Literal(3.0)

        val final = Expr.Binary(
            group,
            Token(TokenType.STAR, "*", null, 1),
            last
        )

        val result = AstPrinter().print(final)

        assertEquals("(* (group (+ 1.0 2.0)) 3.0)", result)
    }
}