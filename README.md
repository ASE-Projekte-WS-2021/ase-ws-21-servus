# Servus

## Development Setup

 1. Make sure, that Java (>= version 11) is installed and set up correctly (check with `javac -version` command)
    - If another version was installed previously and the environment variable is corrected, you may need to restart
    - https://www.oracle.com/java/technologies/downloads/#jdk17-windows 
 2. Setup git hooks by executing the `setup_githooks` scripts in `tools`

## Git Hooks

On every commit git automatically runs checkstyle before committing.

## CI/CD Pipeline

On every push and merge the pipeline runs checkstyle, all tests and checks if the code builds.
If a version tag (e.g. v1) was added, it will be released on the github project.
