package win.rainchan.simplegroupauth

import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isOperator

object PluginSwitchCommand : SimpleCommand(
    owner = PluginMain,
    "ga-switch",
    description = "启用或禁用SimpleGroupAuth"
) {
    @Handler
    suspend fun CommandSender.onCommand(group: Group?, mode: Int) {
        var groupId = group?.id
        if (group == null && this is MemberCommandSender){
            if (this.user.isOperator()){
                groupId = this.group.id
            }else{
                sendMessage("权限不足")
                return
            }
        }
        if (groupId == null){
            sendMessage("请输入群号")
            return
        }
        val mode = ConfigData.AuthType.values().getOrNull(mode)
        if (mode == null){
            sendMessage("模式不存在")
            return
        }
         ConfigData.enabledGroup[groupId] = mode
        sendMessage("成功将验证模式设置为${mode.name}")
    }
}
