package org.example

import kotlin.test.*

class FunctionTest {
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
    fun testBasicFunctionCall() {
        val output = runProgram(
            """
        fun greet() {
            print "Hello!";
        }

        greet();
        """.trimIndent()
        )

        assertEquals("Hello!", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testFunctionParameters() {
        val output = runProgram(
            """
            fun add(a, b) {
                print a + b;
            }
            
            add(5, 10);
            """.trimIndent()
        )

        assertEquals("15", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testFunctionReturn() {
        val output = runProgram(
            """
                fun double(x) {
                    return x * 2;
                }

                print double(7);
            """.trimIndent()
        )

        assertEquals("14", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testEarlyReturn() {
        val output = runProgram(
            """
                fun example() {
                    print "Before";
                    return 42;
                    print "After";
                }

                print example();
            """.trimIndent()
        )

        assertEquals("Before\n42", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testImplicitNilReturn() {
        val output = runProgram(
            """
               fun greet() {
                    print "Hello!";
                }
                
                print greet(); 
            """.trimIndent()
        )

        assertEquals("Hello!\nnil", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testRecursiveFunction() {
        val output = runProgram(
            """
                fun factorial(n) {
                    if (n <= 1) return 1;
                    return n * factorial(n - 1);
                }

                print factorial(5);
            """.trimIndent()
        )

        assertEquals("120", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testClosure() {
        val output = runProgram("""
            fun makeCounter() {
                var i = 0;

                fun count() {
                    i = i + 1;
                    print i;
                }

                return count;
            }

            var counter = makeCounter();
            counter();
            counter();
            counter();
        """.trimIndent())

        assertEquals("1\n2\n3", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testIncorrectArity() {
        val output = runProgram("""
            fun foo(a) {
                print "dude " + a;
            }
            
            foo("what", "up");
        """.trimIndent())

        assertEquals("", output)
        assertFalse(hadError)
        assertTrue(hadRuntimeError)
    }

    @Test
    fun testNonCallableValue() {
        val output = runProgram("""
        var x = 10;
        x();
    """.trimIndent())

        assertEquals("", output)
        assertFalse(hadError)
        assertTrue(hadRuntimeError)
    }
}