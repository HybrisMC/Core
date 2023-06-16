# Toolchain

The toolchain is a utility that manages the structure of the entire project and
sets up specific [version modules](versions.md). It depends on the mapping
library, which has been made a seperate project / included build in order to be
able to be used in the `buildSrc`, which is where the toolchain lives. The
toolchain has a few tasks:

- Keep track of the version modules that have been declared
- Remap the game based on the mapping dependency
- Download and extract libraries and natives that are required to launch the
  game in the development environment
- Declare run configurations for IntelliJ Idea
