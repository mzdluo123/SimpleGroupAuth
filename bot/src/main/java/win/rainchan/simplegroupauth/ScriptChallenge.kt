package win.rainchan.simplegroupauth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.console.plugin.info
import okhttp3.FormBody
import okhttp3.Request
import java.io.File
import java.nio.file.Path
import java.util.*


object ScriptChallenge {
    fun init() {
        val scriptPath = PluginMain.dataFolder.resolve("scripts")
        if (!scriptPath.exists()) {
            scriptPath.mkdir()
            writeDefault(scriptPath.resolve("default.py"))
        }
    }

    fun writeDefault(filePath: File) {
        filePath.writeText(
            """# 本插件支持使用python脚本检查输出是否正确
# #开头的注释将会作为提示信息发送给验证者
# 代码运行使用 https://www.dooccn.com/python3/ 支持联网
# 请将代码文件重命名为 群号.py 并设置验证方式来使用
# 验证成功请输出1，失败请输出0
# 这里的默认答案为123
qq = int(input())
answer = input()

if  answer == "123":
    print(1)
else:
    print(0)    
        """.trimIndent()
        )
    }

    fun getQuestion(groupId: Long): String? {
        val file = PluginMain.dataFolder.resolve("scripts").resolve("${groupId}.py")
        if (!file.exists()) {
            return null
        }
        return file.readLines().asSequence().filter { s -> s.startsWith("#") }
            .map { it.substring(1) }.joinToString("\n")
    }

    fun encodeCode(groupId: Long): String {
        val file = PluginMain.dataFolder.resolve("scripts").resolve("${groupId}.py")
        return Base64.getEncoder().encodeToString(file.readBytes())
    }

    fun hasScript(groupId: Long): Boolean {
        val file = PluginMain.dataFolder.resolve("scripts").resolve("${groupId}.py")
        if (!file.exists()) {
            return false
        }
        return true
    }

    suspend fun auth(groupId: Long, userId: Long, answer: String): Boolean {
        delay((0..1000L).random())
        val req = Request.Builder().post(
            FormBody.Builder()
                .add("language", "20")
                .add("code", encodeCode(groupId))
                .add("stdin", "${userId}\n${answer}").build()
        ).url("https://runcode-api2-ng.dooccn.com/compile2")
            .addHeader("Origin", " https://www.dooccn.com")
            .addHeader("Referer", "https://www.dooccn.com/")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36 Edg/92.0.902.55"
            )
            .build()
        val rsp = withContext(Dispatchers.IO) {
            HttpUtils.client.newCall(req).execute()
        }
        if (!rsp.isSuccessful) {
            PluginMain.logger.error("脚本验证失败 ${rsp.message}")
        }
        val body = rsp.body?.string()
        rsp.close()

        val jsonData = body?.let { Json.parseToJsonElement(it) }
        PluginMain.logger.info("脚本验证返回结果 $jsonData")
        val output = jsonData?.jsonObject?.get("output")?.jsonPrimitive?.content ?: ""
        val result = output.split("\n")[0].toIntOrNull()
        return result == 1
    }
}
