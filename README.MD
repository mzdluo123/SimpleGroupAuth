# MAPluginTemplate

Mirai-console的插件模板，同时支持MiraiAndroid；一次编写，两处使用！

# 使用

## 更改Android应用信息

请到`app\src\main\AndroidManifest.xml`和`app/build.gradle`按照注释更改信息

请在``

## 编写代码

请在`bot`模块下编写你的代码，例如`bot\src\main\java\com\example\maPluginTemplate\PluginMain.kt`

如果你想要更改包名称，需要同时更改`bot\src\main\resources\META-INF\services\net.mamoe.mirai.console.plugin.jvm.JvmPlugin`中的信息

## 测试

本模板已内置mirai-console和MiraiAndroid快捷启动方式，请使用idea打开

## 发布

运行`buildPlugin`task即可编译jvm平台下的jar文件，文件可在`bot\build\libs\bot.jar`找到

请在菜单内找到生成签名的apk文件来发布Android插件