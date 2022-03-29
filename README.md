
# Servus
Servus is an informal greeting in the Bavarian language. The core functionality of our app is to enable German- and English-speaking students to get to know other students in their area through real-life events. With Servus, users can create events at their own location. The events are represented by markers on a map. Events can range from simple hang-outs to sporty activities or parties. Each marker has a corresponding activity icon and its current attendee count which are displayed on the map to give the user initial information before even clicking on the details. Furthermore, when the maximum number of allowed participants is reached, the marker on the map is greyed out and locked until someone leaves the event. For a better understanding of the distance between an event and the user's current location, the latter is always displayed visually on the map. Users are allowed to participate at any available event. Upon participation, the user's profile information will be shared as a "Servus Card" which is then visible in the event details for all other users.

For a quick overview, we created an overview with first app impressions referring to the main functionalities of Servus.
<p align="center">
 <img src="https://user-images.githubusercontent.com/53007064/160564186-3179f696-9a19-4f3f-8154-ba40e81444a0.png" style="border-radius: 12px; width: 80%;"/><br />
</p>

For further information like our target audience, additional functionalities and unique selling propositions view our [App Presentation](https://docs.google.com/presentation/d/1ZVOGwrLfBCtfPwkJsKihA0lLbh0UIbWA0A1cBrZbMGQ/).

<br/>

## Development Setup

 1. Make sure, that Java (>= version 11) is installed and set up correctly (check with `javac -version` command)
    - If another version was installed previously and the environment variable is corrected, you may need to restart
    - https://www.oracle.com/java/technologies/downloads/#jdk17-windows 
 2. Setup git hooks by executing the `setup_githooks` scripts in `tools`
 3. Copy the `.env.template` file, rename it to `.env` and fill in all necessary data (you will need a google maps API key)
<br/>

## Git Hooks

On every commit git automatically runs checkstyle before committing.

<br/>

## CI/CD Pipeline

On every push and merge the pipeline runs checkstyle, all tests and checks if the code builds.
If a version tag (e.g. v1) was added, it will be released on the github project.

<br/>

## Branching

- No pushes direct to main or develop
- Pushes to main are only allowed by pull requests from develop
- Pushed to develop are allowed by all pull requests that succeed the quality gates (mentioned below)
- Every issue/feature needs a separate branch with a feature/ or hotfix/ prefix
 - A branch for Issue Create Map View #5 could be named feature/5-map-view
- Quality gates, that need to be succeeded before a merge to develop is possible:
 - Reviewed by 1 peer 
 - Succeeded the build pipeline (includes style check, tests and possibly other gates)
<br/>

## Development Workflow

**Regularly check for PRs that need to be reviewed.**

1. Choose an issue to work on (this issue should not be blocked by another issue)
2. Create a new feature branch from `develop` for your issue (e.g. `feature/5-map-view` for issue #5)
3. Work on your branch
4. After your done developing, remerge `develop` into your branch to avoid merge conflicts
5. Create a Pull Request to merge your branch into `develop`. After the quality checks suceeded and it is reviewed it can be merged.
<br/>

## Contributors

The individual team members did not have fixed areas of responsibility. Everybody worked in many areas, yet thematic focuses have emerged over time.

- Arne Tiedemann (Design, User Flow, Profiles)
- Benedikt Strasser (Architecture, Firebase, Events)
- Michelle Lanzinger (Map, Map Components, Translation)
- Matthias Zerniekel (Firebase, Profiles, Events)
