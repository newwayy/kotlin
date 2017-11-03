// IGNORE_BACKEND: NATIVE
// WITH_COROUTINES
// WITH_RUNTIME

// MODULE: lib(support)
// FILE: lib.kt

import helpers.*
import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*

var continuation: () -> Unit = { }
var log = ""
var finished = false

suspend fun <T> foo(v: T): T = suspendCoroutineOrReturn { x ->
    continuation = {
        x.resume(v)
    }
    log += "foo($v);"
    COROUTINE_SUSPENDED
}

inline suspend fun boo(v: String): String {
    foo("!$v")
    log += "boo($v);"
    return foo(v)
}

inline suspend fun bar(v: String): String {
    val x = boo(v)
    log += "bar($x);"
    return x
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(handleResultContinuation {
        continuation = { }
        finished = true
    })
}

// MODULE: main(lib)
// FILE: main.kt

import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*

suspend fun baz() {
    val a = bar("A")
    log += "$a;"
    log += "between bar;"
    val b = bar("B")
    log += "$b;"
}

val expectedString =
        "foo(!A);@;boo(A);foo(A);@;bar(A);A;" +
        "between bar;" +
        "foo(!B);@;boo(B);foo(B);@;bar(B);B;"

fun box(): String {
    builder {
        baz()
    }

    while (!finished) {
        log += "@;"
        continuation()
    }

    if (log != expectedString) return "fail: $log"

    return "OK"
}
