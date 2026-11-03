package org.example

import kotlin.test.*

class ResolverTest {
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

            assertFalse(hadError, "Resolver failed.")

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
    fun testLexicalScoping() {
        val output = runProgram("""
            var a = "global";

            {
                fun showA() {
                    print a;
                }

                showA();
                var a = "block";
                showA();
            }
        """.trimIndent())

        assertEquals("global\nglobal", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testShadowedAssignment() {
        val output = runProgram("""
            {
                var x = 10;

                {
                    var x = 50;
                    x = 20;
                    print x;
                }

                print x;
            }
        """.trimIndent())

        assertEquals("20\n10", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testNestedScopeLookup() {
        val output = runProgram("""
            {
                var x = 42;

                {
                    {
                        print x;
                    }
                }
            }
        """.trimIndent())

        assertEquals("42", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testNestedAssignment() {
        val output = runProgram("""
            {
                var x = 10;

                {
                    {
                        x = 20;
                    }
                }

                print x;
            }
        """.trimIndent())

        assertEquals("20", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testSelfInitialization() {
        val tokens = Scanner("{ var x = x; }").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)

        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testDuplicateLocalVariable() {
        val tokens = Scanner("{ var x = 10; var x = 20;}").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)

        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testReturnOutsideFunction() {
        val tokens = Scanner("return 5;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)

        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testGlobalLookup() {
        val output = runProgram("""
            var score = 100;

            fun showScore() {
                print score;
            }

            {
                showScore();
            }
        """.trimIndent())

        assertEquals("100", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testClosureCapture() {
        val output = runProgram("""
            fun outer() {
                var x = 10;

                fun inner() {
                    print x + 5;
                }

                return inner;
            }

            var fn = outer();
            fn();
        """.trimIndent())

        assertEquals("15", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }
}