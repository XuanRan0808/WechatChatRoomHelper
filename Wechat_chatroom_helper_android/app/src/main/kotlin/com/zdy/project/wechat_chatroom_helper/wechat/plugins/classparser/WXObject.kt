package com.zdy.project.wechat_chatroom_helper.wechat.plugins.classparser

object WXObject {


    object Message {

        object C {
            const val SQLiteDatabase = "com.tencent.wcdb.database.SQLiteDatabase"
            const val SQLiteDatabaseCursorFactory = "com.tencent.wcdb.database.SQLiteDatabase\$CursorFactory"
            const val SQLiteErrorHandler = "com.tencent.wcdb.DatabaseErrorHandler"
            const val SQLiteCancellationSignal = "com.tencent.wcdb.support.CancellationSignal"
        }

        object M {
            const val QUERY = "rawQueryWithFactory"
            const val INSERT = "insertWithOnConflict"
            const val UPDATE = "updateWithOnConflict"
        }
    }


    object MainUI {

        object M {
            const val DispatchKeyEventOfLauncherUI = "dispatchKeyEvent"
            const val OnCreate = "onCreate"
            const val StartChattingOfLauncherUI = "startChatting"
            const val CloseChattingOfLauncherUI = "closeChatting"
        }

        object C {
            const val LauncherUI = "com.tencent.mm.ui.LauncherUI"
            const val FitSystemWindowLayoutView = "com.tencent.mm.ui.HomeUI\$FitSystemWindowLayoutView"
        }

    }


    object Adapter {

        object M {
            const val SetAdapter = "setAdapter"
            const val GetCount = "getCount"
            const val OnItemClick = "onItemClick"
            const val OnItemLongClick = "onItemLongClick"

            const val GetView = "getView"

        }

        object C {
            var ConversationWithCacheAdapter = ""
            var ConversationWithAppBrandListView = ""
            var ConversationAvatar = ""
            var ConversationClickListener = ""
            var ConversationLongClickListener = ""
            const val NoMeasuredTextView = "com.tencent.mm.ui.base.NoMeasuredTextView"
        }

    }



    object Tool {

        object C {

            var Logcat = ""
        }

    }


}