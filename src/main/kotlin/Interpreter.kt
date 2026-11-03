package org.example

class Interpreter {
    val globals: Environment = Environment()
    private var environment : Environment = globals
    private val locals = java.util.IdentityHashMap<Expr, Int>()

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    init {
        globals.define("clock", object: LoxCallable {
            override fun arity(): Int = 0

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return System.currentTimeMillis() / 1000.0
            }

            override fun toString(): String = "<native fn>"
        })
    }

    internal fun evaluate(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> expr.value

            is Expr.Logical -> {
                val left = evaluate(expr.left)

                if (expr.operator.type == TokenType.OR) {
                    if (isTruthy(left)) return left
                } else {
                    if (!isTruthy(left)) return left
                }

                evaluate(expr.right)
            }

            is Expr.Set -> {
                val obj = evaluate(expr.obj)
                if (!(obj is LoxInstance)) {
                    throw RuntimeError(expr.name, "Only instances have fields.")
                }

                val value = evaluate(expr.value)
                (obj).set(expr.name, value)
                value
            }

            is Expr.Super -> {
                val distance = locals[expr]
                    ?: error("Unresolved super expression.")

                val superclass = environment.getAt(distance, "super") as LoxClass
                val instance = environment.getAt(distance - 1, "this") as LoxInstance

                val method = superclass.findMethod(expr.method.lexeme)
                    ?: throw RuntimeError(
                        expr.method,
                        "Undefined property '${expr.method.lexeme}'."
                    )

                method.bind(instance)
            }

            is Expr.This -> {
                lookUpVariable(expr.keyword, expr)
            }

            is Expr.Grouping -> evaluate(expr.expression)

            is Expr.Unary -> {
                val right = evaluate(expr.right)

                when (expr.operator.type) {
                    TokenType.MINUS -> {
                        checkNumberOperand(expr.operator, right)
                        -(right as Double)
                    }
                    TokenType.BANG -> !isTruthy(right)
                    else -> null
                }
            }

            is Expr.Binary -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)

                when (expr.operator.type) {
                    TokenType.GREATER -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) > (right as Double)
                    }
                    TokenType.GREATER_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) >= (right as Double)
                    }

                    TokenType.LESS -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) < (right as Double)
                    }
                    TokenType.LESS_EQUAL -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) <= (right as Double)
                    }

                    TokenType.BANG_EQUAL -> !isEqual(left, right)
                    TokenType.EQUAL_EQUAL -> isEqual(left, right)

                    TokenType.MINUS -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) - (right as Double)
                    }
                    TokenType.PLUS -> {
                        when {
                            left is Double && right is Double -> left + right
                            left is String && right is String -> left + right
                            else -> throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                        }
                    }

                    TokenType.SLASH -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) / (right as Double)
                    }
                    TokenType.STAR -> {
                        checkNumberOperands(expr.operator, left, right)
                        (left as Double) * (right as Double)
                    }

                    else -> null
                }
            }

            is Expr.Call -> {
                val callee = evaluate(expr.callee)

                val arguments = mutableListOf<Any?>()
                for (argument in expr.arguments) {
                    arguments.add(evaluate(argument))
                }

                if (callee !is LoxCallable) {
                    throw RuntimeError(expr.paren, "can only call functions and classes.")
                }

                val function = callee
                if (arguments.size != function.arity()) {
                    throw RuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}.")
                }

                function.call(this, arguments)
            }

            is Expr.Get -> {
                val obj = evaluate(expr.obj)
                if (obj is LoxInstance) {
                    obj.get(expr.name)
                } else {
                    throw RuntimeError(expr.name, "Only instances have properties")
                }
            }

            is Expr.Variable -> lookUpVariable(expr.name, expr)

            is Expr.Assign -> {
                val value = evaluate(expr.value)
                val distance = locals[expr]
                if (distance != null) {
                    environment.assignAt(distance, expr.name, value)
                } else {
                    globals.assign(expr.name, value)
                }
                value
            }
        }
    }

    private fun lookUpVariable(name: Token, expr: Expr) : Any? {
        val distance = locals[expr]
        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
    }

    private fun isTruthy(value: Any?): Boolean {
        return value != null && value != false
    }

    private fun isEqual(a: Any?, b: Any?): Boolean {
        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) return

        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            org.example.runtimeError(error)
        }
    }

    private fun execute(statement: Stmt) {
        when (statement) {
            is Stmt.Expression -> evaluate(statement.expression)

            is Stmt.Function -> {
                val function = LoxFunction(statement, environment, false)
                environment.define(statement.name.lexeme, function)
            }
            is Stmt.If -> {
                if (isTruthy(evaluate(statement.condition))) {
                    execute(statement.thenBranch)
                } else if (statement.elseBranch != null) {
                    execute(statement.elseBranch)
                }
            }
            is Stmt.Print -> {
                val value = evaluate(statement.expression)
                println(stringify(value))
            }

            is Stmt.Return -> {
                val value = statement.value?.let { evaluate(it) }
                throw Return(value)
            }

            is Stmt.Var -> {
                val value = if (statement.initializer != null) {
                    evaluate(statement.initializer)
                } else {
                    null
                }

                environment.define(statement.name.lexeme, value)
            }

            is Stmt.While -> {
                while (isTruthy(evaluate(statement.condition))) {
                    execute(statement.body)
                }
            }

            is Stmt.Block -> {
                executeBlock(statement.statements, Environment(environment))
            }

            is Stmt.Class -> {
                var superclass : LoxClass? = null
                if (statement.superclass != null) {
                    val value =  evaluate(statement.superclass)
                    if(value !is LoxClass) {
                        throw RuntimeError(statement.superclass.name, "Superclass must be a class.")
                    }

                    superclass = value as LoxClass?
                }
                environment.define(statement.name.lexeme, null)

                if (statement.superclass != null) {
                    environment = Environment(environment)
                    environment.define("super", superclass)
                }

                val methods = mutableMapOf<String, LoxFunction>()
                for (method in statement.methods) {
                    val function = LoxFunction(method, environment,
                        method.name.lexeme.equals("init"))
                    methods[method.name.lexeme] = function
                }

                val klass = LoxClass(statement.name.lexeme, superclass, methods)

                if (superclass != null) {
                    environment = environment.enclosing!!
                }

                environment.assign(statement.name, klass)
            }
        }
    }

    fun executeBlock(statements: List<Stmt>, newEnvironment: Environment) {
        val previous = environment
        try {
            environment = newEnvironment

            for (statement in statements) {
                execute(statement)
            }
        } finally {
            environment = previous
        }
    }

    private fun stringify(value: Any?): String {
        if (value == null) return "nil"

        if (value is Double) {
            val text = value.toString()
            return text.removeSuffix(".0")

        }

        return value.toString()
    }
}