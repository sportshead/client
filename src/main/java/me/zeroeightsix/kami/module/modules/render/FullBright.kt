package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.SafeTickEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.event.listener
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.max
import kotlin.math.min

@Module.Info(
        name = "FullBright",
        description = "Makes everything brighter!",
        category = Module.Category.RENDER,
        alwaysListening = true
)
object FullBright : Module() {
    private val gamma = setting("Gamma", 12.0f, 5.0f..15.0f, 0.5f)
    private val transitionLength = setting("TransitionLength", 3.0f, 0.0f..10.0f, 0.5f)
    private val oldValue = setting("OldValue", 1.0f, 0.0f..1.0f, 0.1f, { false })

    private var gammaSetting: Float
        get() = mc.gameSettings.gammaSetting
        set(gammaIn) {
            mc.gameSettings.gammaSetting = gammaIn
        }

    override fun onEnable() {
        oldValue.value = mc.gameSettings.gammaSetting
    }

    init {
        listener<SafeTickEvent> {
            if (it.phase != TickEvent.Phase.START) return@listener
            when {
                isEnabled -> {
                    transition(gamma.value)
                    alwaysListening = true
                }

                isDisabled && gammaSetting != oldValue.value -> {
                    transition(oldValue.value)
                }

                else -> {
                    alwaysListening = false
                }
            }
        }
    }

    private fun transition(target: Float) {
        gammaSetting = when {
            gammaSetting !in 0f..15f -> target

            gammaSetting == target -> return

            gammaSetting < target -> min(gammaSetting + getTransitionAmount(), target)

            else -> max(gammaSetting - getTransitionAmount(), target)
        }
    }

    private fun getTransitionAmount(): Float {
        if (transitionLength.value == 0f) return 15f
        return (1f / transitionLength.value / 20f) * (gamma.value - oldValue.value)
    }
}