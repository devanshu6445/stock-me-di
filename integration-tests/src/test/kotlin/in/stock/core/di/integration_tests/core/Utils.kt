package `in`.stock.core.di.integration_tests.core

import java.io.File
import javax.tools.Diagnostic

internal fun File.readTextAndUnify() = readText().unify()

internal fun String.unify() = replace("\r\n", "\n").trimEnd()

fun String.filterByKind(kind: Diagnostic.Kind): String = buildString {
    var currentKind: Diagnostic.Kind? = null
    for (line in this@filterByKind.lineSequence()) {
        val lineKind = line.matchLine()
        if (lineKind != null) {
            currentKind = lineKind
        }
        if (currentKind == kind) {
            append(line)
            append('\n')
        }
    }
}

fun String.matchLine(): Diagnostic.Kind? {
    if (length < 2) return null
    val matchedKind = when (get(0)) {
        'e' -> Diagnostic.Kind.ERROR
        'w' -> Diagnostic.Kind.WARNING
        'v' -> Diagnostic.Kind.NOTE
        else -> null
    } ?: return null

    return if (get(1) == ':') {
        matchedKind
    } else {
        null
    }
}