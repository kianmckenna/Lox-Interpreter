package org.example

sealed class Expr {
    data class Literal(val value: Any?) : Expr()

    data class Grouping(val expression: Expr) : Expr()

    data class Binary(val left: Expr, val operator: Token, val right: Expr) : Expr()

    data class Unary(val operator: Token, val right: Expr) : Expr()

    data class This(val keyword: Token) : Expr()

    data class Super(val keyword: Token, val method: Token) : Expr()

    data class Set(val obj: Expr, val name: Token, val value: Expr) : Expr()

    data class Variable(val name: Token) : Expr()

    data class Assign(val name: Token, val value: Expr) : Expr()

    data class Logical(val left: Expr, val operator: Token, val right: Expr) : Expr()

    data class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) : Expr()

    data class Get(val obj: Expr, val name: Token) : Expr()

    fun describe(expr: Expr): String {
        return when (expr) {
            is Expr.Literal -> "This is a literal"
            is Expr.Grouping -> "This is a grouping"
            is Expr.Binary -> "This is a binary"
            is Expr.Unary -> "This is unary"
            is Expr.Variable -> "This is a variable"
            is Expr.Assign -> "This is assign"
            is Expr.Logical -> "This is logical"
            is Expr.Call -> "This is a call"
            is Expr.Get -> "This is a get"
            is Expr.Set -> "This is a set"
            is Expr.This -> "This is a this"
            is Expr.Super -> "This is a super"
        }
    }
}