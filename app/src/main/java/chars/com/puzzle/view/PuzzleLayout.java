package chars.com.puzzle.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import chars.com.puzzle.R;
import chars.com.puzzle.util.ImagePiece;
import chars.com.puzzle.util.ImageSplitter;

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

    @Override
    public void onClick(View v) {
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
        String firstTag = (String)mFirst.getTag();
        String secondTag = (String)mSecond.getTag();

        String[] firstParams = firstTag.split("_");
        String[] secondParams = secondTag.split("_");

        mSecond.setImageBitmap(mItemBitmaps.get(Integer.parseInt(firstParams[0])).getBitmap());
        mFirst.setImageBitmap(mItemBitmaps.get(Integer.parseInt(secondParams[0])).getBitmap());

        mFirst.setTag(secondTag);
        mSecond.setTag(firstTag);

        mFirst = null;
        mSecond = null;


    }
}
