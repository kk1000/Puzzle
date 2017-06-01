package chars.com.puzzle.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chars.com.puzzle.R;
import chars.com.puzzle.util.ImagePiece;
import chars.com.puzzle.util.ImageSplitter;

import static android.content.ContentValues.TAG;

/**
 * Created by dengw on 2017/5/31.
 */

public class PuzzleLayout extends RelativeLayout implements View.OnClickListener {
    private int mColumn = 3;
    /**
     * 容器的内边距
     */
    private int mPadding;

    /**
     * 每张小图间的距离（横、纵）dp
     */
    private int mMargin = 3;

    private ImageView[] mItems;

    private int mItemWidth;

    /**
     * 游戏图片
     */
    private Bitmap mBitmap;

    private List<ImagePiece> mItemBitmaps;

    private boolean once;

    /**
     * 游戏面板宽度、高度
     */
    private int mWidth;

    private boolean isGameSuccess;

    public  interface PuzzleListener{
        void nextLevel(int nextLevel);
        void timeChanged(int currentTime);
        void gameOver();
    }

    public PuzzleListener mListener;


    public PuzzleListener getmListener() {
        return mListener;
    }

    public void setmListener(PuzzleListener mListener) {
        this.mListener = mListener;
    }

    private int level = 1;
    private static final int TIME_CHANGED = 0x110;
    private static final int NEXT_LEVEL = 0x111;

    // 是否开启时间
    private boolean isTimeEnabled = false;

    public boolean isTimeEnabled() {
        return isTimeEnabled;
    }

    public void setTimeEnabled(boolean timeEnabled) {
        isTimeEnabled = timeEnabled;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case TIME_CHANGED:
                    break;
                case NEXT_LEVEL:
                    level += 1;
                    if (mListener != null){
                        mListener.nextLevel(level);
                    } else {
                        nextLevel();
                    }
                    break;
                default:
                    break;
            }
        };
    };

    public PuzzleLayout(Context context) {
        this(context, null);
    }

    public PuzzleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init(){
        //默认值为3dp
        mMargin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(), getPaddingBottom());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());
        if (!once) {
            // 切图，排序
            initBitmap();
            // 设置ImageView 的宽高等属性
            initItem();
            once = true;
        }
        setMeasuredDimension(mWidth, mWidth);
    }

    /**
     * 设置ImageView 的宽高等属性
     */
    private void initItem() {
        mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        mItems = new ImageView[mColumn * mColumn];
        //生成Item，设置Rule
        for (int i = 0; i < mItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
            mItems[i] = item;
            item.setId(i+1);
            // 在item的tag中存储了index
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemWidth);
            //设置item间横向间隙，通过rightMargin
            //不是最后一列
            if ((i + 1) % mColumn != 0){
                lp.rightMargin = mMargin;
            }
            // 不是第一列
            if (i % mColumn != 0){
                lp.addRule(RelativeLayout.RIGHT_OF, mItems[i -1].getId());
            }
            // 不是第一行，设置topMargin和rule
            if ((i + 1) > mColumn) {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW, mItems[i - mColumn].getId());
            }
            addView(item, lp);
        }
    }

    /**
     * 切图，排序
     */
    private void initBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        }
        mItemBitmaps = ImageSplitter.splitImage(mBitmap, mColumn);
        // 乱序
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece o1, ImagePiece o2) {
                return Math.random() > 0.5 ? 1 : -1;
            }
        });
    }

    /**
     * 获取多个参数的最小值
     * @param params
     * @return
     */
    private int min(int ...params) {
        int min = params[0];
        for (int param:params) {
            if (param < min) {
                min = param;
            }
        }
        return min;
    }

    private ImageView mFirst;
    private ImageView mSecond;
    // 动画层
    private RelativeLayout mAnimateLayout;
    private boolean isAnimating;

    @Override
    public void onClick(View v) {
        if (isAnimating) return;

        //两次点击同一个
        if (mFirst == v){
            mFirst.setColorFilter(null);
            mFirst = null;
            return;
        }

        if (mFirst == null){
            mFirst = (ImageView)v;
            mFirst.setColorFilter(Color.parseColor("#55ff0000"));
        } else {
            mSecond = (ImageView)v;
            //交换Item
            exchangeView();
        }
    }

    /**
     * 交换Item
     */
    private void exchangeView() {
        mFirst.setColorFilter(null);

        //构造动画层
        setUpAnimateLayout();

        ImageView first = new ImageView(getContext());
        final Bitmap firstBitmap = mItemBitmaps.get(getImageIdByTag((String)mFirst.getTag())).getBitmap();
        first.setImageBitmap(firstBitmap);
        LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
        lp.leftMargin = mFirst.getLeft() - mPadding;
        lp.topMargin = mFirst.getTop() - mPadding;
        first.setLayoutParams(lp);
        mAnimateLayout.addView(first);

        ImageView second = new ImageView(getContext());
        final Bitmap secondBitmap = mItemBitmaps.get(getImageIdByTag((String)mSecond.getTag())).getBitmap();
        second.setImageBitmap(secondBitmap);
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
        lp2.leftMargin = mSecond.getLeft() - mPadding;
        lp2.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(lp2);
        mAnimateLayout.addView(second);

        //设置动画
        TranslateAnimation animate = new TranslateAnimation(0, mSecond.getLeft() - mFirst.getLeft(), 0 , mSecond.getTop() - mFirst.getTop());
        animate.setDuration(300);
        animate.setFillAfter(true);
        first.startAnimation(animate);

        TranslateAnimation animate2 = new TranslateAnimation(0, -mSecond.getLeft() + mFirst.getLeft(), 0 , -mSecond.getTop() + mFirst.getTop());
        animate2.setDuration(300);
        animate2.setFillAfter(true);
        second.startAnimation(animate2);

        //监听动画
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mFirst.setVisibility(View.INVISIBLE);
                mSecond.setVisibility(View.INVISIBLE);
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String)mFirst.getTag();
                String secondTag = (String)mSecond.getTag();

                mSecond.setImageBitmap(firstBitmap);
                mFirst.setImageBitmap(secondBitmap);

                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);

                mFirst.setVisibility(View.VISIBLE);
                mSecond.setVisibility(View.VISIBLE);

                mFirst = null;
                mSecond = null;

                mAnimateLayout.removeAllViews();
                //判断游戏是否成功
                checkSuccess();
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 判断游戏是否成功
     */
    private void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < mItems.length; i++) {
            ImageView imageView = mItems[i];
            if (getImageIndexByTag((String) imageView.getTag()) != i) {
                isSuccess = false;
            }
        }
        if (isSuccess) {
            Log.e(TAG, "checkSuccess: sucess");
            Toast.makeText(getContext(), "Success, level up", Toast.LENGTH_LONG).show();
            mHandler.sendEmptyMessage(NEXT_LEVEL);
        }
    }

    /**
     * 根据tag获取id
     * @param tag
     * @return
     */
    public int getImageIdByTag(String tag){
        String[] params = tag.split("_");
        return Integer.parseInt(params[0]);
    }

    public int getImageIndexByTag(String tag){
        String[] params = tag.split("_");
        return Integer.parseInt(params[1]);
    }

    /**
     * 构造动画层
     */
    private void setUpAnimateLayout() {
        if (mAnimateLayout == null) {
            mAnimateLayout = new RelativeLayout(getContext());
            addView(mAnimateLayout);
        }
    }

    public void nextLevel(){
        this.removeAllViews();
        mAnimateLayout = null;
        mColumn++;
        isGameSuccess = false;
        initBitmap();
        initItem();
    }
}
