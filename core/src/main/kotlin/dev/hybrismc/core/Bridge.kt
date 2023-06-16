/*
 * HybrisMC, a Minecraft toolchain and client
 * Copyright (C) 2023, The HybrisMC Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.hybrismc.core

import com.grappenmaker.jvmutil.InstanceAccessor
import com.grappenmaker.jvmutil.accessor
import com.grappenmaker.jvmutil.asmTypeOf
import com.grappenmaker.jvmutil.returnType
import java.io.File
import java.net.InetAddress
import java.net.Proxy
import java.net.SocketAddress
import java.net.URI
import java.security.KeyPair
import java.security.PublicKey
import java.security.Signature
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantLock

fun initBridge() = Unit

// Most of this file is auto-generated
val minecraftClientAccess = accessor<_, MinecraftClient.Static>()

@Named("net/minecraft/client/MinecraftClient")
interface MinecraftClient {
    val attackCooldown: Int
    val blockRenderManager: BlockRenderManager
    val cameraEntity: Entity
    val chunkCullingEnabled: Boolean
    val connectedToRealms: Boolean
    val currentScreen: Screen?
    val entityRenderDispatcher: EntityRenderDispatcher
    val fpsCounter: Int
    val fpsDebugString: String
    val gameRenderer: GameRenderer
    val gameVersion: String
    val inGameHud: InGameHud
    val interactionManager: ClientPlayerInteractionManager
    val is64Bit: Boolean
    val isDemo: Boolean
    val itemRenderer: ItemRenderer
    val languageManager: LanguageManager
    val metricsData: MetricsData
    val musicTracker: MusicTracker
    val openProfilerSection: String
    val particleManager: ParticleManager
    val paused: Boolean
    val profiler: Profiler
    val runDirectory: File
    val running: Boolean
    val server: IntegratedServer
    val session: Session
//    val sessionPropertyMap: PropertyMap
    val sessionService: MinecraftSessionService
    val skinProvider: PlayerSkinProvider
    val skipGameRender: Boolean
    val soundManager: SoundManager
    val targetedEntity: Entity
    val textRenderer: TextRenderer
    val textureManager: TextureManager
    val world: ClientWorld
    val worldRenderer: WorldRenderer
    fun cleanUpAfterCrash()
    fun forcesUnicodeFont(): Boolean
    fun handleBlockBreaking(breaking: Boolean)
    fun handleProfilerKeyPress(digit: Int)
    fun isConnectedToRealms(): Boolean
    fun isInSingleplayer(): Boolean
    fun isIntegratedServerRunning(): Boolean
    fun isPaused(): Boolean
    fun run()
    fun scheduleStop()
    fun setConnectedToRealms(connectedToRealms: Boolean)

    @Named("setScreen", "openScreen")
    fun setScreen(screen: Screen)
    fun stop()
    fun tick()
    val networkProxy: Proxy
//    val sessionProperties: PropertyMap

    @OmitMissingImplementation
    val mouse: Any?

    interface Static : StaticAccessor<MinecraftClient> {
        val currentFps: Int
        val instance: MinecraftClient
        fun checkIs64Bit(): Boolean
        fun isAmbientOcclusionEnabled(): Boolean
        fun isHudEnabled(): Boolean
    }

    companion object : Static by minecraftClientAccess.static()
}

val MinecraftClient.actualMouse get() = mouse as? Mouse

val blockRenderManagerAccess = accessor<_, BlockRenderManager.Static>()

@Named("net/minecraft/client/render/block/BlockRenderManager")
interface BlockRenderManager {
    val blockModelRenderer: BlockModelRenderer
    val fluidRenderer: FluidRenderer

    interface Static : StaticAccessor<BlockRenderManager>
    companion object : Static by blockRenderManagerAccess.static()
}

val blockModelRendererAccess = accessor<_, BlockModelRenderer.Static>()

@Named("net/minecraft/client/render/block/BlockModelRenderer")
interface BlockModelRenderer {
    interface Static : StaticAccessor<BlockModelRenderer>
    companion object : Static by blockModelRendererAccess.static()
}

val fluidRendererAccess = accessor<_, FluidRenderer.Static>()

@Named("net/minecraft/client/render/block/FluidRenderer")
interface FluidRenderer {
    val lavaSprites: Array<Sprite>
    val waterSprites: Array<Sprite>
    fun onResourceReload()

    interface Static : StaticAccessor<FluidRenderer>
    companion object : Static by fluidRendererAccess.static()
}

val spriteAccess = accessor<_, Sprite.Static>()

@Named("net/minecraft/client/texture/Sprite")
interface Sprite {
    val x: Int
    val y: Int
    fun getFrameU(frame: Double): Float
    fun getFrameV(frame: Double): Float
    val maxU: Float
    val maxV: Float
    val minU: Float
    val minV: Float

    interface Static : StaticAccessor<Sprite>
    companion object : Static by spriteAccess.static()
}

val entityAccess = accessor<_, Entity.Static>()

@Named("net/minecraft/entity/Entity")
interface Entity {
    val dataTracker: DataTracker
    val distanceTraveled: Float
    val fallDistance: Float
    val fireTicks: Int
    val firstUpdate: Boolean
    val horizontalCollision: Boolean
    val horizontalSpeed: Float
    val ignoreCameraFrustum: Boolean
    val invulnerable: Boolean
    val netherPortalTime: Int
    val noClip: Boolean
    val onGround: Boolean
    val pitch: Float
    val prevHorizontalSpeed: Float
    val prevPitch: Float
    val prevX: Double
    val prevY: Double
    val prevYaw: Float
    val prevZ: Double
    val stepHeight: Float
    val timeUntilRegen: Int
    val touchingWater: Boolean
    val velocityDirty: Boolean
    val velocityModified: Boolean
    val verticalCollision: Boolean
    val world: World
    val yaw: Float
    fun addVelocity(x: Double, y: Double, z: Double)
    fun baseTick()
    fun canAvoidTraps(): Boolean
    fun checkBlockCollision()
    fun doesNotCollide(offsetX: Double, offsetY: Double, offsetZ: Double): Boolean
    fun doesRenderOnFire(): Boolean
    fun extinguish()
    fun getFlag(index: Int): Boolean
    fun handleStatus(status: Byte)
    fun initDataTracker()
    fun isAlive(): Boolean
    fun isAttackable(): Boolean
    fun isCustomNameVisible(): Boolean
    fun isFireImmune(): Boolean
    fun isImmuneToExplosion(): Boolean
    fun isInsideWall(): Boolean
    fun isInvisible(): Boolean
    fun isOnFire(): Boolean
    fun isPushable(): Boolean
    fun isSilent(): Boolean
    fun isSneaking(): Boolean
    fun isSprinting(): Boolean
    fun isTouchingWater(): Boolean
    fun kill()
    fun onSwimmingStart()
    fun refreshPositionAfterTeleport(x: Double, y: Double, z: Double)
    fun refreshPositionAndAngles(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)
    fun scheduleVelocityUpdate()
    fun setAir(air: Int)
    fun setCustomNameVisible(visible: Boolean)
    fun setFlag(index: Int, value: Boolean)
    fun setHeadYaw(headYaw: Float)
    fun setInvisible(invisible: Boolean)
    fun setOnFireFor(seconds: Int)
    fun setOnFireFromLava()
    fun setRotation(yaw: Float, pitch: Float)
    fun setSilent(silent: Boolean)
    fun setSneaking(sneaking: Boolean)
    fun setSprinting(sprinting: Boolean)
    fun setVelocityClient(x: Double, y: Double, z: Double)
    fun setYaw(yaw: Float)
    fun shouldRender(distance: Double): Boolean
    fun shouldRender(x: Double, y: Double, z: Double): Boolean
    fun shouldRenderName(): Boolean
    fun shouldSetPositionOnLoad(): Boolean
    fun spawnSprintingParticles()
    fun squaredDistanceTo(x: Double, y: Double, z: Double): Double
    fun tick()
    fun tickRiding()
    fun updatePosition(x: Double, y: Double, z: Double)
    fun updatePositionAndAngles(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)
    fun updateSubmergedInWaterState()
    fun updateTrackedPositionAndAngles(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float,
        interpolationSteps: Int,
        interpolate: Boolean
    )

    fun updateWaterState(): Boolean
    val air: Int
    val heightOffset: Double
    val maxNetherPortalTime: Int
    val mountedHeightOffset: Double
    val safeFallDistance: Int
    val savedEntityId: String
    val targetingMargin: Float

    interface Static : StaticAccessor<Entity> {
        var renderDistanceMultiplier: Double
    }

    companion object : Static by entityAccess.static()
}

val dataTrackerAccess = accessor<_, DataTracker.Static>()

@Named("net/minecraft/entity/data/DataTracker")
interface DataTracker {
    val dirty: Boolean
    val lock: ReadWriteLock
    val isEmpty: Boolean

    interface Static : StaticAccessor<DataTracker>
    companion object : Static by dataTrackerAccess.static()
}

val worldAccess = accessor<_, World.Static>()

@Named("net/minecraft/world/World")
interface World {
    val ambientDarkness: Int
    val border: WorldBorder
    val isClient: Boolean
    val iteratingTickingBlockEntities: Boolean
    val lcgBlockSeed: Int
    val rainGradient: Float
    val rainGradientPrev: Float
    val thunderGradient: Float
    val thunderGradientPrev: Float
    fun calculateAmbientDarkness()
    fun disconnect()
    fun getRainGradient(offset: Float): Float
    fun getSkyAngleRadians(tickDelta: Float): Float
    fun getThunderGradient(offset: Float): Float
    fun initWeatherGradients()
    fun isDay(): Boolean
    fun isRaining(): Boolean
    fun isThundering(): Boolean
    fun setLightningTicksLeft(ticks: Int)
    fun setRainGradient(rainGradient: Float)
    fun setThunderGradient(thunderGradient: Float)
    val timeOfDay: Long

    interface Static : StaticAccessor<World>
    companion object : Static by worldAccess.static()
}

val worldBorderAccess = accessor<_, WorldBorder.Static>()

@Named("net/minecraft/world/border/WorldBorder")
interface WorldBorder {
    val centerX: Double
    val centerZ: Double
    val damagePerBlock: Double
    val listeners: List<*>
    val warningBlocks: Int
    val warningTime: Int
    fun getDistanceInsideBorder(x: Double, z: Double): Double
    fun interpolateSize(oldSize: Double, targetSize: Double, time: Long)
    fun setCenter(x: Double, z: Double)
    fun setDamagePerBlock(damagePerBlock: Double)
    fun setSize(size: Double)
    fun setWarningBlocks(warningBlocks: Int)
    fun setWarningTime(warningTime: Int)
    val boundEast: Double
    val boundNorth: Double
    val boundSouth: Double
    val boundWest: Double
    val shrinkingSpeed: Double

    interface Static : StaticAccessor<WorldBorder>
    companion object : Static by worldBorderAccess.static()
}

val screenAccess = accessor<_, Screen.Static>()

@Named("net/minecraft/client/gui/screen/Screen")
interface Screen {
    val clickedLink: URI?
    val client: MinecraftClient
    val width: Int
    val height: Int
    val textRenderer: TextRenderer
    fun init()
    fun insertText(text: String, override: Boolean)
    fun openLink(link: URI)
    fun removed()

    interface Static : StaticAccessor<Screen> {
        val ALLOWED_PROTOCOLS: Set<String>
        fun hasAltDown(): Boolean
        fun hasControlDown(): Boolean
        fun hasShiftDown(): Boolean
        fun isPaste(code: Int): Boolean
        fun isSelectAll(code: Int): Boolean
        fun isCopy(code: Int): Boolean
        fun isCut(code: Int): Boolean
    }

    companion object : Static by screenAccess.static()
}

val textRendererAccess = accessor<_, TextRenderer.Static>()

@Named("net/minecraft/client/font/TextRenderer")
interface TextRenderer {
    val fontHeight: Int
    fun isRightToLeft(): Boolean
    fun mirror(text: String): String
    fun trimToWidth(text: String, width: Int): String
    fun trimToWidth(text: String, width: Int, backwards: Boolean): String

    interface Static : StaticAccessor<TextRenderer>
    companion object : Static by textRendererAccess.static()
}

val entityRenderDispatcherAccess = accessor<_, EntityRenderDispatcher.Static>()

@Named("net/minecraft/client/render/entity/EntityRenderDispatcher")
interface EntityRenderDispatcher {
    val modelRenderers: Map<String, EntityRenderer<*>>
    var renderHitboxes: Boolean
    var renderShadows: Boolean
    val renderers: Map<EntityType<*>, EntityRenderer<*>>
    val textRenderer: TextRenderer
    val textureManager: TextureManager
    val world: World

    interface Static : StaticAccessor<EntityRenderDispatcher>
    companion object : Static by entityRenderDispatcherAccess.static()
}

val identifierAccess = accessor<_, Identifier.Static>()

@Named("net/minecraft/util/Identifier")
interface Identifier {
    val namespace: String
    val path: String

    interface Static : StaticAccessor<Identifier> {
        @ConstructorAccess
        fun construct(name: String): Identifier

        @ConstructorAccess
        fun construct(namespace: String, path: String): Identifier
    }

    companion object : Static by identifierAccess.static()
}

val textureManagerAccess = accessor<_, TextureManager.Static>()

@Named("net/minecraft/client/texture/TextureManager")
interface TextureManager {
    val textures: Map<Identifier, AbstractTexture>

    interface Static : StaticAccessor<TextureManager>
    companion object : Static by textureManagerAccess.static()
}

val abstractTextureAccess = accessor<_, AbstractTexture.Static>()

@Named("net/minecraft/client/texture/AbstractTexture")
interface AbstractTexture {
    val bilinear: Boolean
    val glId: Int
    val mipmap: Boolean
    fun clearGlId()
    fun setFilter(bilinear: Boolean, mipmap: Boolean)

    interface Static : StaticAccessor<AbstractTexture>
    companion object : Static by abstractTextureAccess.static()
}

val entityRendererAccess = accessor<_, EntityRenderer.Static>()

@Named("net/minecraft/client/render/entity/EntityRenderer")
interface EntityRenderer<T : Entity> {
    val dispatcher: EntityRenderDispatcher

    interface Static : StaticAccessor<EntityRenderer<*>>
    companion object : Static by entityRendererAccess.static()
}

val entityTypeAccess = accessor<_, EntityType.Static>()

@Named("net/minecraft/entity/EntityType")
interface EntityType<T : Entity> {
    interface Static : StaticAccessor<EntityType<*>>
    companion object : Static by entityTypeAccess.static()
}

val gameRendererAccess = accessor<_, GameRenderer.Static>()

@Named("net/minecraft/client/render/GameRenderer")
interface GameRenderer {
    val blockOutlineEnabled: Boolean
    val client: MinecraftClient
    val firstPersonRenderer: HeldItemRenderer
    val lastSkyDarkness: Float
    val lastWindowFocusedTime: Long
    val renderHand: Boolean
    val renderingPanorama: Boolean
    val skyDarkness: Float
    val ticks: Int
    val viewDistance: Float
    fun onResized(width: Int, height: Int)
    fun shouldRenderBlockOutline(): Boolean
    fun tick()
    fun updateTargetedEntity(tickDelta: Float)

    interface Static : StaticAccessor<GameRenderer>
    companion object : Static by gameRendererAccess.static()
}

val heldItemRendererAccess = accessor<_, HeldItemRenderer.Static>()

@Named("net/minecraft/client/render/item/HeldItemRenderer")
interface HeldItemRenderer {
    val client: MinecraftClient
    fun getMapAngle(tickDelta: Float): Float
    fun updateHeldItems()

    interface Static : StaticAccessor<HeldItemRenderer>
    companion object : Static by heldItemRendererAccess.static()
}

val inGameHudAccess = accessor<_, InGameHud.Static>()

@Named("net/minecraft/client/gui/hud/InGameHud")
interface InGameHud {
    val chatHud: ChatHud
    val client: MinecraftClient
    val debugHud: DebugHud
    val heartJumpEndTick: Long
    val heldItemTooltipFade: Int
    val itemRenderer: ItemRenderer
    val lastHealthCheckTime: Long
    val lastHealthValue: Int
    val overlayRemaining: Int
    val overlayTinted: Boolean
    val playerListHud: PlayerListHud
    val renderHealthValue: Int
    val spectatorHud: SpectatorHud
    val ticks: Int
    val titleFadeInTicks: Int
    val titleFadeOutTicks: Int
    val titleRemainTicks: Int
    val vignetteDarkness: Float
    fun resetDebugHudChunk()
    fun setDefaultTitleFade()
    fun tick()

    interface Static : StaticAccessor<InGameHud> {
        val PUMPKIN_BLUR: Identifier
    }

    companion object : Static by inGameHudAccess.static()
}

val chatHudAccess = accessor<_, ChatHud.Static>()

@Named("net/minecraft/client/gui/hud/ChatHud")
interface ChatHud {
    val client: MinecraftClient
    val hasUnreadNewMessages: Boolean
    val messageHistory: List<String>
    val messages: List<ChatHudLine>
    val scrolledLines: Int
    fun addToMessageHistory(message: String)
    fun isChatFocused(): Boolean
    fun reset()
    fun resetScroll()
    val width: Int
    val height: Int
    val visibleLineCount: Int

    interface Static : StaticAccessor<ChatHud>
    companion object : Static by chatHudAccess.static()
}

val chatHudLineAccess = accessor<_, ChatHudLine.Static>()

@Named("net/minecraft/client/gui/hud/ChatHudLine")
interface ChatHudLine {
    val creationTick: Int

    interface Static : StaticAccessor<ChatHudLine>
    companion object : Static by chatHudLineAccess.static()
}

val clientPlayerInteractionManagerAccess = accessor<_, ClientPlayerInteractionManager.Static>()

@Named("net/minecraft/client/network/ClientPlayerInteractionManager")
interface ClientPlayerInteractionManager {
    val blockBreakingCooldown: Int
    val blockBreakingSoundCooldown: Float
    val breakingBlock: Boolean
    val client: MinecraftClient
    val currentBreakingPos: BlockPos
    val currentBreakingProgress: Float
    val lastSelectedSlot: Int
    val networkHandler: ClientPlayNetworkHandler
    val selectedStack: ItemStack
    fun cancelBlockBreaking()
    fun hasCreativeInventory(): Boolean
    fun hasExperienceBar(): Boolean
    fun hasExtendedReach(): Boolean
    fun hasLimitedAttackSpeed(): Boolean
    fun hasRidingInventory(): Boolean
    fun hasStatusBars(): Boolean
    fun isBreakingBlock(): Boolean
    fun isFlyingLocked(): Boolean
    fun syncSelectedSlot()
    fun tick()
    val reachDistance: Float

    interface Static : StaticAccessor<ClientPlayerInteractionManager> {
        fun clickButton(syncId: Int, buttonId: Int)
    }

    companion object : Static by clientPlayerInteractionManagerAccess.static()
}

val vec3iAccess = accessor<_, Vec3i.Static>()

@Named("net/minecraft/util/math/Vec3i")
interface Vec3i {
    val x: Int
    val y: Int
    val z: Int

    interface Static : StaticAccessor<Vec3i> {
        val ZERO: Vec3i

        @ConstructorAccess
        fun construct(x: Int, y: Int, z: Int): Vec3i
    }

    companion object : Static by vec3iAccess.static()
}

val blockPosAccess = accessor<_, BlockPos.Static>()

@Named("net/minecraft/util/math/BlockPos")
interface BlockPos : Vec3i {
    fun asLong(): Long

    interface Static : StaticAccessor<BlockPos> {
        val BITS_X: Long
        val BITS_Y: Long
        val BITS_Z: Long
        val BIT_SHIFT_X: Int
        val BIT_SHIFT_Z: Int
        val ORIGIN: BlockPos
        val SIZE_BITS_X: Int
        val SIZE_BITS_Y: Int
        val SIZE_BITS_Z: Int

        @ConstructorAccess
        fun construct(x: Int, y: Int, z: Int): BlockPos
    }

    companion object : Static by blockPosAccess.static()
}

val clientPlayNetworkHandlerAccess = accessor<_, ClientPlayNetworkHandler.Static>()

@Named("net/minecraft/client/network/ClientPlayNetworkHandler")
interface ClientPlayNetworkHandler {
    val client: MinecraftClient
    val connection: ClientConnection
    val loginScreen: Screen
    val playerListEntries: Map<UUID, PlayerListEntry>
    val profile: GameProfile
    val world: ClientWorld
    fun clearWorld()
    val playerList: Collection<PlayerListEntry>

    interface Static : StaticAccessor<ClientPlayNetworkHandler>
    companion object : Static by clientPlayNetworkHandlerAccess.static()
}

val clientConnectionAccess = accessor<_, ClientConnection.Static>()

@Named("net/minecraft/network/ClientConnection")
interface ClientConnection {
    val address: SocketAddress

    //    val disconnectReason: Text
    val disconnected: Boolean
    val encrypted: Boolean
    val packetQueue: Queue<Any>
    val side: NetworkSide
    fun disableAutoRead()
    fun handleDisconnection()
    fun isEncrypted(): Boolean
    fun isLocal(): Boolean
    fun isOpen(): Boolean
    fun sendQueuedPackets()
    fun tick()

    interface Static : StaticAccessor<ClientConnection>
    companion object : Static by clientConnectionAccess.static()
}

val networkSideAccess = accessor<_, NetworkSide.Static>()

@Named("net/minecraft/network/NetworkSide")
interface NetworkSide {
    interface Static : StaticAccessor<NetworkSide> {
        val CLIENTBOUND: NetworkSide
        val SERVERBOUND: NetworkSide
    }

    companion object : Static by networkSideAccess.static()
}

val NetworkSide.opposite
    get() = if (this == NetworkSide.CLIENTBOUND) NetworkSide.SERVERBOUND else NetworkSide.CLIENTBOUND

val gameProfileAccess = accessor<_, GameProfile.Static>()

@Named("com/mojang/authlib/GameProfile")
interface GameProfile {
    val id: UUID?
    val name: String?
//    val properties: PropertyMap

    interface Static : StaticAccessor<GameProfile> {
        @ConstructorAccess
        fun construct(uuid: UUID, name: String): GameProfile
    }

    companion object : Static by gameProfileAccess.static()
}

val gameProfilePropertyAccess = accessor<_, GameProfileProperty.Static>()

@Named("com/mojang/authlib/properties/Property")
interface GameProfileProperty {
    val name: String
    val value: String
    val signature: String?

    interface Static : StaticAccessor<GameProfileProperty> {
        @ConstructorAccess
        fun construct(name: String, value: String, signature: String?)
    }

    companion object : Static by gameProfilePropertyAccess.static()
}

fun String.decodeBase64() = Base64.getDecoder().decode(encodeToByteArray())

fun GameProfileProperty.isSignatureValid(key: PublicKey): Boolean {
    return Signature.getInstance("SHA1withRSA").run {
        initVerify(key)
        update(value.encodeToByteArray())
        verify((signature ?: return true).decodeBase64())
    }
}

val playerListEntryAccess = accessor<_, PlayerListEntry.Static>()

@Named("net/minecraft/client/network/PlayerListEntry")
interface PlayerListEntry {
    //    val displayName: Text
    var latency: Int
    val model: String
    val profile: GameProfile
    val texturesLoaded: Boolean
    fun hasSkinTexture(): Boolean
    fun loadTextures()

    interface Static : StaticAccessor<PlayerListEntry>
    companion object : Static by playerListEntryAccess.static()
}

val clientWorldAccess = accessor<_, ClientWorld.Static>()

@Named("net/minecraft/client/world/ClientWorld")
interface ClientWorld : World {
    val client: MinecraftClient

    interface Static : StaticAccessor<ClientWorld>
    companion object : Static by clientWorldAccess.static()
}

val itemStackAccess = accessor<_, ItemStack.Static>()

@Named("net/minecraft/item/ItemStack")
interface ItemStack {
    val count: Int
    val item: Item
    fun hasCustomName(): Boolean
    fun hasEnchantments(): Boolean
    fun isDamageable(): Boolean
    fun isDamaged(): Boolean
    fun isEnchantable(): Boolean
    fun isStackable(): Boolean
    fun removeCustomName()
    var damage: Int
    val maxCount: Int
    val maxDamage: Int
    val maxUseTime: Int
    var repairCost: Int
    val translationKey: String

    interface Static : StaticAccessor<ItemStack> {
        val MODIFIER_FORMAT: DecimalFormat
    }

    companion object : Static by itemStackAccess.static()
}

val itemAccess = accessor<_, Item.Static>()

@Named("net/minecraft/item/Item")
interface Item {
    val maxCount: Int
    val maxDamage: Int
    val recipeRemainder: Item
    val translationKey: String
    fun isDamageable(): Boolean
    fun isFood(): Boolean
    fun isNetworkSynced(): Boolean
    val enchantability: Int

    interface Static : StaticAccessor<Item> {
        val BLOCK_ITEMS: Map<Block, Item>
    }

    companion object : Static by itemAccess.static()
}

val blockAccess = accessor<_, Block.Static>()

@Named("net/minecraft/block/Block")
interface Block {
    val translationKey: String

    interface Static : StaticAccessor<Block>
    companion object : Static by blockAccess.static()
}

val debugHudAccess = accessor<_, DebugHud.Static>()

@Named("net/minecraft/client/gui/hud/DebugHud")
interface DebugHud {
    val client: MinecraftClient
    fun getMetricsLineColor(value: Int, g: Int, y: Int, r: Int): Int
    fun interpolateColor(color1: Int, color2: Int, dt: Float): Int
    fun toMiB(bytes: Long): Long
    val leftText: List<String>
    val rightText: List<String>

    interface Static : StaticAccessor<DebugHud>
    companion object : Static by debugHudAccess.static()
}

val itemRendererAccess = accessor<_, ItemRenderer.Static>()

@Named("net/minecraft/client/render/item/ItemRenderer")
interface ItemRenderer {
    val models: ItemModels
    val textureManager: TextureManager

    interface Static : StaticAccessor<ItemRenderer>
    companion object : Static by itemRendererAccess.static()
}

val itemModelsAccess = accessor<_, ItemModels.Static>()

@Named("net/minecraft/client/render/item/ItemModels")
interface ItemModels {
    fun reloadModels()

    interface Static : StaticAccessor<ItemModels>
    companion object : Static by itemModelsAccess.static()
}

val playerListHudAccess = accessor<_, PlayerListHud.Static>()

@Named("net/minecraft/client/gui/hud/PlayerListHud")
interface PlayerListHud {
    val client: MinecraftClient

    //    val footer: Text
//    val header: Text
    val inGameHud: InGameHud
    val visible: Boolean
    fun clear()

    interface Static : StaticAccessor<PlayerListHud>
    companion object : Static by playerListHudAccess.static()
}

val spectatorHudAccess = accessor<_, SpectatorHud.Static>()

@Named("net/minecraft/client/gui/hud/SpectatorHud")
interface SpectatorHud {
    val client: MinecraftClient
    val lastInteractionTime: Long
    val spectatorMenu: SpectatorMenu
    fun isOpen(): Boolean
    fun selectSlot(slot: Int)
    fun useSelectedCommand()
    val spectatorMenuHeight: Float

    interface Static : StaticAccessor<SpectatorHud>
    companion object : Static by spectatorHudAccess.static()
}

val spectatorMenuAccess = accessor<_, SpectatorMenu.Static>()

@Named("net/minecraft/client/gui/hud/spectator/SpectatorMenu")
interface SpectatorMenu {
    val currentGroup: SpectatorMenuCommandGroup
    val page: Int
    val selectedSlot: Int
    fun close()
    fun useCommand(slot: Int)
    val commands: List<SpectatorMenuCommand>

    interface Static : StaticAccessor<SpectatorMenu> {
        val BLANK_COMMAND: SpectatorMenuCommand
        val CLOSE_COMMAND: SpectatorMenuCommand
        val DISABLED_NEXT_PAGE_COMMAND: SpectatorMenuCommand
        val NEXT_PAGE_COMMAND: SpectatorMenuCommand
        val PREVIOUS_PAGE_COMMAND: SpectatorMenuCommand
    }

    companion object : Static by spectatorMenuAccess.static()
}

val spectatorMenuCommandAccess = accessor<_, SpectatorMenuCommand.Static>()

@Named("net/minecraft/client/gui/hud/spectator/SpectatorMenuCommand")
interface SpectatorMenuCommand {
    fun isEnabled(): Boolean

    interface Static : StaticAccessor<SpectatorMenuCommand>
    companion object : Static by spectatorMenuCommandAccess.static()
}

val spectatorMenuCommandGroupAccess = accessor<_, SpectatorMenuCommandGroup.Static>()

@Named("net/minecraft/client/gui/hud/spectator/SpectatorMenuCommandGroup")
interface SpectatorMenuCommandGroup {
    val commands: List<SpectatorMenuCommand>

    interface Static : StaticAccessor<SpectatorMenuCommandGroup>
    companion object : Static by spectatorMenuCommandGroupAccess.static()
}

val languageManagerAccess = accessor<_, LanguageManager.Static>()

@Named("net/minecraft/client/resource/language/LanguageManager")
interface LanguageManager {
    val currentLanguageCode: String
    val languageDefs: Map<String, LanguageDefinition>

    interface Static : StaticAccessor<LanguageManager>
    companion object : Static by languageManagerAccess.static()
}

val languageDefinitionAccess = accessor<_, LanguageDefinition.Static>()

@Named("net/minecraft/client/resource/language/LanguageDefinition")
interface LanguageDefinition {
    val name: String
    val region: String
    val rightToLeft: Boolean

    interface Static : StaticAccessor<LanguageDefinition>
    companion object : Static by languageDefinitionAccess.static()
}

val metricsDataAccess = accessor<_, MetricsData.Static>()

@Named("net/minecraft/util/MetricsData")
interface MetricsData {
    val sampleCount: Int
    val samples: Array<Long>
    val startIndex: Int
    val writeIndex: Int
    fun pushSample(time: Long)
    fun wrapIndex(index: Int): Int
    val currentIndex: Int

    interface Static : StaticAccessor<MetricsData>
    companion object : Static by metricsDataAccess.static()
}

val musicTrackerAccess = accessor<_, MusicTracker.Static>()

@Named("net/minecraft/client/sound/MusicTracker")
interface MusicTracker {
    val timeUntilNextSong: Int

    interface Static : StaticAccessor<MusicTracker>
    companion object : Static by musicTrackerAccess.static()
}

val particleManagerAccess = accessor<_, ParticleManager.Static>()

@Named("net/minecraft/client/particle/ParticleManager")
interface ParticleManager {
    val textureManager: TextureManager
    fun registerDefaultFactories()
    fun tick()
    val debugString: String

    interface Static : StaticAccessor<ParticleManager>
    companion object : Static by particleManagerAccess.static()
}

val profilerAccess = accessor<_, Profiler.Static>()

@Named("net/minecraft/util/profiler/Profiler")
interface Profiler {
    fun pop()
    fun push(location: String)
    fun swap(location: String)

    interface Static : StaticAccessor<Profiler>
    companion object : Static by profilerAccess.static()
}

val integratedServerAccess = accessor<_, IntegratedServer.Static>()

@Named("net/minecraft/server/integrated/IntegratedServer")
interface IntegratedServer : MinecraftServer {
    val client: MinecraftClient
    val paused: Boolean

    interface Static : StaticAccessor<IntegratedServer>
    companion object : Static by integratedServerAccess.static()
}

val minecraftServerAccess = accessor<_, MinecraftServer.Static>()

@Named("net/minecraft/server/MinecraftServer")
interface MinecraftServer {
    var demo: Boolean
    var flightEnabled: Boolean
    val keyPair: KeyPair
    val lastPlayerSampleUpdate: Long
    val lastTickLengths: Array<Long>
    val loading: Boolean
    var motd: String
    val networkIo: ServerNetworkIo
    var onlineMode: Boolean
    var playerIdleTimeout: Int
    val playerManager: PlayerManager
    val profiler: Profiler
    val proxy: Proxy
    var pvpEnabled: Boolean
    val running: Boolean
    var serverIp: String
    var serverPort: Int
    val serverThread: Thread
    val stopped: Boolean
    val ticks: Int
    val timeReference: Long
    fun areCommandBlocksEnabled(): Boolean
    fun exit()
    fun forcePlayerSampleUpdate()
    fun getFile(name: String): File
    fun hasGui(): Boolean
    fun isDedicated(): Boolean
    fun isDemo(): Boolean
    fun isFlightEnabled(): Boolean
    fun isHardcore(): Boolean
    fun isLoading(): Boolean
    fun isMonsterSpawningEnabled(): Boolean
    fun isNetherAllowed(): Boolean
    fun isOnlineMode(): Boolean
    fun isPvpEnabled(): Boolean
    fun isRunning(): Boolean
    fun isStopped(): Boolean
    fun isUsingNativeTransport(): Boolean
    fun setupServer(): Boolean
    fun shouldBroadcastRconToOps(): Boolean
    fun shouldSpawnAnimals(): Boolean
    fun shouldSpawnNpcs(): Boolean
    val currentPlayerCount: Int
    val maxPlayerCount: Int
    val maxWorldBorderRadius: Int
    val networkCompressionThreshold: Int
    val opPermissionLevel: Int
    val playerNames: Array<String>
    val runDirectory: File
    val serverMotd: String
    val sessionService: MinecraftSessionService
    val spawnProtectionRadius: Int
    val version: String

    interface Static : StaticAccessor<MinecraftServer>
    companion object : Static by minecraftServerAccess.static()
}

val minecraftSessionServiceAccess = accessor<_, MinecraftSessionService.Static>()

@Named("com/mojang/authlib/minecraft/MinecraftSessionService")
interface MinecraftSessionService {
    fun joinServer(profile: GameProfile, token: String, hash: String)
    fun hasJoinedServer(profile: GameProfile, unknown: String): GameProfile
//    fun getTextures(profile: GameProfile, slim: Boolean)
//    fun fillProfileProperties(profile: GameProfile, load: Boolean)

    interface Static : StaticAccessor<MinecraftSessionService>
    companion object : Static by minecraftSessionServiceAccess.static()
}

val serverNetworkIoAccess = accessor<_, ServerNetworkIo.Static>()

@Named("net/minecraft/server/ServerNetworkIo")
interface ServerNetworkIo {
    val active: Boolean
    val connections: List<ClientConnection>
    val server: MinecraftServer
    fun bind(address: InetAddress, port: Int)
    fun bindLocal(): SocketAddress
    fun stop()
    fun tick()

    interface Static : StaticAccessor<ServerNetworkIo> {
        @ConstructorAccess
        fun construct(server: MinecraftServer)
    }

    companion object : Static by serverNetworkIoAccess.static()
}

val sessionAccess = accessor<_, Session.Static>()
val sessionAccountTypeAccess = accessor<_, Session.AccountType.Static>()

@Named("net/minecraft/client/util/Session")
interface Session {
    val accessToken: String
    val accountType: AccountType
    val username: String
    val uuid: String
    val profile: GameProfile
    val sessionId: String

    @Named("net/minecraft/client/util/Session\$AccountType")
    interface AccountType {
        val name: String

        interface Static : StaticAccessor<AccountType> {
            val BY_NAME: Map<String, AccountType>
            val LEGACY: AccountType
            val MOJANG: AccountType
        }

        companion object : Static by sessionAccountTypeAccess.static()
    }

    interface Static : StaticAccessor<Session>
    companion object : Static by sessionAccess.static()
}

val playerSkinProviderAccess = accessor<_, PlayerSkinProvider.Static>()

@Named("net/minecraft/client/texture/PlayerSkinProvider")
interface PlayerSkinProvider {
    val sessionService: MinecraftSessionService
    val skinCacheDir: File

    interface Static : StaticAccessor<PlayerSkinProvider>
    companion object : Static by playerSkinProviderAccess.static()
}

val soundManagerAccess = accessor<_, SoundManager.Static>()

@Named("net/minecraft/client/sound/SoundManager")
interface SoundManager {
    val soundSystem: SoundSystem
    fun close()
    fun pauseAll()
    fun resumeAll()
    fun stopAll()

    interface Static : StaticAccessor<SoundManager>
    companion object : Static by soundManagerAccess.static()
}

val soundSystemAccess = accessor<_, SoundSystem.Static>()

@Named("net/minecraft/client/sound/SoundSystem")
interface SoundSystem {
    val startTicks: Map<SoundInstance, Int>
    val started: Boolean
    val ticks: Int
    fun pauseAll()
    fun reloadSounds()
    fun resumeAll()
    fun start()
    fun stop()
    fun stopAll()
    fun tick()

    interface Static : StaticAccessor<SoundSystem>
    companion object : Static by soundSystemAccess.static()
}

val soundInstanceAccess = accessor<_, SoundInstance.Static>()

@Named("net/minecraft/client/sound/SoundInstance")
interface SoundInstance {
    fun isRepeatable(): Boolean
    val pitch: Float
    val repeatDelay: Int
    val volume: Float

    interface Static : StaticAccessor<SoundInstance>
    companion object : Static by soundInstanceAccess.static()
}

val worldRendererAccess = accessor<_, WorldRenderer.Static>()

@Named("net/minecraft/client/render/WorldRenderer")
interface WorldRenderer {
    val blockEntityCount: Int
    val cameraChunkX: Int
    val cameraChunkY: Int
    val cameraChunkZ: Int
    val chunkBuilder: ChunkBuilder
    val chunks: BuiltChunkStorage
    val client: MinecraftClient
    val entityRenderDispatcher: EntityRenderDispatcher
    val lastCameraChunkUpdateX: Double
    val lastCameraChunkUpdateY: Double
    val lastCameraChunkUpdateZ: Double
    val lastCameraPitch: Double
    val lastCameraX: Double
    val lastCameraY: Double
    val lastCameraYaw: Double
    val lastCameraZ: Double
    val lastTranslucentSortX: Double
    val lastTranslucentSortY: Double
    val lastTranslucentSortZ: Double
    val noCullingBlockEntities: Set<BlockEntity>
    val playingSongs: Map<BlockPos, SoundInstance>
    val ticks: Int
    val world: ClientWorld
    fun onResized(width: Int, height: Int)
    fun reload()
    fun renderDarkSky()
    fun renderLightSky()
    fun renderStars()
    fun scheduleTerrainUpdate()
    fun tick()
    fun updateNoCullingBlockEntities(removed: Collection<BlockEntity>, added: Collection<BlockEntity>)
    val chunksDebugString: String
    val entitiesDebugString: String

    interface Static : StaticAccessor<WorldRenderer> {
        val CLOUDS: Identifier
        val END_SKY: Identifier
        val FORCEFIELD: Identifier
        val MOON_PHASES: Identifier
        val SUN: Identifier
    }

    companion object : Static by worldRendererAccess.static()
}

val blockEntityAccess = accessor<_, BlockEntity.Static>()

@Named("net/minecraft/block/entity/BlockEntity")
interface BlockEntity {
    val pos: BlockPos
    val removed: Boolean
    val world: World
    fun cancelRemoval()
    fun hasWorld(): Boolean
    fun isRemoved(): Boolean
    fun markDirty()
    fun markRemoved()

    interface Static : StaticAccessor<BlockEntity>
    companion object : Static by blockEntityAccess.static()
}

val playerManagerAccess = accessor<_, PlayerManager.Static>()

@Named("net/minecraft/server/PlayerManager")
interface PlayerManager {
    val advancementTrackers: Map<UUID, PlayerAdvancementTracker>
    val bannedIps: BannedIpList
    val bannedProfiles: BannedPlayerList
    val cheatsAllowed: Boolean
    val latencyUpdateTimer: Int
    val maxPlayers: Int
    val ops: OperatorList
    val playerMap: Map<UUID, ServerPlayerEntity>
    val players: List<ServerPlayerEntity>
    val server: MinecraftServer
    val viewDistance: Int
    val whitelist: Whitelist
    val whitelistEnabled: Boolean
    fun canBypassPlayerLimit(profile: GameProfile): Boolean
    fun disconnectAllPlayers()
    fun getPlayersByIp(ip: String): List<ServerPlayerEntity>
    fun isOperator(profile: GameProfile): Boolean
    fun isWhitelistEnabled(): Boolean
    fun isWhitelisted(profile: GameProfile): Boolean
    fun reloadWhitelist()
    fun saveAllPlayerData()
    fun setCheatsAllowed(cheatsAllowed: Boolean)
    fun setViewDistance(viewDistance: Int)
    fun setWhitelistEnabled(whitelistEnabled: Boolean)
    fun updatePlayerLatency()
    val currentPlayerCount: Int
    val maxPlayerCount: Int
    val opNames: Array<String>
    val playerNames: Array<String>
    val whitelistedNames: Array<String>

    interface Static : StaticAccessor<PlayerManager> {
        val BANNED_IPS_FILE: File
        val BANNED_PLAYERS_FILE: File
        val DATE_FORMATTER: SimpleDateFormat
        val OPERATORS_FILE: File
        val WHITELIST_FILE: File
    }

    companion object : Static by playerManagerAccess.static()
}

val playerAdvancementTrackerAccess = accessor<_, PlayerAdvancementTracker.Static>()

@Named("net/minecraft/advancement/PlayerAdvancementTracker")
interface PlayerAdvancementTracker {
    val currentDisplayTab: Advancement
    val dirty: Boolean
    val owner: ServerPlayerEntity
    val progressUpdates: Set<Advancement>
    val visibleAdvancements: Set<Advancement>
    fun clearCriteria()
    fun save()

    interface Static : StaticAccessor<PlayerAdvancementTracker>
    companion object : Static by playerAdvancementTrackerAccess.static()
}

val advancementAccess = accessor<_, Advancement.Static>()

@Named("net/minecraft/advancement/Advancement")
interface Advancement {
    val children: Set<Advancement>
    val display: AdvancementDisplay
    val id: Identifier
    val parent: Advancement
    val requirements: Array<String>
    val rewards: AdvancementRewards
//    val text: Text
    val requirementCount: Int

    interface Static : StaticAccessor<Advancement>
    companion object : Static by advancementAccess.static()
}

val advancementDisplayAccess = accessor<_, AdvancementDisplay.Static>()

@Named("net/minecraft/advancement/AdvancementDisplay")
interface AdvancementDisplay {
//    val description: Text
//    val title: Text

    interface Static : StaticAccessor<AdvancementDisplay>
    companion object : Static by advancementDisplayAccess.static()
}

val advancementRewardsAccess = accessor<_, AdvancementRewards.Static>()

@Named("net/minecraft/advancement/AdvancementRewards")
interface AdvancementRewards {
    val experience: Int
    val loot: Array<Identifier>
    val recipes: Array<Identifier>

    interface Static : StaticAccessor<AdvancementRewards> {
        val NONE: AdvancementRewards
    }

    companion object : Static by advancementRewardsAccess.static()
}

val serverPlayerEntityAccess = accessor<_, ServerPlayerEntity.Static>()

@Named("net/minecraft/entity/player/ServerPlayerEntity")
interface ServerPlayerEntity : PlayerEntity {
    val chatColors: Boolean
    val interactionManager: ServerPlayerInteractionManager
    val killedEnderdragon: Boolean
    val language: String
    val lastActionTime: Long
    val lastHealth: Float
    val lastHungerLevel: Int
    val lastXp: Int
    val networkHandler: ServerPlayNetworkHandler
    val ping: Int
    val removedEntities: List<Int>
    val screenHandlerSyncId: Int
    val server: MinecraftServer
    val serverPosX: Double
    val serverPosZ: Double
    val skipPacketSlotUpdates: Boolean
    val spawnProtectionTicks: Int
    val spectatingEntity: Entity
    val statHandler: ServerStatHandler
    val syncedHealth: Float
    val wasHungry: Boolean
    fun closeOpenedScreenHandler()
    fun handleFall(distance: Double, arg1: Boolean)
    fun incrementSyncId()
    fun isPvpEnabled(): Boolean
    fun listenToScreenHandler()
    fun markHealthDirty()
    fun sendResourcePackUrl(url: String, hash: String)
    fun tickPlayer()
    fun updateLastActionTime()
    val ip: String

    interface Static : StaticAccessor<ServerPlayerEntity>
    companion object : Static by serverPlayerEntityAccess.static()
}

val chunkBuilderAccess = accessor<_, ChunkBuilder.Static>()

@Named("net/minecraft/client/world/ChunkBuilder")
interface ChunkBuilder {
//    val builtChunk: BuiltChunk // mostly unmapped
    val chunkAssemblyHelper: ChunkAssemblyHelper
    val lock: ReentrantLock
    val tasks: List<Runnable>

    interface Static : StaticAccessor<ChunkBuilder>
    companion object : Static by chunkBuilderAccess.static()
}

val builtChunkStorageAccess = accessor<_, BuiltChunkStorage.Static>()

@Named("net/minecraft/client/render/BuiltChunkStorage")
interface BuiltChunkStorage {
    val sizeX: Int
    val sizeY: Int
    val sizeZ: Int
    val world: World
    val worldRenderer: WorldRenderer
    fun clear()
    fun setViewDistance(viewDistance: Int)
    fun updateCameraPosition(x: Double, z: Double)

    interface Static : StaticAccessor<BuiltChunkStorage>
    companion object : Static by builtChunkStorageAccess.static()
}

val bannedIpListAccess = accessor<_, BannedIpList.Static>()

@Named("net/minecraft/server/BannedIpList")
interface BannedIpList {
    fun isBanned(ip: SocketAddress): Boolean
    fun stringifyAddress(address: SocketAddress): String

    interface Static : StaticAccessor<BannedIpList>
    companion object : Static by bannedIpListAccess.static()
}

val bannedPlayerListAccess = accessor<_, BannedPlayerList.Static>()

@Named("net/minecraft/server/BannedPlayerList")
interface BannedPlayerList {
    operator fun contains(profile: GameProfile): Boolean
    fun toString(profile: GameProfile): String

    interface Static : StaticAccessor<BannedPlayerList>
    companion object : Static by bannedPlayerListAccess.static()
}

val operatorListAccess = accessor<_, OperatorList.Static>()

@Named("net/minecraft/server/OperatorList")
interface OperatorList {
    fun toString(profile: GameProfile): String

    interface Static : StaticAccessor<OperatorList>
    companion object : Static by operatorListAccess.static()
}

val whitelistAccess = accessor<_, Whitelist.Static>()

@Named("net/minecraft/server/Whitelist")
interface Whitelist {
    fun isAllowed(profile: GameProfile): Boolean
    fun toString(profile: GameProfile): String

    interface Static : StaticAccessor<Whitelist>
    companion object : Static by whitelistAccess.static()
}

val playerEntityAccess = accessor<_, PlayerEntity.Static>()

@Named("net/minecraft/entity/player/PlayerEntity")
interface PlayerEntity : LivingEntity {
    val abilities: PlayerAbilities
    val abilityResyncCountdown: Int
    val capeX: Double
    val capeY: Double
    val capeZ: Double
    val enchantmentTableSeed: Int
    val experienceLevel: Int
    val experiencePickUpDelay: Int
    val experienceProgress: Float
    val fishHook: FishingBobberEntity
    val gameProfile: GameProfile
    val hungerManager: HungerManager
    val inventory: PlayerInventory
    val lastPlayedLevelUpSoundTime: Int
    val prevCapeX: Double
    val prevCapeY: Double
    val prevCapeZ: Double
    val prevStrideDistance: Float
    var reducedDebugInfo: Boolean
    val sleepTimer: Int
    val strideDistance: Float
    val totalExperience: Int
    fun addExhaustion(exhaustion: Float)
    fun addExperience(experience: Int)
    fun addScore(score: Int)
    fun canConsume(ignoreHunger: Boolean): Boolean
    fun canFoodHeal(): Boolean
    fun closeHandledScreen()
    fun isMainPlayer(): Boolean
    fun requestRespawn()
    fun sendAbilitiesUpdate()
    val nextLevelExperience: Int
    var score: Int

    interface Static : StaticAccessor<PlayerEntity>
    companion object : Static by playerEntityAccess.static()
}

val livingEntityAccess = accessor<_, LivingEntity.Static>()

@Named("net/minecraft/entity/LivingEntity")
interface LivingEntity : Entity {
    val absorptionAmount: Float
    val attacker: LivingEntity
    val attacking: LivingEntity
    val attackingPlayer: PlayerEntity
    val bodyTrackingIncrements: Int
    val bodyYaw: Float
    val damageTracker: DamageTracker
    val dead: Boolean
    val deathTime: Int
    val defaultMaxHealth: Int
    val despawnCounter: Int
    val effectsChanged: Boolean
    val forwardSpeed: Float
    val handSwingProgress: Float
    val handSwingTicks: Int
    val handSwinging: Boolean
    val headYaw: Float
    val hurtTime: Int
    var jumping: Boolean
    val jumpingCooldown: Int
    val lastAttackTime: Int
    val lastAttackedTime: Int
    val lastHandSwingProgress: Float
    val maxHurtTime: Int
    var movementSpeed: Float
    val playerHitTimer: Int
    val prevBodyYaw: Float
    val prevHeadYaw: Float
    val prevStepBobbingAmount: Float
    val randomLargeSeed: Float
    val randomSmallSeed: Float
    val serverPitch: Double
    val serverY: Double
    val serverYaw: Double
    val serverZ: Double
    val sidewaysSpeed: Float
    val stepBobbingAmount: Float
    fun dropXp()
    fun endCombat()
    fun enterCombat()
    fun getHandSwingProgress(tickDelta: Float): Float
    fun getNextAirUnderwater(air: Int): Int
    fun heal(amount: Float)
    fun isBaby(): Boolean
    fun isClimbing(): Boolean
    fun isSleeping(): Boolean
    fun jump()
    fun markEffectsDirty()
    fun shouldAlwaysDropXp(): Boolean
    fun tickCramming()
    fun tickHandSwing()
    fun tickMovement()
    fun tickNewAi()
    fun tickStatusEffects()
    fun turnHead(bodyRotation: Float, headRotation: Float): Float
    fun updatePotionVisibility()
    var health: Float
    val jumpVelocity: Float
    val maxHealth: Float
    val soundPitch: Float
    val soundVolume: Float

    interface Static : StaticAccessor<LivingEntity> {
        val SPRINTING_SPEED_BOOST_ID: UUID
    }

    companion object : Static by livingEntityAccess.static()
}

val damageTrackerAccess = accessor<_, DamageTracker.Static>()

@Named("net/minecraft/entity/damage/DamageTracker")
interface DamageTracker {
    val ageOnLastAttacked: Int
    val ageOnLastDamage: Int
    val ageOnLastUpdate: Int
    val entity: LivingEntity
    val hasDamage: Boolean
    val recentDamage: List<DamageRecord>
    val recentlyAttacked: Boolean
    fun update()
    val timeSinceLastAttack: Int

    interface Static : StaticAccessor<DamageTracker>
    companion object : Static by damageTrackerAccess.static()
}

val damageRecordAccess = accessor<_, DamageRecord.Static>()

@Named("net/minecraft/entity/damage/DamageRecord")
interface DamageRecord {
    val damage: Float
    val damageSource: DamageSource
    val fallDistance: Float

    interface Static : StaticAccessor<DamageRecord>
    companion object : Static by damageRecordAccess.static()
}

val damageSourceAccess = accessor<_, DamageSource.Static>()

@Named("net/minecraft/entity/damage/DamageSource")
interface DamageSource {
    fun isScaledWithDifficulty(): Boolean
    fun isSourceCreativePlayer(): Boolean
    val exhaustion: Float
    val name: String

    interface Static : StaticAccessor<DamageSource>
    companion object : Static by damageSourceAccess.static()
}

val serverPlayerInteractionManagerAccess = accessor<_, ServerPlayerInteractionManager.Static>()

@Named("net/minecraft/server/network/ServerPlayerInteractionManager")
interface ServerPlayerInteractionManager {
    val failedToMine: Boolean
    val mining: Boolean
    val miningPos: BlockPos
    val tickCounter: Int
    fun isCreative(): Boolean

    interface Static : StaticAccessor<ServerPlayerInteractionManager>
    companion object : Static by serverPlayerInteractionManagerAccess.static()
}

val serverStatHandlerAccess = accessor<_, ServerStatHandler.Static>()

@Named("net/minecraft/stat/ServerStatHandler")
interface ServerStatHandler {
    val file: File
    val server: MinecraftServer
    fun save()
    fun updateStatSet()

    interface Static : StaticAccessor<ServerStatHandler>
    companion object : Static by serverStatHandlerAccess.static()
}

val serverPlayNetworkHandlerAccess = accessor<_, ServerPlayNetworkHandler.Static>()

@Named("net/minecraft/server/network/ServerPlayNetworkHandler")
interface ServerPlayNetworkHandler {
    val connection: ClientConnection
    val creativeItemDropThreshold: Int
    val keepAliveId: Long
    val lastKeepAliveTime: Long
    val lastTickMovePacketsCount: Int
    val messageCooldown: Int
    val server: MinecraftServer
    fun requestTeleport(x: Double, y: Double, z: Double, yaw: Float, pitch: Float)

    interface Static : StaticAccessor<ServerPlayNetworkHandler>
    companion object : Static by serverPlayNetworkHandlerAccess.static()
}

val chunkAssemblyHelperAccess = accessor<_, ChunkAssemblyHelper.Static>()

@Named("net/minecraft/client/world/ChunkAssemblyHelper")
interface ChunkAssemblyHelper {
    val blockEntities: List<BlockEntity>
    val chunkOcclusionData: ChunkOcclusionData
    val unusedLayers: Array<Boolean>
    val usedLayers: Array<Boolean>

    interface Static : StaticAccessor<ChunkAssemblyHelper> {
        val UNSUPPORTED: ChunkAssemblyHelper
    }

    companion object : Static by chunkAssemblyHelperAccess.static()
}

val chunkOcclusionDataAccess = accessor<_, ChunkOcclusionData.Static>()

@Named("net/minecraft/client/render/chunk/ChunkOcclusionData")
interface ChunkOcclusionData {
    val visibility: BitSet
    fun addOpenEdgeFaces(faces: Set<Direction>)
    fun fill(visible: Boolean)

    interface Static : StaticAccessor<ChunkOcclusionData> {
        val DIRECTION_COUNT: Int
    }

    companion object : Static by chunkOcclusionDataAccess.static()
}

val directionAccess = accessor<_, Direction.Static>()
val directionAxisAccess = accessor<_, Direction.Axis.Static>()

@Named("net/minecraft/util/math/Direction")
interface Direction {
    val axis: Axis
    val id: Int
    val idHorizontal: Int
    val idOpposite: Int
    val horizontal: Int
    val offsetX: Int
    val offsetY: Int
    val offsetZ: Int

    interface Static : StaticAccessor<Direction> {
        val ALL: Array<Direction>
        val DOWN: Direction
        val EAST: Direction
        val HORIZONTAL: Array<Direction>
        val NORTH: Direction
        val SOUTH: Direction
        val UP: Direction
        val WEST: Direction
    }

    @Named("net/minecraft/util/math/Direction\$Axis")
    interface Axis {
        val name: String
        fun isHorizontal(): Boolean
        fun isVertical(): Boolean

        interface Static : StaticAccessor<Axis> {
            val X: Axis
            val Y: Axis
            val Z: Axis
            val name: String
        }

        companion object : Static by directionAxisAccess.static()
    }

    companion object : Static by directionAccess.static()
}

val playerAbilitiesAccess = accessor<_, PlayerAbilities.Static>()

@Named("net/minecraft/entity/player/PlayerAbilities")
interface PlayerAbilities {
    val allowFlying: Boolean
    val allowModifyWorld: Boolean
    val creativeMode: Boolean
    var flySpeed: Float
    val flying: Boolean
    val invulnerable: Boolean
    var walkSpeed: Float

    interface Static : StaticAccessor<PlayerAbilities>
    companion object : Static by playerAbilitiesAccess.static()
}

val projectileEntityAccess = accessor<_, ProjectileEntity.Static>()

@Named("net/minecraft/entity/projectile/ProjectileEntity")
interface ProjectileEntity {
    val ownerUuid: UUID
    fun setVelocity(x: Double, y: Double, z: Double, speed: Float, divergence: Float)

    interface Static : StaticAccessor<ProjectileEntity> {
        fun updateRotation(yaw: Float, pitch: Float): Float
    }

    companion object : Static by projectileEntityAccess.static()
}

val fishingBobberEntityAccess = accessor<_, FishingBobberEntity.Static>()

@Named("net/minecraft/entity/projectile/FishingBobberEntity")
interface FishingBobberEntity : ProjectileEntity {
    val fishAngle: Float
    val fishTravelCountdown: Int
    val hookCountdown: Int
    val removalTimer: Int
    val waitCountdown: Int

    interface Static : StaticAccessor<FishingBobberEntity>
    companion object : Static by fishingBobberEntityAccess.static()
}

val hungerManagerAccess = accessor<_, HungerManager.Static>()

@Named("net/minecraft/entity/player/HungerManager")
interface HungerManager {
    val exhaustion: Float
    var foodLevel: Int
    val prevFoodLevel: Int
    fun add(food: Int, saturationModifier: Float)
    fun addExhaustion(exhaustion: Float)
    fun isNotFull(): Boolean
    val saturationLevel: Float

    interface Static : StaticAccessor<HungerManager>
    companion object : Static by hungerManagerAccess.static()
}

val playerInventoryAccess = accessor<_, PlayerInventory.Static>()

@Named("net/minecraft/entity/player/PlayerInventory")
interface PlayerInventory {
    val player: PlayerEntity
    val selectedSlot: Int
    fun dropAll()
    fun updateItems()
    val emptySlot: Int
    val hotbarSize: Int

    interface Static : StaticAccessor<PlayerInventory>
    companion object : Static by playerInventoryAccess.static()
}

val inventoryAccess = accessor<_, Inventory.Static>()

@Named("net/minecraft/inventory/Inventory")
interface Inventory {
    // will add support for inventories manually later
    fun markDirty()

    interface Static : StaticAccessor<Inventory>
    companion object : Static by inventoryAccess.static()
}

val textComponent = findNamedClass("net/minecraft/text/Text")
val textComponentSerializer = findNamedClass("net/minecraft/text/Text\$Serializer") {
    methods {
        "create" {
            arguments hasExact listOf(asmTypeOf<String>())
            method.isStatic()
            method match { it.method.returnType.internalName.endsWith("Text") }
        }
    }
}

val textComponentAccess by globalAccessorRegistry.accessor<_, TextComponent.Static>(textComponent)
val tcsAccess by globalAccessorRegistry.accessor<_, TextComponent.Serializer.Static>(textComponentSerializer)

interface TextComponent : InstanceAccessor {
    interface Serializer : InstanceAccessor {
        interface Static : com.grappenmaker.jvmutil.StaticAccessor<Serializer> {
            fun create(json: String): TextComponent
        }

        companion object : Static by tcsAccess.static
    }

    interface Static : com.grappenmaker.jvmutil.StaticAccessor<TextComponent>
    companion object : Static by textComponentAccess.static
}