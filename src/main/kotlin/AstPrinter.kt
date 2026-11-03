package org.example

class AstPrinter {
    fun print(expr: Expr): Any? {
        return when (expr) {
            is Expr.Literal -> {
                expr.value?.toString() ?: "nil"
            }
            is Expr.Grouping -> {
                parenthesize("group", expr.expression)
            }

            is Expr.Unary -> {
                parenthesize(expr.operator.lexeme, expr.right)
            }

            is Expr.Binary -> {
                parenthesize(expr.operator.lexeme, expr.left, expr.right)
            }

            is Expr.Variable -> expr.name.lexeme

            is Expr.Assign -> {
                parenthesize("assign ${expr.name.lexeme}", expr.value)
            }

            is Expr.Logical -> {
                parenthesize("logical", expr.left, expr.right)
            }

            is Expr.Call -> {
                parenthesize("call", expr.callee)
            }

            is Expr.Get -> {
                parenthesize("get", expr.obj)
            }

            is Expr.Set -> {
                parenthesize("set", expr.obj)
            }

            is Expr.Super -> {
                "super.${expr.method.lexeme}"
            }

            is Expr.This -> {
                parenthesize("this")
            }
        }
    }

    private fun parenthesize(name: String, vararg exprs: Expr): String {
        val result = StringBuilder()
        result.append("($name")

        for (expr in exprs) {
            result.append(" ")
            result.append(print(expr))
        }

        result.append(")")

        return result.toString()
    }
}