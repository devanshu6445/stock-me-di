import com.vanniktech.maven.publish.SonatypeHost

plugins {
	id("com.vanniktech.maven.publish")
	signing
}

mavenPublishing {
	// Configure POM metadata for the published artifact
	pom {
		name.set("K-DI")
		description.set("K-DI, A compile-time dependency injection framework for KMM")
		inceptionYear.set("2025")
		url.set("https://github.com/devanshu6445/stock-me-di")

		licenses {
			license {
				name.set("MIT")
				url.set("https://opensource.org/licenses/MIT")
			}
		}

		// Specify developers information
		developers {
			developer {
				id.set("Devanshu Pathsariya")
				name.set("devanshu6445")
				email.set("devanshu6445@gmail.com")
			}
		}

		// Specify SCM information
		scm {
			url.set("https://github.com/devanshu6445/stock-me-di")
		}
	}

	// Configure publishing to Maven Central
	publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

	// Enable GPG signing for all publications
	signAllPublications()
}

signing {
	val privateKeyFile = project.findProperty("signing.privateKeyFile") as? String
		?: error("No Private key file found")
	val passphrase = project.findProperty("signing.password") as? String
		?: error("No Passphrase found for signing")

	// Read the private key from the file
	val privateKey = File(privateKeyFile).readText(Charsets.UTF_8)

	useInMemoryPgpKeys(privateKey, passphrase)
	publishing.publications.all {
		sign(this)
	}
}
