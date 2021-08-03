package win.rainchan.simplegroupauth

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.contact.Group

object ConfigData:AutoSavePluginData("config") {
    public enum class AuthType{
        DISABLED,
        ENTERNED_CAPTCHA,
        ENTERNED_CHALLENGE,
        AUTO_ACCEPT,
        AUTO_ACCEPT_DENY
    }
    val enabledGroup:MutableMap<Long,AuthType> by value()

    val challengeFile:MutableMap<Long,String> by value()

    fun authType(group:Group): AuthType {
        return enabledGroup[group.id] ?: return AuthType.DISABLED
    }
}
