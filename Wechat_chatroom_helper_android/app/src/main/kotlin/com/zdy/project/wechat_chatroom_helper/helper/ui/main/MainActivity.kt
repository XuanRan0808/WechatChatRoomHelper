package ui

import android.content.*
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.zdy.project.wechat_chatroom_helper.R
import com.zdy.project.wechat_chatroom_helper.helper.ui.BaseActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.QuestionActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.config.ConfigActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.functionsetting.FunctionSettingActivity
import com.zdy.project.wechat_chatroom_helper.helper.ui.uisetting.UISettingActivity
import com.zdy.project.wechat_chatroom_helper.io.WechatJsonUtils
import manager.PermissionHelper
import com.zdy.project.wechat_chatroom_helper.io.AppSaveInfo


class MainActivity : BaseActivity() {


    private lateinit var listContent: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle(R.string.app_name)

        //加載佈局
        setContentView(R.layout.activity_main)
        listContent = findViewById(R.id.list_content)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            when (PermissionHelper.check(this)) {
                PermissionHelper.ALLOW -> {
                    //加載可配置項的佈局
                    WechatJsonUtils.init(this)
                    initSetting(arrayOf("群消息助手状态",
                            getString(R.string.title_function_setting_string),
                            getString(R.string.title_ui_setting_string),
                            getString(R.string.title_question_string),
                            getString(R.string.title_other_setting_string),
                            getString(R.string.title_about_string)))
                }
                PermissionHelper.ASK -> {
                    initSetting(arrayOf("群消息助手状态"))
                }
                PermissionHelper.DENY -> {
                    initSetting(arrayOf("群消息助手状态"))
                }
            }
        }

    }

    private fun initSetting(array: Array<String>) {

        listContent.removeAllViews()

        repeat(array.size) { index ->

            title = array[index]

            val itemView = LayoutInflater.from(thisActivity).inflate(R.layout.layout_setting_item, listContent, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)
            val switch = itemView.findViewById<SwitchCompat>(android.R.id.button1)

            text1.text = title

            itemView.setOnClickListener { switch.performClick() }

            when (title) {
                "群消息助手状态" -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, ConfigActivity::class.java))
                    }

                    when (PermissionHelper.check(thisActivity)) {
                        PermissionHelper.ALLOW -> {
                            if (AppSaveInfo.hasSuitWechatDataInfo()) {
                                val saveWechatVersionInfo = AppSaveInfo.wechatVersionInfo()
                                val currentWechatVersionInfo = MyApplication.get().getWechatVersionCode().toString()
                                if (saveWechatVersionInfo == currentWechatVersionInfo) {
                                    setSuccessText(text2, "本地适配文件适用于 $saveWechatVersionInfo 版本微信，已适配。")
                                } else {
                                    setFailText(text2, "本地适配文件适用于 $saveWechatVersionInfo 版本微信，当前微信版本：$currentWechatVersionInfo， 点击获取新的适配文件。")
                                }
                            } else {
                                setFailText(text2, "本地未发现适配文件， 点击获取新的适配文件。")
                            }
                        }
                        PermissionHelper.ASK -> {
                            setWarmText(text2, "未获得外部存储存储权限，点击获取并创建新的适配文件。")
                        }
                        PermissionHelper.DENY -> {
                            setFailText(text2, "您已经拒绝了我们的权限授予，点击手动授予权限。")
                        }
                    }
                }

                getString(R.string.title_question_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, QuestionActivity::class.java))
                    }
                    text2.setText(R.string.sub_title_question_string)
                }

                getString(R.string.title_ui_setting_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, UISettingActivity::class.java))
                    }
                    text2.setText(R.string.sub_title_ui_setting_string)
                }

                getString(R.string.title_function_setting_string) -> {
                    itemView.setOnClickListener {
                        thisActivity.startActivity(Intent(thisActivity, FunctionSettingActivity::class.java))
                    }
                    text2.setText(R.string.sub_title_function_setting_string)
                }

            }

            listContent.addView(itemView)
        }

    }

    private fun setWarmText(view: TextView, msg: String) {
        view.text = msg
        view.setTextColor(ContextCompat.getColor(thisActivity, R.color.warm_color))
    }

    private fun setFailText(view: TextView, msg: String) {
        view.text = msg
        view.setTextColor(ContextCompat.getColor(thisActivity, R.color.error_color))
    }

    private fun setSuccessText(view: TextView, msg: String) {
        view.text = msg
        view.setTextColor(ContextCompat.getColor(thisActivity, R.color.right_color))
    }


}