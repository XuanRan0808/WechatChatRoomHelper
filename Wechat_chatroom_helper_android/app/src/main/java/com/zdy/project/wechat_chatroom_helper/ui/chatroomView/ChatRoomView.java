package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.zdy.project.wechat_chatroom_helper.HookLogic;
import com.zdy.project.wechat_chatroom_helper.manager.Type;
import com.zdy.project.wechat_chatroom_helper.model.ChatInfoModel;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.ConfigChatRoomDialog;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.WhiteListDialog;
import com.zdy.project.wechat_chatroom_helper.ui.wechat.chatroomView.ChatRoomRecyclerViewAdapter;
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

import cn.bingoogolapple.swipebacklayout.MySwipeBackLayout;
import de.robv.android.xposed.XposedHelpers;
import network.ApiManager;
import utils.AppSaveInfoUtils;

import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Arrow;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Setting;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public class ChatRoomView implements ChatRoomContract.View {


    private ChatRoomContract.Presenter mPresenter;

    private Context mContext;
    private AbsoluteLayout mContainer;


    private MySwipeBackLayout swipeBackLayout;

    private LinearLayout mainView;
    private RecyclerView mRecyclerView;
    private ViewGroup mToolbarContainer;
    private Toolbar mToolbar;

    private ChatRoomRecyclerViewAdapter mAdapter;


    private boolean isInAnim = false;
    private boolean isDragging = false;

    private String uuid = "0";
    private Type type;

    public ChatRoomView(Context context, final ViewGroup container, Type type) {

        this.mContainer = (AbsoluteLayout) container;
        this.mContext = context;
        this.type = type;

        int width = ScreenUtils.getScreenWidth(mContext);
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width, ViewGroup.LayoutParams
                .MATCH_PARENT, 0, 0);


        mainView = new LinearLayout(mContext);
        mainView.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(mContext),
                ViewGroup.LayoutParams.MATCH_PARENT));
        mainView.setOrientation(LinearLayout.VERTICAL);

        mRecyclerView = new RecyclerView(mContext);
        mRecyclerView.setId(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return !isDragging;
            }
        });

        mainView.addView(initToolbar());
        mainView.addView(mRecyclerView);
        mainView.setClickable(true);

        mainView.setBackground(new ColorDrawable(0xFFFFFFFF));

        initSwipeBack();

        mContainer.addView(swipeBackLayout, params);

        swipeBackLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeBackLayout.mSlideOffset = 1;
                Log.v("dispatchKeyEvent", " isShowing, mSlideOffset = " + swipeBackLayout.mSlideOffset);
            }
        });
        swipeBackLayout.mSlideOffset = 1;

        uuid = DeviceUtils.getIMELCode(context);

        ApiManager.INSTANCE.sendRequestForUserStatistics("init", uuid, Build.MODEL);
    }


    private void initSwipeBack() {
        swipeBackLayout = new MySwipeBackLayout(mContext);
        swipeBackLayout.attachToView(mainView, mContext);
    }


    @Override
    public void setOnDialogItemClickListener(ChatRoomRecyclerViewAdapter.OnDialogItemClickListener listener) {
        mAdapter.setOnDialogItemClickListener(listener);
    }

    @Override
    public boolean isShowing() {
        return !swipeBackLayout.isOpen();
    }


    @Override
    public void show() {
        show(ScreenUtils.getScreenWidth(mContext));
    }

    @Override
    public void dismiss() {
        dismiss(0);
    }

    @Override
    public void show(int offest) {
        swipeBackLayout.closePane();
        //  ApiManager.INSTANCE.sendRequestForUserStatistics("open", uuid, Build.MODEL);
    }

    @Override
    public void dismiss(int offest) {
        swipeBackLayout.openPane();
//        ApiManager.INSTANCE.sendRequestForUserStatistics("close", uuid, Build.MODEL);
    }


    public boolean isInAnim() {
        return isInAnim;
    }

    @Override
    public void init() {
        mAdapter = new ChatRoomRecyclerViewAdapter(mContext, mPresenter.getOriginAdapter());
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void showMessageRefresh(final String targetUserName) {
        new Handler(mContext.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ArrayList<ChatInfoModel> data = mAdapter.getData();
                for (int i = 0; i < data.size(); i++) {
                    ChatInfoModel item = data.get(i);
                    if (Objects.equals(item.getAvatarString(), targetUserName)) {
                        Object object = HookLogic.getMessageBeanForOriginIndex(mPresenter.getOriginAdapter(),
                                mAdapter.getMuteListInAdapterPositions().get(i));

                        data.set(i, ChatInfoModel.Companion.convertFromObject(object, mPresenter.getOriginAdapter(), mContext));
                        mAdapter.setData(data);
                        mAdapter.notifyItemChanged(i);
                    }
                }
            }
        });
    }


    @Override
    public void showMessageRefresh(ArrayList<Integer> muteListInAdapterPositions) {
        ArrayList<ChatInfoModel> data = new ArrayList<>();
        for (Integer muteListInAdapterPosition : muteListInAdapterPositions) {
            Object object = HookLogic.getMessageBeanForOriginIndex(mPresenter.getOriginAdapter(),
                    muteListInAdapterPosition);

            data.add(ChatInfoModel.Companion.convertFromObject(object, mPresenter.getOriginAdapter(), mContext));
        }

        mAdapter.setMuteListInAdapterPositions(muteListInAdapterPositions);
        mAdapter.setData(data);

        mAdapter.notifyDataSetChanged();
    }


    private View initToolbar() {
        mToolbarContainer = new RelativeLayout(mContext);

        mToolbar = new Toolbar(mContext);

        int height = ScreenUtils.dip2px(mContext, 48);

        mToolbar.setLayoutParams(
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        mToolbar.setNavigationIcon(mContext.getResources()
                .getIdentifier(Drawable_String_Arrow, "drawable", mContext.getPackageName()));

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mToolbar.setBackgroundColor(Color.parseColor("#" + AppSaveInfoUtils.INSTANCE.toolbarColorInfo()));
        mRecyclerView.setBackgroundColor(Color.parseColor("#" + AppSaveInfoUtils.INSTANCE.helperColorInfo()));


        switch (type) {
            case CHAT_ROOMS:
                mToolbar.setTitle("群消息助手");
                break;
            case OFFICIAL:
                mToolbar.setTitle("公众号助手");
                break;
        }
        mToolbar.setTitleTextColor(0xFFFAFAFA);

        Class<?> clazz;
        try {
            clazz = Class.forName("android.widget.Toolbar");
            Field mTitleTextView = clazz.getDeclaredField("mTitleTextView");
            mTitleTextView.setAccessible(true);
            TextView textView = (TextView) mTitleTextView.get(mToolbar);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

            Field mNavButtonView = clazz.getDeclaredField("mNavButtonView");
            mNavButtonView.setAccessible(true);
            ImageButton imageButton = (ImageButton) mNavButtonView.get(mToolbar);
            ViewGroup.LayoutParams layoutParams = imageButton.getLayoutParams();
            layoutParams.height = height;
            imageButton.setLayoutParams(layoutParams);

        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        ImageView imageView = new ImageView(mContext);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        imageView.setLayoutParams(params);
        imageView.setPadding(height / 5, height / 5, height / 5, height / 5);
        imageView.setImageResource(mContext.getResources().
                getIdentifier(Drawable_String_Setting, "drawable", mContext.getPackageName()));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case OFFICIAL:
                        WhiteListDialog dialog = new WhiteListDialog(mContext);
                        dialog.setList(HookLogic.officialNickNameEntries);
                        dialog.setType(Type.OFFICIAL);
                        dialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                XposedHelpers.callMethod(mPresenter.getOriginAdapter(), "notifyDataSetChanged");
                            }
                        });
                        dialog.show();
                        break;
                    case CHAT_ROOMS:
                        ConfigChatRoomDialog configChatRoomDialog = new ConfigChatRoomDialog(mContext);
                        configChatRoomDialog.setOnModeChangedListener(new ConfigChatRoomDialog.OnModeChangedListener() {
                            @Override
                            public void onChanged() {
                                XposedHelpers.callMethod(mPresenter.getOriginAdapter(), "notifyDataSetChanged");
                            }
                        });
                        configChatRoomDialog.setOnWhiteListClickListener(new ConfigChatRoomDialog.OnWhiteListClickListener() {
                            @Override
                            public void onClick() {
                                ChatRoomRecyclerViewAdapter adapter = (ChatRoomRecyclerViewAdapter) mRecyclerView.getAdapter();

                                ArrayList<ChatInfoModel> data = adapter.getData();

                                ArrayList<String> list = new ArrayList<>();
                                for (int i = 0; i < data.size(); i++) {
                                    list.add(String.valueOf(data.get(i).getNickname()));
                                }

                                WhiteListDialog dialog = new WhiteListDialog(mContext);

                                if (AppSaveInfoUtils.INSTANCE.chatRoomTypeInfo().equals("1"))
                                    dialog.setList(HookLogic.allChatRoomNickNameEntries);
                                else dialog.setList(HookLogic.muteChatRoomNickNameEntries);

                                dialog.setType(Type.CHAT_ROOMS);
                                dialog.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        XposedHelpers.callMethod(mPresenter.getOriginAdapter(), "notifyDataSetChanged");
                                    }
                                });
                                dialog.show();

                            }
                        });
                        configChatRoomDialog.show();

                        break;


                }
            }
        });

        imageView.getDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        mToolbarContainer.addView(mToolbar);
        mToolbarContainer.addView(imageView);

        return mToolbarContainer;
    }

    @Override
    public void setPresenter(ChatRoomContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
