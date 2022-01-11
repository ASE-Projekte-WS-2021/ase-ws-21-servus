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

## Branching

- No pushes direct to main or develop
- Pushes to main are only allowed by pull requests from develop
- Pushed to develop are allowed by all pull requests that succeed the quality gates (mentioned below)
- Every issue/feature needs a separate branch with a feature/ or hotfix/ prefix
 - A branch for Issue Create Map View #5 could be named feature/5-map-view
- Quality gates, that need to be succeeded before a merge to develop is possible:
 - Reviewed by 1 peer 
 - Succeeded the build pipeline (includes style check, tests and possibly other gates)
