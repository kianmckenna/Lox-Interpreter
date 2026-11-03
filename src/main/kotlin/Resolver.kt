package org.example

class Resolver(private val interpreter: Interpreter) {
    private val scopes = mutableListOf<MutableMap<String, Boolean>>()

    private enum class FunctionType {
        NONE,
        FUNCTION,
        INITIALIZER,
        METHOD,
    }

    private enum class ClassType {
        NONE,
        CLASS,
        SUBCLASS,
    }

    private var currentFunction = FunctionType.NONE

    private var currentClass = ClassType.NONE

    private fun beginScope() {
        scopes.add(mutableMapOf())
    }

    private fun endScope() {
        scopes.removeAt(scopes.lastIndex)
    }

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun resolve(statement: Stmt) {
        when (statement) {
            is Stmt.Block -> {
                beginScope()
                resolve(statement.statements)
                endScope()
            }

            is Stmt.Class -> {
                val enclosingClass = currentClass
                currentClass = ClassType.CLASS

                declare(statement.name)
                define(statement.name)

                if (statement.superclass != null && statement.name.lexeme.equals(statement.superclass.name.lexeme)) {
                    org.example.error(statement.superclass.name, "A class can't inherit from itself.")
                }

                if (statement.superclass != null) {
                    currentClass = ClassType.SUBCLASS
                    resolve(statement.superclass)
                }

                if (statement.superclass != null) {
                    beginScope()
                    scopes.last()["super"] = true
                }

                beginScope()
                scopes.last()["this"] = true

                for (method in statement.methods) {
                    var declaration = FunctionType.METHOD
                    if (method.name.lexeme.equals("init")) {
                        declaration = FunctionType.INITIALIZER
                    }
                    resolveFunction(method, declaration)
                }

                endScope()

                if (statement.superclass != null) endScope()

                currentClass = enclosingClass
            }

            is Stmt.Expression -> {
                resolve(statement.expression)
            }

            is Stmt.Var -> {
                declare(statement.name)
                if (statement.initializer != null) {
                    resolve(statement.initializer)
                }
                define(statement.name)
            }

            is Stmt.While -> {
                resolve(statement.condition)
                resolve(statement.body)
            }

            is Stmt.Function -> {
                declare(statement.name)
                define(statement.name)

                resolveFunction(statement, FunctionType.FUNCTION)
            }

            is Stmt.If -> {
                resolve(statement.condition)
                resolve(statement.thenBranch)
                if (statement.elseBranch != null) {
                    resolve(statement.elseBranch)
                }
            }

            is Stmt.Print -> {
                resolve(statement.expression)
            }

            is Stmt.Return -> {
                if (currentFunction == FunctionType.NONE) {
                    org.example.error(statement.keyword, "Can't return from top-level code.")
                }

                if (statement.value != null) {
                    if (currentFunction == FunctionType.INITIALIZER) {
                        org.example.error(statement.keyword, "Can't return a value from an initializer.")
                    }
                    resolve(statement.value)
                }
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type
        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction
    }

    private fun resolve(expr: Expr) {
        when (expr) {
            is Expr.Literal -> {
                // Nothing to resolve
            }

            is Expr.Logical -> {
                resolve(expr.left)
                resolve(expr.right)
            }

            is Expr.Set -> {
                resolve(expr.value)
                resolve(expr.obj)
            }

            is Expr.Super -> {
                if (currentClass == ClassType.NONE) {
                    org.example.error(expr.keyword, "Can't use 'super' outside of a class")
                } else if (currentClass != ClassType.SUBCLASS) {
                    org.example.error(expr.keyword, "Can't use 'super' in a class with no superclass.")
                }
                resolveLocal(expr, expr.keyword)
            }

            is Expr.This -> {
                if (currentClass == ClassType.NONE) {
                    org.example.error(expr.keyword, "Can't use 'this' outside of a class.")
                }

                resolveLocal(expr, expr.keyword)
            }

            is Expr.Grouping -> {
                resolve(expr.expression)
            }

            is Expr.Unary -> {
                resolve(expr.right)
            }

            is Expr.Binary -> {
                resolve(expr.left)
                resolve(expr.right)
            }

            is Expr.Call -> {
                resolve(expr.callee)

                for (argument in expr.arguments) {
                    resolve(argument)
                }
            }

            is Expr.Get -> {
                resolve(expr.obj)
            }

            is Expr.Variable -> {
                if (!scopes.isEmpty() && scopes.last()[expr.name.lexeme] == false)  {
                    org.example.error(expr.name, "Can't read local variable in its own initializer.")
                }

                resolveLocal(expr, expr.name)
            }

            is Expr.Assign -> {
                resolve(expr.value)
                resolveLocal(expr, expr.name)
            }
        }
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return

        val scope = scopes.last()
        if (scope.containsKey(name.lexeme)) {
            org.example.error(name, "Already a variable with this name in this scope.")
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.last()[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for (i in scopes.indices.reversed()) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.lastIndex - i)
                return
            }
        }
    }
}