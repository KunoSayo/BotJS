package com.sifsstudio.botjs.item

import com.sifsstudio.botjs.BotJS
import com.sifsstudio.botjs.env.api.ability.*
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import thedarkcolour.kotlinforforge.forge.registerObject

object Items {
    val REGISTRY: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, BotJS.ID)

    val TAB = object : CreativeModeTab("botjs") {
        override fun makeIcon(): ItemStack {
            return WRENCH.defaultInstance
        }
    }

    val TIMING_UPGRADE: Item
            by REGISTRY.registerObject("timing_upgrade") { UpgradeItem.withAbility { TimingAbility(it) } }

    val MOVEMENT_UPGRADE: Item
            by REGISTRY.registerObject("movement_upgrade") { UpgradeItem.withAbility { MovementAbility(it) } }

    val OUTPUT_UPGRADE: Item
            by REGISTRY.registerObject("output_upgrade") { UpgradeItem.withAbility { OutputAbility(it) } }

    val INTERACTION_UPGRADE: Item
            by REGISTRY.registerObject("interaction_upgrade") { UpgradeItem.withAbility { InteractionAbility(it) } }

    val SENSING_UPGRADE: Item
            by REGISTRY.registerObject("sensing_upgrade") { UpgradeItem.withAbility { SensingAbility(it) } }

    val WRENCH: Item
            by REGISTRY.registerObject("wrench") {
                Item(Item.Properties().stacksTo(1).tab(TAB))
            }

    val PROGRAMMER: Item
            by REGISTRY.registerObject("programmer") {
                Item(Item.Properties().stacksTo(1).tab(TAB))
            }

    val SWITCH: Item
            by REGISTRY.registerObject("switch") {
                Item(Item.Properties().stacksTo(1).tab(TAB))
            }
}