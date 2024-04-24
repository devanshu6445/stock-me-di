package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.Resolver

class KspResolver(
    private val delegate: Resolver
) : Resolver by delegate