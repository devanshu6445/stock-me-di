package `in`.stock.core.di.compiler.core

import com.google.devtools.ksp.processing.Resolver

internal class KspResolver(
  private val delegate: Resolver
) : XResolver, Resolver by delegate