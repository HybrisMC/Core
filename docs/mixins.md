# Mixins

The core exposes a service to interact with SpongePowered Mixins, and loading
them through the common GameLoader. A way to register these mixins properly has
not yet been made, and should happens when the version modules are properly
implemented. Mixins, as we all know, are dependant on the specific version of
the game. To work around this / try to write "universal" code, conditional
mixins can be created. A typical examplel looks like this:

```kt
// Notice the usage of targets instead of the class reference
// This is because you cannot do class references if the class is not on compile classpath
@Mixin(targets = ["net/minecraft/client/MinecraftClient"])
// Declares the versions to apply this mixin to.
// In the future, this will support version ranges
@OnlyOn(MinecraftVersion.V1_8, MinecraftVersion.V1_12)
class TestMixin {
    @Inject(at = [At("HEAD")], method = ["initializeGame()V"])
    fun testA(ci: CallbackInfo) {
        println("This mixin will always be called on 1.8 and 1.12!")
    }

    @Inject(at = [At("HEAD")], method = ["initializeGame()V"])
    @OnlyOn(MinecraftVersion.V1_8)
    fun testB(ci: CallbackInfo) {
        println("This mixin will only be called on 1.8!")
    }
}
```
