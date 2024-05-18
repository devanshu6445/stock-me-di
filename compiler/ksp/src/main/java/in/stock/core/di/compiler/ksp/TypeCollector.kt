package `in`.stock.core.di.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import `in`.stock.core.di.compiler.core.KspResolver
import `in`.stock.core.di.compiler.core.Messenger
import `in`.stock.core.di.compiler.ksp.exceptions.ClassConstructException
import `in`.stock.core.di.compiler.ksp.utils.*
import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import javax.inject.Inject
import javax.inject.Singleton

// todo refactor this to provide different implementation for collecting types for different kotlin elements
@Singleton
class TypeCollector @Inject constructor(
  private val resolver: KspResolver,
  private val messenger: Messenger
) {

  private val typeToComponentMap = hashMapOf<String, List<KSClassDeclaration>>()

  private val allProvidersMap by lazy {
    resolver.getAllProviders().associate {
      it.returnType?.resolve()?.declaration?.qualifiedName?.asString() to it.parentDeclaration
    }
  }

  fun collectTypes(type: KSDeclaration): Sequence<KSClassDeclaration> {
    return when (type) {
      is KSClassDeclaration, is KSFunctionDeclaration -> {
        val components = type.getComponents()
          .distinctBy { it.qualifiedName?.asString() }
        typeToComponentMap[type.qualifiedName?.asString().orEmpty()] = components.toList()
        components
      }

      else -> {
        TODO()
      }
    }
  }

  private fun KSDeclaration.getDependencies(): Sequence<KSDeclaration> {
    val resolveParameters = when (this) {
      is KSClassDeclaration -> {
        getConstructors().flatMap { it.parameters }.map { it.type } + getDeclaredProperties().map { it.type }
      }
      is KSFunctionDeclaration -> {
        parameters.map { it.type }.asSequence()
      }
      else -> {
        messenger.fatalError(IllegalArgumentException("This type is not supported by @EntryPoint"),this)
      }
    }.map {
      it.resolve().declaration
    }
    return resolveParameters + resolveParameters.flatMap { it.getDependencies() }
  }

  private fun KSDeclaration.getComponents(): Sequence<KSClassDeclaration> {
    val types = getDependencies().distinctBy { it.qualifiedName?.asString() }

    val components = sequence {
      types.forEach {
        messenger.warn("${it.qualifiedName?.asString()}", it)
        when (it) {
          is KSClassDeclaration -> {
            when {
              it.hasAnnotation(INJECT.packageName, INJECT.simpleName) -> {
                val components = typeToComponentMap[qualifiedName?.asString().orEmpty()]

                if (components != null) {
                  yieldAll(components)
                } else {
                  val component =
                    it.annotations.firstOrNull { annotation ->
                      annotation.annotationType.resolve()
                        .declaration.hasAnnotation(Scope.packageName, Scope.simpleName)
                    }
                      // todo remove this `AssociatedWith` annotation dependency from codebase
                      ?.annotationType?.resolve()?.declaration?.getAnnotationArgument(
                        AssociatedWith::class.qualifiedName.orEmpty()
                      ) as? KSClassDeclaration?
                  if (component != null) {
                    yield(component)
                  }
                }
              }

              allProvidersMap[it.qualifiedName?.asString()] != null -> {
                yield(
                  allProvidersMap[it.qualifiedName?.asString()]
                    ?.getAnnotationArgument(InstallIn::class.qualifiedName.orEmpty()) as KSClassDeclaration
                )
              }

              else -> {
                messenger.fatalError(ClassConstructException("Please mark the class with @Inject or provide it through @Provides"),it)
              }
            }
          }

          is KSFunctionDeclaration -> {
            throw NotImplementedError()
          }

          else -> throw NotImplementedError()
        }
      }
    }

    return components
  }
}

@OptIn(KspExperimental::class)
fun Resolver.getAllProviders(): Sequence<KSFunctionDeclaration> {
  suspend fun SequenceScope<KSFunctionDeclaration>.visit(declarations: Sequence<KSDeclaration>) {
    declarations.forEach {
      when (it) {
        is KSClassDeclaration -> {
          if (it.hasAnnotation(Module::class)) {
            visit(it.getAllFunctions())
          }
        }

        is KSFunctionDeclaration -> {
          if (it.hasAnnotation(Provides.packageName, Provides.simpleName)) {
            yield(it)
          }
        }
      }
    }
  }

  return sequence {
    // get all the provider from the current module
    for (file in getAllFiles()) {
      visit(file.declarations)
    }

    // get all the provider from other dependent module
    visit(getDeclarationsFromPackage(InternalPackage))
  }
}