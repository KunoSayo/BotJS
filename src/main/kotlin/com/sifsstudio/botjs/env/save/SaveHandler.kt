package com.sifsstudio.botjs.env.save

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.env.*
import com.sifsstudio.botjs.util.ThreadLoop
import com.sifsstudio.botjs.util.describe
import com.sifsstudio.botjs.util.pick
import com.sifsstudio.botjs.util.submit
import kotlinx.coroutines.future.asCompletableFuture
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.event.server.ServerStoppingEvent
import java.util.concurrent.CompletableFuture

object SaveHandler {

    private var savingHandle: ThreadLoop.DisposableHandle? = null

    private val waitingHandle = mutableSetOf<ThreadLoop.DisposableHandle>()

    private var processSave = true

    enum class Marker {
        UNLOAD,
        SAVE,
    }

    var tick: Int = 0

    private fun save(): Boolean {
        val future = CompletableFuture<Unit>()
        describe(future) {
            globalSafeSuspend {
                runSafepoint(SafepointEvent.SAVE)
            }
            BotEnvGlobal.ALL_ENV.values.pick { !it.controller.loaded }.forEach {}
        }
        return true
    }

    private fun unload(dim: ResourceKey<Level>): Boolean {
        val future = CompletableFuture<Unit>()
        describe(future) {
            globalSafeSuspend {
                filterNot { it.entity.level.dimension() == dim }
                runSafepoint(SafepointEvent.SAVE)
            }
            ThreadLoop.Main.execute {
                BotEnvGlobal.ALL_ENV.values
                    .pick {!it.controller.loaded && it.controller.runState.get() == BotEnvState.READY}
                    .map { it.controller.scheduleWrite().asCompletableFuture() }
                    .forEach { it.join() }
            }
        }
        return true
    }

    private fun fetchHandle(m: Marker): ThreadLoop.DisposableHandle? {
        val res = waitingHandle.find { it.markers.contains(m) }
        if(res != null && !res.done) {
            return res
        }
        val now = savingHandle
        if(now != null && !now.done) {
            if(m in now.markers) {
                return now
            }
        }
        return null
    }

    private fun addHandle(it: ThreadLoop.DisposableHandle) {
        val handle = savingHandle
        if(handle != null && !handle.done) {
            waitingHandle += it
        } else savingHandle = it
    }

    fun onStop(e: ServerStoppingEvent) {
        processSave = false
        globalSafeSuspend {
            unloadAll()
        }
        BotEnvGlobal.ALL_ENV.values.forEach {
            while(it.controller.runState.get() != BotEnvState.READY) {
                Thread.onSpinWait()
            }
        }
        ThreadLoop.Sync.submit {
            BotEnvGlobal.ALL_ENV.values.forEach {
                it.controller.scheduleWrite()
            }
        }
    }

    fun onStart(e: ServerStartedEvent) {
        processSave = true
    }

    fun onSave(e: LevelEvent.Save) {
        if(processSave) {
            with(ThreadLoop.Sync) {
                execute {
                    val unloadRef = { unload((e.level as ServerLevel).dimension()) }
                    if (fetchHandle(Marker.UNLOAD) == null) {
                        addHandle(schedule(unloadRef).apply { markers += Marker.UNLOAD })
                    }
                }
            }
        }
    }

    fun onTick(e: TickEvent) {
        if(tick++ > BotJS.CONFIG.saveDurationTicks) {
            tick = 1
            with(ThreadLoop.Sync) {
                execute {
                    if (fetchHandle(Marker.SAVE) == null) {
                        addHandle(schedule(::save).apply {
                            markers += Marker.SAVE
                        })
                    }
                }
            }
        }
    }
}