package org.example

import kotlin.test.*

class ControlFlowTest {
    @BeforeTest
    fun setup() {
        hadError = false
        hadRuntimeError = false
    }

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

    @Test
    fun testIfTrue() {
        val output = runProgram(
            """
        if (true) print 1;
        """.trimIndent()
        )

        assertEquals("1", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testIfFalse() {
        val output = runProgram(
            """
        if (false) print 1;
        """.trimIndent()
        )

        assertEquals("", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testIfElse() {
        val output = runProgram(
            """
        if (false) print 1;
        else print 2;
        """.trimIndent()
        )

        assertEquals("2", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testShortCircuit() {
        val output = runProgram(
            """
            var x = 0;
            true or (x = 5);
            print x;
            """.trimIndent()
        )

        assertEquals("0", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testLogicalAndShortCircuit() {
        val output = runProgram(
            """
            var x = 0;
            false and (x = 5);
            print x;
            """.trimIndent()
        )

        assertEquals("0", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testWhileLoop() {
        val output = runProgram(
            """
            var i = 0;

            while (i < 3) {
                print i;
                i = i + 1;
            }
            """.trimIndent()
        )

        assertEquals("0\n1\n2", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testForLoop() {
        val output = runProgram(
            """
            for (var i = 0; i < 3; i = i + 1) {
                print i;
            }
            """.trimIndent()
        )

        assertEquals("0\n1\n2", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testForLoopScope() {
        val output = runProgram(
            """
            for (var i = 0; i < 3; i = i + 1) {
                print i;
            }
            print i;
            """.trimIndent()
        )

        assertFalse(hadError)
        assertTrue(hadRuntimeError)
    }

    @Test
    fun testForWithoutInitializer() {
        val output = runProgram(
            """
            var i = 0;

            for (; i < 3; i = i + 1) {
                print i;
            }

            print i;
            """.trimIndent()
        )

        assertEquals("0\n1\n2\n3", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testDanglingElse() {
        val output = runProgram(
            """
            if (true)
                if (false)
                    print 1;
                else
                    print 2;
            """.trimIndent()
        )

        assertEquals("2", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testLogicalValues() {
        val output = runProgram(
            """
            print nil or "fallback";
            print "hello" or "fallback";
            print 0 and "something";
            """.trimIndent()
        )

        assertEquals("fallback\nhello\nsomething", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }
}