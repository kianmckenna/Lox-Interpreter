package org.example

import kotlin.test.*

class StatementTest {
    private fun runProgram(source: String): String {
        val tokens = Scanner(source).scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError, "Scanning or parsing failed.")

        val output = java.io.ByteArrayOutputStream()
        val originalOut = System.out

        try {
            System.setOut(java.io.PrintStream(output))

            val interpreter = Interpreter()
            val resolver = Resolver(interpreter)

            resolver.resolve(statements)
            assertFalse(hadError, "Resolution failed.")

            interpreter.interpret(statements)
        } finally {
            System.setOut(originalOut)
        }

        return output.toString()
            .replace("\r\n", "\n")
            .trim()
    }

    @BeforeTest
    fun setup() {
        hadError = false
        hadRuntimeError = false
    }

    @Test
    fun testVariableDeclaration() {
        val output = runProgram(
            """
        var x = 10;
        print x;
        var y = 20;
        print y;
        """.trimIndent()
        )

        assertEquals("10\n20", output)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testBlockShadowing() {
        val output = runProgram(
            """
        var x = "global";

        {
            var x = "local";
            print x;
        }

        print x;
        """.trimIndent()
        )

        assertEquals("local\nglobal", output)
        assertFalse(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testEnclosingAssignment() {
        val output = runProgram(
            """
            var x = 10;

            {
                x = 20;
            }
        
            print x;
            """.trimIndent()
        )

        assertEquals("20", output)
        assertFalse(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testUndefinedVariable() {
        val output = runProgram(
            """
            print missing;
            """.trimIndent()
        )

        assertEquals("", output)
        assertTrue(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testInvalidAssignmentTarget() {
        val tokens = Scanner("5 = 3;").scanTokens()

        assertFalse(hadError)

        val statements = Parser(tokens).parse()

        assertTrue(hadError)
        assertFalse(hadRuntimeError)

        assertEquals(1, statements.size)
        assertIs<Stmt.Expression>(statements[0])
    }

    @Test
    fun testUninitializedVariable() {
        val output = runProgram(
            """
            var x;
            print x;
            """.trimIndent()
        )

        assertEquals("nil", output)
        assertFalse(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testAssignmentExpression() {
        val output = runProgram(
            """
            var x;
            print x = 20;
            """.trimIndent()
        )

        assertEquals("20", output)
        assertFalse(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testExpressionStatement() {
        val output = runProgram(
            """
            5 + 3;
            print 10;
            """.trimIndent()
        )

        assertEquals("10", output)
        assertFalse(hadRuntimeError)
        assertFalse(hadError)
    }
}