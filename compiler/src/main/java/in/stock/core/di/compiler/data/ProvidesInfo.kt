package `in`.stock.core.di.compiler.data

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

data class ProvidesInfo(
    val functionName: KSName,
    val moduleClass: KSClassDeclaration,
    val scope: KSAnnotation,
    val dependencies: List<KSTypeReference>,
    val dependencyType: KSTypeReference,
    val parametersName: List<KSName>
) {
    val resolvedDepType by lazy {
        dependencyType.resolve()
    }

    val providerName by lazy {
        resolvedDepType.toClassName().let {
            ClassName(it.packageName, it.simpleName + "Provider")
        }
    }
}