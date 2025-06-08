package `in`.kdi.core.kcp.k2

import `in`.kdi.core.kcp.utils.FqNames
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.utils.AbstractSimpleClassPredicateMatchingService

class PredicateMatcher(
  session: FirSession
) : AbstractSimpleClassPredicateMatchingService(session = session) {
  override val predicate: DeclarationPredicate = DeclarationPredicate.create {
    annotated(FqNames.EntryPoint)
  }
}