package org.example

class LoxClass(val name: String, val superclass: LoxClass?, val methods: MutableMap<String, LoxFunction>) : LoxCallable {

    fun findMethod(name: String): LoxFunction? {
        if (methods.containsKey(name)) return methods[name]

        if (superclass != null) {
            return superclass.findMethod(name)
        }

        return null
    }

    override fun toString(): String {
        return name
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>) : Any? {
        val instance = LoxInstance(this)
        val initializer = findMethod("init")
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments)
        }

        return instance
    }

    override fun arity() : Int {
        val initializer = findMethod("init")
        if (initializer == null) return 0
        return initializer.arity()
    }
}