package win.rainchan.simplegroupauth

import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.data.PluginDataExtensions
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.util.concurrent.ConcurrentHashMap

class EventListener : SimpleListenerHost() {
    private val needAuth = ConcurrentHashMap<Long, HashSet<Long>>()  // 每个群需要验证的人
    // private val captchaCookie = ConcurrentHashMap<Long, Cookie>()

    @EventHandler
    suspend fun GroupMessageEvent.onMsg() {
//        if (this.message.contentToString() == "test"){
//            val groupId = group.id
//            if (sender is NormalMember){
//                val accountSet = needAuth.getOrDefault(groupId, HashSet())
//                accountSet.add(sender.id)
//                needAuth[groupId] = accountSet
//                launch { captchaSession( sender as NormalMember) }
//            }
//        }
    }

    @EventHandler
    suspend fun BotInvitedJoinGroupRequestEvent.invite() {

    }

    @EventHandler
    suspend fun MemberJoinRequestEvent.onRequest() {

    }

    @EventHandler
    suspend fun MemberJoinEvent.onJoin() {

        when (ConfigData.authType(group)) {
            ConfigData.AuthType.ENTERNED_CAPTCHA -> {
                delay(2000)
                val accountSet = needAuth.getOrDefault(groupId, HashSet())
                accountSet.add(member.id)
                needAuth[groupId] = accountSet
                launch { captchaSession(member) }
            }

        }
    }

    suspend fun captchaSession( member: NormalMember) {
        val group = member.group
        val groupId = group.id
        group.sendMessage(At(member) + PlainText("欢迎来到本群，为保障良好的聊天环境，请在120秒内输入以下验证码。验证码不区分大小写"))
        val rsp = HttpUtils.getCaptCha()
        val cookie = rsp.headers("Set-Cookie")
        if (!rsp.isSuccessful || cookie.isEmpty()) {
            group.sendMessage("网络错误 ${rsp.message}")
            return
        }
        var timeoutJob:Job? = null
        var  listener:Listener<GroupMessageEvent>? = null
        var tries = 0
        val img = rsp.body!!.bytes().toExternalResource()
        group.sendMessage(At(member) + img.uploadAsImage(group))
        img.close()
        rsp.close()

        listener = GlobalEventChannel.subscribeAlways<GroupMessageEvent> {
            val thisMember = this.sender
            if (thisMember !is NormalMember){
                return@subscribeAlways
            }
            if (!needAuth[groupId]?.contains(thisMember.id)!!){
                return@subscribeAlways
            }
            if (thisMember == member) {
                val code = message.contentToString().trim()
                val result = HttpUtils.verifyCaptcha(cookie, code)
                if (result) {
                    group.sendMessage(member.at() + PlainText("您已通过验证！"))
                    needAuth[groupId]?.remove(member.id)
                    timeoutJob?.cancel()
                    listener?.complete()
                    return@subscribeAlways
                }
                tries ++
                if (tries >=3){
                    group.sendMessage(member.at() + PlainText("您未通过验证，请重新加群"))
                    needAuth[groupId]?.remove(member.id)
                    launch {
                        delay(5000)
                        member.kick("请重试")
                    }
                    timeoutJob?.cancel()
                    listener?.complete()
                    return@subscribeAlways
                }
                group.sendMessage(member.at() + PlainText("验证码错误，您还有${3-tries}次机会"))

            }
        }
        timeoutJob = launch {
            delay(120*1000)
            if (needAuth[groupId]?.contains(member.id) == true){
                group.sendMessage(member.at() + PlainText("验证超时，请重新加群"))
                needAuth[groupId]?.remove(member.id)
                member.kick("请重试")
            }

        }

    }
}
