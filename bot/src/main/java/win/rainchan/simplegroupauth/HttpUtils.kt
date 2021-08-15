package win.rainchan.simplegroupauth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object HttpUtils {
     val client = OkHttpClient.Builder().build()
    suspend fun getCaptCha(): Response {
        val req = Request.Builder().get().url("https://mail.sina.com.cn/cgi-bin/imgcode.php").build()
        return withContext(Dispatchers.IO) {
            client.newCall(req).execute()
        }
    }

    suspend fun verifyCaptcha(cookie: String,code:String):Boolean{
        val req = Request.Builder().post(
           FormBody.Builder().add("phonenumber","15874523695") // random data
               .add("email","fgsj842376tysd@sina.com")
               .add("imgvcode",code)
               .build()
        ).url("https://mail.sina.com.cn/cgi-bin/RegPhoneCode.php")
            .addHeader("cookie",cookie)
            .addHeader("referer","https://mail.sina.com.cn/register/regmail.php")
            .addHeader("origin","https://mail.sina.com.cn")
            .addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36 Edg/92.0.902.55")
            .build()
        val rsp = withContext(Dispatchers.IO){
            client.newCall(req).execute()
        }
        if (rsp.isSuccessful){
            val json  = Json.parseToJsonElement(rsp.body!!.string())
            val rspCode = json.jsonObject["code"]?.jsonPrimitive?.int
            if (rspCode == null){
                PluginMain.logger.error("校验验证码时出现问题 $json")
                return false
            }
            if (rspCode != -102){
                PluginMain.logger.info("验证成功")
                return true
            }
            PluginMain.logger.info("验证失败")
            return false

        }
        PluginMain.logger.error("校验验证码时出现问题 ${rsp.message}")
        return false
    }
}
