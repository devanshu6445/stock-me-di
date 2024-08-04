echo "Dev test repo"

if [ ! -f "settings.gradle.kts" ]; then
  echo "ERROR:"
  echo "Please run this script from the root directory of the repo."
  exit 1
fi

gradle :compiler:integration-tests:compileKotlin
