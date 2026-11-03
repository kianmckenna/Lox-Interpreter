package org.example

import kotlin.test.*

class InheritanceTest {

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
    fun testInheritedMethod() {
        val output = runProgram("""
            class Animal {
                speak() {
                    print "Some sound";
                }
            }

            class Dog < Animal {}

            var dog = Dog();
            dog.speak();
        """.trimIndent())

        assertEquals("Some sound", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testMethodOverride() {
        val output = runProgram("""
            class Animal {
                speak() {
                    print "Some sound";
                }
            }

            class Dog < Animal {
                speak() {
                    print "Woof!";
                }
            }

            var animal = Animal();
            var dog = Dog();

            animal.speak();
            dog.speak();
        """.trimIndent())

        assertEquals("Some sound\nWoof!", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testSuper() {
        val output = runProgram("""
            class Animal {
                speak() {
                    print "Some sound";
                }
            }

            class Dog < Animal {
                speak() {
                    super.speak();
                    print "Woof!";
                }
            }

            var dog = Dog();
            dog.speak();
        """.trimIndent())

        assertEquals("Some sound\nWoof!", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testInheritedInitializer() {
        val output = runProgram("""
            class Animal {
                init(name) {
                    this.name = name;
                }

                speak() {
                    print this.name;
                }
            }

            class Dog < Animal {}

            var dog = Dog("Buddy");
            dog.speak();
        """.trimIndent())

        assertEquals("Buddy", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testInvalidSuperClass() {
        val output = runProgram("""
            var Animal = "not a class";

            class Dog < Animal {}
        """.trimIndent())

        assertTrue(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testSelfInheritance() {
        val tokens = Scanner("class Animal < Animal {}").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testSuperOutsideClass() {
        val tokens = Scanner("super.speak();").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testSuperWithoutSuperclass() {
        val tokens = Scanner("class Animal {\n" +
                "    speak() {\n" +
                "        super.speak();\n" +
                "    }\n" +
                "}").scanTokens()
        val statements = Parser(tokens).parse()

        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)
        resolver.resolve(statements)

        assertTrue(hadError)
        assertFalse(hadRuntimeError)
    }

    @Test
    fun testUndefinedSuperMethod() {
        val tokens = Scanner("""
        class Animal {
            speak() {
                print "Some sound";
            }
        }

        class Dog < Animal {
            move() {
                super.fly();
            }
        }

        Dog().move();
    """.trimIndent()).scanTokens()

        val statements = Parser(tokens).parse()
        assertFalse(hadError)

        val interpreter = Interpreter()
        val resolver = Resolver(interpreter)

        resolver.resolve(statements)
        assertFalse(hadError, "Resolution should succeed.")

        interpreter.interpret(statements)

        assertTrue(hadRuntimeError)
        assertFalse(hadError)
    }

    @Test
    fun testSuperPreservesThis() {
        val output = runProgram("""
            class Animal {
                showName() {
                    print this.name;
                }
            }

            class Dog < Animal {
                init(name) {
                    this.name = name;
                }

                showName() {
                    super.showName();
                }
            }

            var dog = Dog("Buddy");
            dog.showName();
        """.trimIndent())

        assertEquals("Buddy", output)
        assertFalse(hadError)
        assertFalse(hadRuntimeError)
    }
}