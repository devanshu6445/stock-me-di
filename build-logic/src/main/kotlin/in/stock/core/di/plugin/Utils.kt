package `in`.stock.core.di.plugin

import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

inline fun <T : Task> TaskProvider<out T>?.letIfPresent(block: (TaskProvider<out T>) -> Unit) {
	if (this != null && isPresent) {
		block(this)
	}
}

fun <T : Task> TaskProvider<out T>?.dependsOn(taskName: String): TaskProvider<out T>? {
	this?.letIfPresent { nonNullThis ->
		nonNullThis.configure { this.dependsOn(taskName) }
	}

	return this
}