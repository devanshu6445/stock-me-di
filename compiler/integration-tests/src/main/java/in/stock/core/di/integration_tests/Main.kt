package `in`.stock.core.di.integration_tests

import `in`.stock.core.di.runtime.annotations.AssociatedWith
import `in`.stock.core.di.runtime.annotations.Component
import `in`.stock.core.di.runtime.annotations.InstallIn
import `in`.stock.core.di.runtime.annotations.Module
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Qualifier
import me.tatarka.inject.annotations.Scope

fun main(args: Array<String>) {
	val entryPoint = EntryPointTest()
	entryPoint.onCreate()
}

@Component
@SampleScope
abstract class Sample {

	@Named("one")
	abstract val fooSet: Set<Foo>
}

@Scope
@AssociatedWith(Sample::class)
annotation class SampleScope


@Module
@InstallIn(Sample::class)
@SampleScope
object SampleModule {

	@IntoSet
	@Provides
	@Named("one")
	fun provideVMMap(): Foo = Foo()

	@IntoSet
	@Provides
	@Named("one")
	fun provideVMMap1(): Foo = Foo()
}

@Qualifier
@Target(
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.VALUE_PARAMETER,
	AnnotationTarget.TYPE, AnnotationTarget.PROPERTY
)
annotation class Named(val value: String)

class Foo