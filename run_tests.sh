#!/bin/bash

echo "Navigating to the script's directory (assuming project root)..."
# Get the directory of the script itself
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "Current working directory: $(pwd)"

echo "Starting JUnit tests with Maven..."

# Run Maven clean test
# The 'clean' goal removes previous build artifacts
# The 'test' goal compiles test sources and runs the tests
mvn clean test

TEST_EXIT_CODE=$?

echo "Maven tests finished with exit code: $TEST_EXIT_CODE"

if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "All tests passed successfully!"
    echo "You can find the output PDF files (if tests were successful and configured to save them) in the 'test_outputs' directory relative to the project root."
else
    echo "Some tests failed. Please check the Maven output above for details."
fi

exit $TEST_EXIT_CODE 