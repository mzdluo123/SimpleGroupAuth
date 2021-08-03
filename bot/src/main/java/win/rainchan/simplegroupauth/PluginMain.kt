package win.rainchan.simplegroupauth

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.registerTo
import net.mamoe.mirai.utils.info


object PluginMain : KotlinPlugin(
    JvmPluginDescription(
        id = "win.rainchan.simplegroupauth",
        name = "SimpleGroupAuth",
        version = "0.1.0"
    )
) {
    override fun onEnable() {
        ConfigData.reload()
        PluginSwitchCommand.register()
        EventListener().registerTo(GlobalEventChannel)
        logger.info("SimpleGroupAuth已启动")
    }

}
