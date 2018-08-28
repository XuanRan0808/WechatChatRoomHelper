package com.zdy.project.wechat_chatroom_helper.helper.ui.config

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.annotation.ColorRes
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.Constants
import com.zdy.project.wechat_chatroom_helper.LogUtils
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo
import com.zdy.project.wechat_chatroom_helper.wechat.WXClassParser
import dalvik.system.DexClassLoader
import manager.PermissionHelper
import me.omico.base.activity.SetupWizardBaseActivity
import net.dongliu.apk.parser.ApkFile
import com.zdy.project.wechat_chatroom_helper.helper.utils.WechatJsonUtils
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils
import java.io.File
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class ConfigActivity : SetupWizardBaseActivity(), View.OnClickListener {
    override fun onClick(v: View) {
        when (v.id) {
            R.id.config_step2_button1 -> {
                val check = PermissionHelper.check(this)
                when (check) {
                    PermissionHelper.ALLOW -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.material_deep_teal_500)
                        setNavigationBarNextButtonEnabled(true)
                    }
                    PermissionHelper.ASK -> PermissionHelper.requestPermission(this)
                    PermissionHelper.DENY -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color_material)
                        setNavigationBarNextButtonEnabled(false)
                    }
                }
            }
            else -> {
            }
        }
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            if (setupStep == PAGE_WRITE_AND_READ_FILE) {
                val check = PermissionHelper.check(this)
                when (check) {
                    PermissionHelper.ALLOW -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_success, R.color.material_deep_teal_500)
                        setNavigationBarNextButtonEnabled(true)
                    }
                    PermissionHelper.DENY -> {
                        initColorTextView(R.id.config_step2_text1, R.string.config_permission_fail, R.color.error_color_material)
                        setNavigationBarNextButtonEnabled(false)
                    }
                }
            }
        }
    }


    private val PAGE_WELCOME = 0
    private val PAGE_WRITE_AND_READ_FILE = 1
    private val PAGE_WRITE_CONFIG = 2

    private val TEXT_CHANGE_LINE = 1
    private val TEXT_ADDITION = 2

    private var setupStep: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupStep = intent.getIntExtra("EXTRA_SETUP_STEP", 0)
    }

    override fun initLayout(viewGroup: ViewGroup) {
        when (setupStep) {

            PAGE_WELCOME -> {
                navigationBar.backButton.visibility = View.GONE
                initLayout(viewGroup, R.layout.config_layout_step_1, R.string.config_step1_title, true)
            }
            PAGE_WRITE_AND_READ_FILE -> {
                initLayout(viewGroup, R.layout.config_layout_step_2, R.string.config_step2_title, false)
                findViewById<View>(R.id.config_step2_button1).setOnClickListener(this)
            }
            PAGE_WRITE_CONFIG -> {
                initLayout(viewGroup, R.layout.config_layout_step_3, R.string.config_step3_title, false)
                parseApkClasses()
            }
        }
    }


    override fun onNavigateBack() {
        onBackPressed()
    }

    override fun onNavigateNext() {

        when (setupStep) {
            PAGE_WELCOME, PAGE_WRITE_AND_READ_FILE -> {
                intentNextStep()
            }
            PAGE_WRITE_CONFIG -> {

            }
        }
    }


    private fun intentNextStep() {
        val intent = Intent(this, ConfigActivity::class.java)
        intent.putExtra("EXTRA_SETUP_STEP", setupStep + 1)
        startActivity(intent)
    }


    private fun initLayout(viewGroup: ViewGroup, @LayoutRes layout: Int, @StringRes title: Int, nextButtonEnable: Boolean) {
        val inflate = LayoutInflater.from(this).inflate(layout, viewGroup, false)
        viewGroup.addView(inflate)
        setupWizardLayout.setHeaderText(title)
        setupWizardLayout.headerTextView.setTextColor(ContextCompat.getColor(this, R.color.white_100))
        setNavigationBarNextButtonEnabled(nextButtonEnable)
    }


    private fun initColorTextView(@IdRes id: Int, @StringRes text: Int, @ColorRes color: Int) {
        val textView = findViewById<TextView>(id)
        textView.setText(text)
        textView.setTextColor(ContextCompat.getColor(this, color))
    }


    private var parseThread: Thread? = null

    private lateinit var textHandler: TextHandler

    private var configHashMap = hashMapOf<String, String>()


    override fun setContentView(layoutResID: Int) {
        super.setContentView(R.layout.activity_guide)
    }

    private fun parseApkClasses() {

        val configTextView = findViewById<TextView>(R.id.config_step3_text1)


        textHandler = TextHandler(configTextView)

        if (parseThread != null) {
            if (parseThread!!.isAlive) {
                parseThread!!.interrupt()
            }
            parseThread = null
        }

        parseThread = thread {

            val random = Random()
            var currentRandomInt = 1
            val classes = mutableListOf<Class<*>>()

            try {
                val publicSourceDir = this.packageManager.getApplicationInfo(Constants.WECHAT_PACKAGE_NAME, 0).publicSourceDir
                val apkFile = ApkFile(File(publicSourceDir))


                writeScrollText(TEXT_ADDITION, getString(R.string.config_step3_text1),
                        publicSourceDir, apkFile.apkMeta.versionName, apkFile.apkMeta.versionCode.toString())
                writeScrollText(TEXT_ADDITION, getString(R.string.config_step3_text2))

                val dexClasses = apkFile.dexClasses

                val optimizedDirectory = getDir("dex", 0).absolutePath
                val classLoader = DexClassLoader(publicSourceDir, optimizedDirectory, null, classLoader)

                dexClasses.map { it.classType.substring(1, it.classType.length - 1).replace("/", ".") }
                        .filter { it.contains(Constants.WECHAT_PACKAGE_NAME) }
                        .forEachIndexed { index, className ->

                            try {
                                val clazz = classLoader.loadClass(className)
                                classes.add(clazz)

                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }

                            if (index == currentRandomInt) {
                                currentRandomInt += random.nextInt(1000)
                                textHandler.sendMessage(Message.obtain(textHandler, 1, index + 1, classes.size))

                            }
                        }

                configHashMap["conversationWithCacheAdapter"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithCacheAdapter(classes))
                configHashMap["conversationWithAppBrandListView"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationWithAppBrandListView(classes))
                configHashMap["conversationAvatar"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationAvatar(classes))
                configHashMap["conversationClickListener"] = parseAnnotatedElementToName(WXClassParser.Adapter.getConversationClickListener(classes))
                configHashMap["logcat"] = parseAnnotatedElementToName(WXClassParser.PlatformTool.getLogcat(classes))

                writeNewConfig()

                writeScrollText(TEXT_ADDITION, getString(R.string.config_step3_text3),
                        WechatJsonUtils.configPath, apkFile.apkMeta.versionName, apkFile.apkMeta.versionCode.toString())
                setNavigationBarNextButtonEnabled(true)

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    @Throws(Exception::class)
    private fun parseAnnotatedElementToName(element: AnnotatedElement?): String {
        return if (element == null) throw ClassNotFoundException()
        else {
            writeScrollText(TEXT_ADDITION, getString(R.string.config_step3_text4), element)

            when (element) {
                is Method -> element.name
                is Class<*> -> element.name
                else -> ""
            }
        }
    }

    private fun writeNewConfig() {
        WechatJsonUtils.getFileString()
        configHashMap.forEach { k, v ->
            AppSaveInfo.addConfigItem(k, v)
            writeScrollText(TEXT_ADDITION, getString(R.string.config_step3_text5), k, v)
        }
    }


    private fun writeScrollText(type: Int, text: String, vararg args: Any) {
        textHandler.sendMessage(Message.obtain(textHandler, type,
                String.format(Locale.CHINESE, text, *args)))
    }


    class TextHandler(private var configTextView: TextView) : Handler() {

        override fun handleMessage(msg: Message) {

            val context = configTextView.context

            val time = SimpleDateFormat("HH:mm:ss", Locale.CHINESE).format(Calendar.getInstance().time)

            when (msg.what) {
                1 -> {
                    if (configTextView.tag == null) {
                        configTextView.tag = configTextView.text
                    }
                    configTextView.text =
                            String.format(Locale.CHINESE, context.getString(R.string.config_step3_text_ex1), time, msg.arg1, msg.arg2, configTextView.tag)
                }

                2 -> {
                    configTextView.text =
                            String.format(Locale.CHINESE, context.getString(R.string.config_step3_text_ex2), time, msg.obj as String, configTextView.text.toString())
                }
            }


        }
    }
}