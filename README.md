# Hybris Core
The core library of the HybrisMC ecosystem.

## Building
Clone the repository, and run the following command:
```shell
./gradlew build
```
If Gradle complains it cannot find or download a toolchain, download a JDK 16 (or higher) distribution. [OpenJDK](https://jdk.java.net) is recommended.

A development environment will be initialized the first time the Gradle wrapper is called. Launch the game using:
```shell
./gradlew :<version>:launchGame
```
(where `<version>` should be replaced with the name of the subproject of the target version).

## Documentation
A work-in-progress documentation set can be found in [here](docs).

## Contributing
Hybris is open source, contributions are welcome! Make sure to [create a pull request](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request) with your changes and a maintainer will review it. If a change is more complex, always make sure to discuss it in an Issue first.