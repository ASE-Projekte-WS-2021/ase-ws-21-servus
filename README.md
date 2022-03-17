# Servus

Servus is an informal greeting in the Bavarian language. The core functionality of our app is to enable students to get to know other students in their area through real-life events. With Servus, users can create events at their own location. The events are represented by markers on a map and other users can join these events to participate. Events can range from simple hang-outs to sporty activities or parties.

## Development Setup

 1. Make sure, that Java (>= version 11) is installed and set up correctly (check with `javac -version` command)
    - If another version was installed previously and the environment variable is corrected, you may need to restart
    - https://www.oracle.com/java/technologies/downloads/#jdk17-windows 
 2. Setup git hooks by executing the `setup_githooks` scripts in `tools`
 3. Copy the `.env.template` file, rename it to `.env` and fill in all necessary data 

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

## Development Workflow

**Regularly check for PRs that need to be reviewed.**

1. Choose an issue to work on (this issue should not be blocked by another issue)
2. Create a new feature branch from `develop` for your issue (e.g. `feature/5-map-view` for issue #5)
3. Work on your branch
4. After your done developing, remerge `develop` into your branch to avoid merge conflicts
5. Create a Pull Request to merge your branch into `develop`. After the quality checks suceeded and it is reviewed it can be merged.
