package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

import com.zdy.project.wechat_chatroom_helper.manager.Type;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.util.ArrayList;

/**
 * Created by Mr.Zdy on 2017/8/25.
 */

public class ChatRoomViewPresenter implements ChatRoomContract.Presenter {

    private Context mContext;

    private Object mAdapter;

    private ChatRoomContract.View mView;

    private ViewGroup chatRoomView;

    public ChatRoomViewPresenter(Context context, Type type) {
        mContext = context;

        chatRoomView = new AbsoluteLayout(context);
        chatRoomView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        chatRoomView.setPadding(0, ScreenUtils.getStatusHeight(context),
                0, ScreenUtils.getNavigationBarHeight(context));

        mView = new ChatRoomView(context, chatRoomView, type);

        mView.setPresenter(this);
    }


    public void setOnDialogItemClickListener(ChatRoomRecyclerViewAdapter.OnDialogItemClickListener listener) {
        mView.setOnDialogItemClickListener(listener);
    }

    public void setAdapter(Object mAdapter) {
        this.mAdapter = mAdapter;
    }


    public void setListInAdapterPositions(ArrayList<Integer> listInAdapterPositions) {
        mView.showMessageRefresh(listInAdapterPositions);
    }

    @Override
    public void setMessageRefresh(String targetUserName) {
        mView.showMessageRefresh(targetUserName);
    }

    @Override
    public ViewGroup getPresenterView() {
        return chatRoomView;
    }

    public boolean isShowing() {
        return mView.isShowing();
    }

    public void show() {
        mView.show();
    }

    public void dismiss() {
        mView.dismiss();
    }

    @Override
    public void start() {
        mView.init();
    }

    @Override
    public Object getOriginAdapter() {
        return mAdapter;
    }



}
