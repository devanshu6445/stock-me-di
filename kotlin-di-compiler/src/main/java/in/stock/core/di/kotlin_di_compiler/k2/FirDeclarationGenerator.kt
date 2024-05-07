package `in`.stock.core.di.kotlin_di_compiler.k2

import `in`.stock.core.di.kotlin_di_compiler.utils.FqNames
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.LookupPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.plugin.createTopLevelFunction
import org.jetbrains.kotlin.fir.resolve.providers.firProvider
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames

class FirDeclarationGenerator(session: FirSession, private val messageCollector: MessageCollector) :
  FirDeclarationGenerationExtension(session) {

  companion object {
    val GeneratedFunction = Name.identifier("createFunction")
  }

  private val predicate = LookupPredicate.create {
    annotated(FqNames.EntryPoint)
  }

  private val matchedClasses by lazy {
    session.predicateBasedProvider.getSymbolsByPredicate(predicate)
      .filterIsInstance<FirRegularClassSymbol>()
  }

  private val matchedClassesClassId by lazy {
    matchedClasses.map {
      it.classId
    }
  }

  private val matchedTopLevelFunctions by lazy {
    session.predicateBasedProvider.getSymbolsByPredicate(predicate)
      .filterIsInstance<FirNamedFunctionSymbol>()
  }

  private val matchedTopLevelFunctionsCallableId by lazy {
    matchedTopLevelFunctions.map {
      it.callableId
    }
  }

  override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
    val constructor = createConstructor(
      owner = context.owner,
      key = Key,
      isPrimary = false,
    ) {
      val component = session.firProvider.getFirClassifierByFqName(
        ClassId(
          context.owner.classId.packageFqName, Name.identifier("${context.owner.name.identifier}Component")
        )
      )?.symbol as FirRegularClassSymbol

      valueParameter(
        name = Name.identifier("component"),
        type = ConeClassLikeTypeImpl(
          component.toLookupTag(),
          emptyArray(),
          false
        )
      )
    }
    return listOf(constructor.symbol)
  }

  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?
  ): List<FirNamedFunctionSymbol> {
    return when {
      callableId in matchedTopLevelFunctionsCallableId -> {
        listOf(
          createTopLevelFunction(
            Key,
            callableId,
            session.builtinTypes.stringType.type
          ).symbol
        )
      }
//            callableId.classId in matchedClassesClassId -> {
//                listOf(
//                    createMemberFunction(
//                        context?.owner ?: return emptyList(),
//                        Key,
//                        callableId.callableName,
//                        returnType = session.builtinTypes.stringType.type,
//                    ).symbol
//                )
//            }

      else -> emptyList()
    }
  }


  override fun getTopLevelCallableIds(): Set<CallableId> {
    return matchedTopLevelFunctions.map {
      it.callableId
    }.toSet()
  }

  override fun getTopLevelClassIds(): Set<ClassId> {
    messageCollector.report(
      CompilerMessageSeverity.STRONG_WARNING,
      "$matchedTopLevelFunctions"
    )
    return matchedTopLevelFunctions.map {
      ClassId(it.callableId.packageName, it.name)
    }.toSet()
  }

  override fun getCallableNamesForClass(
    classSymbol: FirClassSymbol<*>,
    context: MemberGenerationContext
  ): Set<Name> {
    return if (classSymbol in matchedClasses)
      setOf(classSymbol.name, SpecialNames.INIT)
    else emptySet()
  }

  override fun FirDeclarationPredicateRegistrar.registerPredicates() {
    register(DeclarationPredicate.create {
      annotated(FqNames.EntryPoint)
    })
  }

  object Key : GeneratedDeclarationKey() {
    override fun toString(): String {
      return "EntryPointGeneratorKey"
    }
  }
}