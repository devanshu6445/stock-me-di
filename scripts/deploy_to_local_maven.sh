echo "Publish compiler plugin"

if [ ! -f "settings.gradle.kts" ]; then
    echo "ERROR:"
    echo "Please run this script from the root directory of the repo."
    exit 1
fi

gradle :compiler:core:publishToMavenLocal
gradle :runtime:publishToMavenLocal
gradle :compiler:ksp:publishToMavenLocal
gradle :compiler:kcp:publishToMavenLocal