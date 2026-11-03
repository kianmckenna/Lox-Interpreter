package org.example

import kotlin.test.*

class ClassTest {

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
    fun testBasicClassInstantiation() {
        val output = runProgram("""
            class Bagel {}

            var bagel = Bagel();
            print bagel;
        """.trimIndent())

        assertEquals("Bagel instance", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testInstanceFields() {
        val output = runProgram("""
            class Bagel {}

            var bagel = Bagel();
            bagel.flavor = "Blueberry";
            bagel.price = 3.50;

            print bagel.flavor;
            print bagel.price;
        """.trimIndent())

        assertEquals("Blueberry\n3.5", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testUndefinedProperty() {
        val output = runProgram("""
            class Bagel {}

            var bagel = Bagel();
            print bagel.size;
        """.trimIndent())

        assertTrue(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testMethods() {
        val output = runProgram("""
            class Greeter {
                greet() {
                    print "Hello, world!";
                }
            }

            var greeter = Greeter();
            greeter.greet();
        """.trimIndent())

        assertEquals("Hello, world!", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testThisKeyword() {
        val output = runProgram("""
            class Person {
                sayName() {
                    print this.name;
                }
            }

            var person = Person();
            person.name = "Alex";
            person.sayName();
        """.trimIndent())

        assertEquals("Alex", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testInitializer() {
        val output = runProgram("""
            class Person {
                init(name) {
                    this.name = name;
                }

                sayName() {
                    print this.name;
                }
            }

            var person = Person("Alex");
            person.sayName();
        """.trimIndent())

        assertEquals("Alex", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testInitializerArity() {
        val output = runProgram("""
            class Person {
                init(name) {
                    this.name = name;
                }
            }

            var person = Person();
        """.trimIndent())

        assertTrue(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testBoundMethods() {
        val output = runProgram("""
            class Person {
                init(name) {
                    this.name = name;
                }

                sayName() {
                    print this.name;
                }
            }

            var person = Person("Alex");
            var method = person.sayName;

            method();
        """.trimIndent())

        assertEquals("Alex", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testReturnFromInitializer() {
        val tokens = Scanner("""
        class Person {
            init() {
                return 42;
            }
        }
    """.trimIndent()).scanTokens()

        val statements = Parser(tokens).parse()
        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testThisOutsideClass() {
        val tokens = Scanner("print this;").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testClassFieldsIndependent() {
        val output = runProgram("""
            class Counter {}

            var first = Counter();
            var second = Counter();

            first.count = 10;
            second.count = 20;

            first.count = first.count + 1;

            print first.count;
            print second.count;
        """.trimIndent())

        assertEquals("11\n20", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testFieldShadowsMethod() {
        val output = runProgram("""
            class Greeter {
                greet() {
                    print "Hello from method!";
                }
            }

            var greeter = Greeter();
            greeter.greet = "Hello from field!";

            print greeter.greet;
        """.trimIndent())

        assertEquals("Hello from field!", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }
}