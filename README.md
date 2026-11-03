# Lox Interpreter

**Student:** Kian McKenna  
**Programming Language:** Kotlin  
**Development Environment:** IntelliJ IDEA  
**Java Version:** JDK 21  
**Build System:** Gradle

## 1. Project Summary

This project implements the Lox interpreter from *Crafting Interpreters* by Robert Nystrom using Kotlin.

test-output.txt contains my unit testing results

**Chapter 4:** Implemented a scanner that converts Lox source code into tokens.

**Chapter 5:** Implemented an abstract syntax tree (AST) using Kotlin sealed and data classes for binary, unary, grouping, and literal expressions. Implemented a recursive AST printer using Kotlin's `when` expression instead of Java's Visitor pattern.

**Chapter 6:** Implemented a recursive descent parser that converts scanned tokens into abstract syntax trees, handling operator precedence, associativity, grouping, and syntax errors.

**Chapter 7:** Implemented a tree-walk interpreter that recursively evaluates abstract syntax trees. Supports arithmetic, comparisons, equality, string concatenation, unary operators, grouping, and Lox truthiness rules. Added runtime type checking, error reporting, and value formatting.

**Chapter 8:** Implemented statement execution, variable declarations, variable lookup and assignment, and block scoping. Added an environment system supporting nested scopes, variable shadowing, and assignments to enclosing variables. Updated the parser to handle statements, declarations, and syntax error recovery.

**Chapter 9:** Implemented control flow using conditional statements, while loops, and for loops. Added logical and and or operators with short-circuit evaluation. Implemented for loops by transforming them into existing while and block statements.

**Chapter 10:** Implemented user-defined functions, function calls, parameter binding, return statements, and recursion. Added a LoxCallable interface to support native and user-defined functions. Implemented closures that preserve access to enclosing environments after a function returns.

**Chapter 11:** Implemented a resolver to perform static analysis of variable references before program execution.

**Chapter 12:** Implemented object oriented programming functionality with class declarations, object instantiation, instance fields, methods, initializers, and the this keyword.

**Chapter 13:** Implemented inheritance and the super keyword.

**Testing:** 95 automated unit tests covering the scanner, AST printer, parser, interpreter, statements, functions, resolver, classes, inheritance and control flow. All tests passed, as documented in `test-output.txt`.

## 2. Project Structure

```text
LoxInterpreter/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       ├── Main.kt
│   │       ├── Scanner.kt
│   │       ├── Token.kt
│   │       ├── TokenType.kt
│   │       ├── Expr.kt
│   │       ├── AstPrinter.kt
│   │       ├── RuntimeError.kt
│   │       ├── Interpreter.kt
│   │       ├── Environment.kt
│   │       ├── LoxCallable.kt
│   │       ├── LoxFunction.kt
│   │       ├── LoxInstance.kt
│   │       ├── Return.kt
│   │       ├── Resolver.kt
│   │       ├── Stmt.kt
│   │       └── Parser.kt
│   └── test/
│       └── kotlin/
│           ├── ScannerTest.kt
│           ├── AstPrinterTest.kt
│           ├── ClassTest.kt
│           ├── InheritanceTest.kt
│           ├── ResolverTest.kt
│           ├── ControlFlowTest.kt
│           ├── InterpreterTest.kt
│           ├── StatementTest.kt
│           ├── FunctionTest.kt
│           └── ParserTest.kt
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── README.md
└── test-output.txt
```

## 3. Build Instructions

### Build

**Requirements:** JDK 21 and PowerShell (Windows) or a compatible terminal. The Gradle wrapper is provided, so no separate Gradle or IntelliJ installation is required. Internet access may be necessary for the initial Gradle dependency download.

.\gradlew.bat classes

## 4. Running the Interpreter

All commands should be executed from the project root directory.

### Run in Interactive Mode

.\gradlew.bat run --console=plain

### Run a Lox Source File

.\gradlew.bat run --args="test.lox"

Replace `test.lox` with the path to the desired source file.

### Run Tests

.\gradlew.bat test

### Generate Test Output

.\gradlew.bat clean test --console=plain 2>&1 | Out-File -FilePath test-output.txt -Encoding utf8

The project can be compiled, tested, and executed without IntelliJ IDEA.

For macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`.

The interpreter supports interactive input and reading Lox programs from files.


## 5. Current Limitations

None
