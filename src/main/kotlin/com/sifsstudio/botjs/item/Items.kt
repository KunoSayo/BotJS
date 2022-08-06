package com.sifsstudio.botjs.item

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.env.ability.WaitAbility
import net.minecraft.world.item.Item
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.registerObject

object Items {
    val REGISTRY: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, BotJS.ID)

    val INTERRUPT_UPGRADE: Item
        by REGISTRY.registerObject("interrupt_upgrade") { UpgradeItem.withAbility(WaitAbility()) }

}