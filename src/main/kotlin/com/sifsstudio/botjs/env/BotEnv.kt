package com.sifsstudio.botjs.env

import com.sifsstudio.botjs.entity.BotEntity
import com.sifsstudio.botjs.env.ability.Ability
import com.sifsstudio.botjs.env.ability.TimeKillingAbility
import com.sifsstudio.botjs.env.api.Bot
import com.sifsstudio.botjs.env.task.Task
import com.sifsstudio.botjs.env.task.TaskFuture
import dev.latvian.mods.rhino.Context
import dev.latvian.mods.rhino.ScriptableObject
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
import java.util.Collections
import kotlin.collections.HashSet
import kotlin.reflect.KClass

class BotEnv(val entity: BotEntity): Runnable {
    private val context: Context = Context.enter()!!
    private val tasks: MutableMap<Task<*>, Boolean> = Collections.synchronizedMap(Object2BooleanOpenHashMap())
    private val abilities: MutableSet<Ability> = HashSet()
    private var active = false
    var script: String = ""
    private var available = false

    init {
        install {TimeKillingAbility(it)}
    }

    fun<T: Task<*>> pending(tsk: T) =
        synchronized(this) {
            if(available && active) {
                tasks[tsk] = false
                tsk.future
            } else TaskFuture.failedFuture()
        }

    private inline fun install(cap: (BotEnv) -> Ability) = abilities.add(cap(this))

    fun uninstall(cap: KClass<out Ability>) = abilities.removeIf(cap::isInstance)

    private fun createRoot(): ScriptableObject = context.initStandardObjects().apply {
        defineProperty("bot", Bot(abilities), ScriptableObject.CONST)
    }

    override fun run() {
        check(available && !active)
        active = true
        val root = createRoot()
        context.evaluateString(root, script, "bot_script", 1, null)
    }

    fun tick() = synchronized(this) {
        if(!active || !available) return
        tasks.iterator().run {
            var entry: MutableMap.MutableEntry<Task<*>, Boolean>
            var task: Task<*>
            while(hasNext()) {
                entry = next()
                task = entry.key
                if(!entry.value) {
                    if(!task.accepts(this@BotEnv)){
                        remove()
                    } else entry.setValue(true)
                } else {
                    task.tick()
                    if (task.future.done) {
                        remove()
                    }
                }
            }
        }
    }

    fun discard() = synchronized(this){
        available = false
        tasks.clear()
    }

    fun enable() {
        available = true
    }
}