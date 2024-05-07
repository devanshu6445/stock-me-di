package `in`.stock.core.di.kotlin_di_compiler.k2

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.AnnotationFqn
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionSessionComponent
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate

class ConstructorChecker(session: FirSession) : FirExtensionSessionComponent(session) {
  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(DeclarationPredicate.create {
      annotated(AnnotationFqn(""))
    })
  }


}