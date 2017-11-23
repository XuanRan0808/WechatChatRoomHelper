package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.zdy.project.wechat_chatroom_helper.model.MessageEntity;
import com.zdy.project.wechat_chatroom_helper.network.ApiManager;
import com.zdy.project.wechat_chatroom_helper.ui.MySwipeBackLayout;
import com.zdy.project.wechat_chatroom_helper.utils.DeviceUtils;
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Arrow;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Setting;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public class ChatRoomView2 implements ChatRoomContract.View {


    private ChatRoomContract.Presenter mPresenter;

    private Context mContext;
    private AbsoluteLayout mContainer;


    //   private MySwipeBackLayout swipeBackLayout;

    private LinearLayout mainView;
    private RecyclerView mRecyclerView;
    private ViewGroup mToolbarContainer;
    private Toolbar mToolbar;

    private ChatRoomRecyclerViewAdapter mAdapter;


    private boolean isInAnim = false;
    private boolean isDragging = false;

    private String uuid = "0";
    private String title;

    ChatRoomView2(Context context, final ViewGroup container, String title) {

        this.mContainer = (AbsoluteLayout) container;
        this.mContext = context;
        this.title = title;

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

        //  mContainer.addView(swipeBackLayout, params);
        mContainer.addView(((View) o), params);

//        swipeBackLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                dismiss();
//                swipeBackLayout.mSlideOffset = 1;
//            }
//        });

        uuid = DeviceUtils.getIMELCode(context);
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }


    Object o;

    private void initSwipeBack() {
        //    swipeBackLayout = new MySwipeBackLayout(mContext);
        //   swipeBackLayout.attachToView(mainView, mContext);


        Class<?> aClass = XposedHelpers.findClass("com.tencent.mm.ui.widget.SwipeBackLayout", HookLogic.mClassLoader);

        Constructor c1 = null;
        try {
            c1 = aClass.getDeclaredConstructor(Context.class);
            o = c1.newInstance(mContext);

            ((ViewGroup) o).addView(mainView);
            XposedHelpers.callMethod(o, "init");
        } catch (NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void setOnDialogItemClickListener(ChatRoomRecyclerViewAdapter.OnDialogItemClickListener listener) {
        mAdapter.setOnDialogItemClickListener(listener);
    }

    @Override
    public boolean isShowing() {

        //   XposedBridge.log("dispatchKeyEvent, ChatRoomView2.isShowing = " + !swipeBackLayout.isOpen());

        //   return !swipeBackLayout.isOpen();

        return true;
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
        //   XposedBridge.log("ChatRoomView2, show");
        // swipeBackLayout.closePane();
        ApiManager.getINSTANCE().sendRequestForUserStatistics("open", uuid, Build.MODEL);
    }

    @Override
    public void dismiss(int offest) {

        //    XposedBridge.log("ChatRoomView2, dismiss");
        //   swipeBackLayout.openPane();
        ApiManager.getINSTANCE().sendRequestForUserStatistics("close", uuid, Build.MODEL);
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
                ArrayList data = mAdapter.getData();
                for (int i = 0; i < data.size(); i++) {
                    Object item = data.get(i);
                    MessageEntity entity = new MessageEntity(item);
                    if (entity.field_username.equals(targetUserName)) {

                        Object object = HookLogic.getMessageBeanForOriginIndex(mPresenter.getOriginAdapter(),
                                mAdapter.getMuteListInAdapterPositions().get(i));

                        data.set(i, object);
                        mAdapter.setData(data);
                        mAdapter.notifyItemChanged(i);
                    }
                }
            }
        });
    }


    @Override
    public void showMessageRefresh(ArrayList<Integer> muteListInAdapterPositions) {
        ArrayList<Object> data = new ArrayList<Object>();
        for (Integer muteListInAdapterPosition : muteListInAdapterPositions) {
            Object object = HookLogic.getMessageBeanForOriginIndex(mPresenter.getOriginAdapter(),
                    muteListInAdapterPosition);
            data.add(object);
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
        mToolbar.setBackgroundColor(Color.parseColor("#" + PreferencesUtils.getToolBarColor()));


        mToolbar.setTitle(title);
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
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.zdy.project.wechat_chatroom_helper",
                        "kt.MainActivity");
                intent.setComponent(cn);
                mContext.startActivity(intent);
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
