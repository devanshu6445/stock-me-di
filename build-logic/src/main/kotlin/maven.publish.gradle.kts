import com.vanniktech.maven.publish.SonatypeHost

plugins {
	id("com.vanniktech.maven.publish")
	signing
}

mavenPublishing {
	// Configure POM metadata for the published artifact
	pom {
		name.set("KDI Compiler Plugin")
		description.set("Kotlin compiler plugin for dependency injection")

		url.set("https://github.com/devanshu6445/kdi")

		licenses {
			license {
				name.set("The Apache License, Version 2.0")
				url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
			}
		}

		developers {
			developer {
				id.set("devanshu6445")
				name.set("Devanshu Pathsariya")
			}
		}

		scm {
			connection.set("https://github.com/devanshu6445/kdi.git")
			developerConnection.set("https://github.com/devanshu6445/kdi.git")
			url.set("https://github.com/devanshu6445/kdi")
		}
	}

	// Configure publishing to Maven Central
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

	// Enable GPG signing for all publications
	signAllPublications()
}

// signing {
//
// 	val privateKeyFile = project.findProperty("signing.privateKeyFile") as? String
// 		?: error("No Private key file found")
// 	val passphrase = project.findProperty("signing.password") as? String
// 		?: error("No Passphrase found for signing")
//
// 	// Read the private key from the file
// 	val privateKey = File(privateKeyFile).readText(Charsets.UTF_8)
//
// 	useInMemoryPgpKeys(privateKey, passphrase)
// 	publishing.publications.all {
// 		sign(this)
// 	}
// }
