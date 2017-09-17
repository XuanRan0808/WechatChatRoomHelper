package com.zdy.project.wechat_chatroom_helper.ui.chatroomView;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.MotionEvent;
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
import com.zdy.project.wechat_chatroom_helper.ui.MySwipeBackLayout;
import com.zdy.project.wechat_chatroom_helper.utils.PreferencesUtils;
import com.zdy.project.wechat_chatroom_helper.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Arrow;
import static com.zdy.project.wechat_chatroom_helper.Constants.Drawable_String_Setting;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_Status_Bean;
import static com.zdy.project.wechat_chatroom_helper.Constants.Method_Message_True_Content;
import static com.zdy.project.wechat_chatroom_helper.Constants.Value_Message_True_Content_Params;

/**
 * Created by Mr.Zdy on 2017/8/27.
 */

public class ChatRoomView2 implements ChatRoomContract.View {


    private ChatRoomContract.Presenter mPresenter;

    private Context mContext;
    private AbsoluteLayout mContainer;

    //  private LinearLayout contentView;
    //  private View maskView;

    private MySwipeBackLayout swipeBackLayout;

    private LinearLayout mainView;
    private RecyclerView mRecyclerView;
    private ViewGroup mToolbarContainer;
    private Toolbar mToolbar;

    private ChatRoomRecyclerViewAdapter mAdapter;


    private boolean isInAnim = false;
    private boolean isDragging = false;


    ChatRoomView2(Context context, final ViewGroup container) {
        this.mContainer = (AbsoluteLayout) container;
        this.mContext = context;

        int width = ScreenUtils.dip2px(mContext, 16) + ScreenUtils.getScreenWidth(mContext);
        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(width, ViewGroup.LayoutParams
                .MATCH_PARENT, 0, 0);

//        maskView = new View(mContext);
//        maskView.setLayoutParams(new ViewGroup.LayoutParams(ScreenUtils.dip2px(mContext, 16),
//                ViewGroup.LayoutParams.MATCH_PARENT));
//        maskView.setBackground(new GradientDrawable(
//                GradientDrawable.Orientation.RIGHT_LEFT, new int[]{0x55000000, 0x2A000000, 0x00000000}));

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

        mainView.setBackground(new ColorDrawable(0xFFFFFFFF));

        //   contentView.addView(maskView);
        //   contentView.addView(mainView);
        //   contentView.setClickable(true);

        initSwipeBack();

        mContainer.addView(swipeBackLayout, params);

        dismiss();
    }

    private void initSwipeBack() {
        swipeBackLayout = new MySwipeBackLayout(mContext);
        swipeBackLayout.attachToView(mainView, mContext);
    }


    @Override
    public void setOnDialogItemClickListener(ChatRoomRecyclerViewAdapter.OnDialogItemClickListener listener) {
        // if (contentView.getTranslationX() == -ScreenUtils.dip2px(mContext, 16))
        mAdapter.setOnDialogItemClickListener(listener);
    }

    @Override
    public boolean isShowing() {
        return swipeBackLayout.getTranslationX() != ScreenUtils.getScreenWidth(mContext) + ScreenUtils.dip2px(mContext, 16);
    }


    @Override
    public void show() {
        show(ScreenUtils.getScreenWidth(mContext) + ScreenUtils.dip2px(mContext, 16));
    }

    @Override
    public void dismiss() {
        dismiss(0);
    }

    @Override
    public void show(int offest) {
        XposedBridge.log("show, offest = " + offest);
        if (isInAnim) return;

        isInAnim = true;
        ValueAnimator animator = ValueAnimator.ofInt(offest, -ScreenUtils.dip2px
                (mContext, 16));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                swipeBackLayout.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isInAnim = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(200);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setTarget(swipeBackLayout);
        animator.start();
    }

    @Override
    public void dismiss(int offest) {
        XposedBridge.log("dismiss, offest = " + offest);
        if (isInAnim) return;

        isInAnim = true;
        ValueAnimator animator = ValueAnimator.ofInt(offest, ScreenUtils.getScreenWidth(mContext) + ScreenUtils.dip2px(mContext, 16));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                swipeBackLayout.setTranslationX((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isInAnim = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setTarget(swipeBackLayout);
        animator.setDuration(200);
        animator.start();
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
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
//                new DiffCallBack(mAdapter.getData(), data), true);
//        diffResult.dispatchUpdatesTo(mAdapter);

        mAdapter.setData(data);

        mAdapter.notifyDataSetChanged();
    }


    private class DiffCallBack extends DiffUtil.Callback {
        private List<Object> mOldDatas, mNewDatas;

        DiffCallBack(List<Object> mOldDatas, List<Object> mNewDatas) {
            this.mOldDatas = mOldDatas;
            this.mNewDatas = mNewDatas;
        }

        @Override
        public int getOldListSize() {
            return mOldDatas != null ? mOldDatas.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return mNewDatas != null ? mNewDatas.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return true;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Object oldItem = mOldDatas.get(oldItemPosition);
            Object newItem = mNewDatas.get(newItemPosition);

            CharSequence oldContent = (CharSequence) XposedHelpers.callMethod(
                    mPresenter.getOriginAdapter(),
                    Method_Message_True_Content,
                    oldItem,
                    ScreenUtils.dip2px(mContext, 13),
                    XposedHelpers.getBooleanField(XposedHelpers.callMethod(
                            mPresenter.getOriginAdapter(),
                            Method_Message_Status_Bean, oldItem),
                            Value_Message_True_Content_Params));

            CharSequence newContent = (CharSequence) XposedHelpers.callMethod(
                    mPresenter.getOriginAdapter(),
                    Method_Message_True_Content,
                    newItem,
                    ScreenUtils.dip2px(mContext, 13),
                    XposedHelpers.getBooleanField(XposedHelpers.callMethod(
                            mPresenter.getOriginAdapter(),
                            Method_Message_Status_Bean, newItem),
                            Value_Message_True_Content_Params));

            return newContent.equals(oldContent);
        }
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
        mToolbar.setTitle("群消息助手");
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
                        "com.zdy.project.wechat_chatroom_helper.ui.MainActivity");
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
