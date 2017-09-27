package com.example.administrator.refreshdemo.myview;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.administrator.refreshdemo.R;
import com.example.administrator.refreshdemo.minterface.OnRefreshListener;
import com.orhanobut.logger.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
public class MyListView extends ListView implements AbsListView.OnScrollListener{
    private static final String TAG="MyListView";
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private Date mDate;
    private SimpleDateFormat mSimpleDateFormat;
    private int firstVisibleItemPosition;//屏幕显示子啊第一个的item的索引
    private int downY;//按下屏幕时y轴的偏移量
    private int headerViewHeight;//头布局的高度(通过measureHeight测量获得)
    public View headerView;//头布局的对象
    private final int DOWN_PULL_REFRESH=0;//下拉刷新状态
    private final int RELEASE_REFRESH=1;//松开刷新
    private final int REFRESHING=2;//正在刷新状态
    private int currentState_header=REFRESHING;//头布局的状态，默认为下拉刷新状态
    private Animation upAnimation;//向上旋转的动画
    private Animation downAnimation;//向下旋转的动画
    private ImageView ivArrow;//头布局的显示箭头
    private ProgressBar mProgressBar;//头布局的进度条
    private TextView tvState;//头布局的状态
    private TextView tvLastUpdateTime;//头布局的最后更新时间
    private OnRefreshListener mOnRefreshListener;
    private boolean isScrollToBottom;//是否滑动到底部
    private View footerView;//脚布局的对象
    private int footerViewHeight;//脚布局的高度
    private boolean isLoadingMore=false;//是否正在加载更多中
    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        mLayoutInflater=LayoutInflater.from(mContext);
        initHeaderView();
        initFooterView();
        this.setOnScrollListener(this);
    }
    private void initHeaderView(){
        headerView= mLayoutInflater.inflate(R.layout.head_layout,null);
        ivArrow= (ImageView) headerView.findViewById(R.id.iv_listView_header_arrow);
        mProgressBar= (ProgressBar) headerView.findViewById(R.id.pb_listView_header);
        tvState= (TextView) headerView.findViewById(R.id.tv_listView_header_state);
        tvLastUpdateTime= (TextView) headerView.findViewById(R.id.tv_listView_header_last_update_time);
        tvLastUpdateTime.setText("最后刷新时间：" + getLastUpdateTime());
        headerView.measure(0,0);
        headerViewHeight=headerView.getMeasuredHeight();  //头布局的高度为负值（在屏幕top的上方）
        headerView.setPadding(0, -headerViewHeight, 0, 0);//填充左、上、右、下
        this.addHeaderView(headerView);
        initAnimation();
    }
    private String getLastUpdateTime(){//最后刷新时间
        mDate=new Date();
        mSimpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//获取当前时间
        return mSimpleDateFormat.format(mDate);
    }
    private void initAnimation(){
        //松手动画，以自身中心为原点，顺时针旋转180度
        upAnimation=new RotateAnimation(0f, 180f,Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        upAnimation.setDuration(500);
        upAnimation.setFillAfter(true);  //动画结束后，保留状态
        //下拉动画
        downAnimation=new RotateAnimation(0f, -360f,Animation.RELATIVE_TO_SELF,
                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        downAnimation.setDuration(500);
        downAnimation.setFillAfter(true);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN://按下
                downY= (int) ev.getY();  //记录按下时的坐标，与移动的坐标进行比较
                break;
            case MotionEvent.ACTION_MOVE://移动
                int moveY= (int) ev.getY();
                int diff=(moveY-downY)/2;   //间距=移动的点-按下的点
                //paddingTop=-头布局的高度+间距
                int paddingTop=-headerViewHeight+diff;
                //只有当firstVisibleItem===0时，才进行相应的刷新准备,同时要想刷新，必须首先下拉一下，即diff>0
                if(firstVisibleItemPosition==0&&paddingTop>-headerViewHeight){
                    //下拉之后还要判断是下拉刷新还是松开刷新（默认执行次判断）
                    if (paddingTop>0&&currentState_header==DOWN_PULL_REFRESH){//当前状态为正在往下拉
                        Logger.d("即将要松开刷新");
                        //改变状态，以便进行松开刷新
                        currentState_header=RELEASE_REFRESH;
                        refreshHeaderView();//进行相应的刷新动画操作，currentState_header为最新改变的值
                    }else if (paddingTop<0&&currentState_header==RELEASE_REFRESH){//当前状态为松手了，当没有成功
                      /*  Logger.d("还要下拉刷新");*/
                        currentState_header=DOWN_PULL_REFRESH;
                        refreshHeaderView();
                    }
                    //将头布局显示出来(随着paddingTop变化)
                    headerView.setPadding(0,paddingTop,0,0);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP://松手
                //松手时判断当前的状态
                if (currentState_header==RELEASE_REFRESH){
                    currentState_header=REFRESHING;
                    refreshHeaderView();
                }else if (currentState_header==DOWN_PULL_REFRESH){//回到初始的状态
                    //隐藏头布局
                    headerView.setPadding(0,-headerViewHeight,0,0);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }
    public void refreshHeaderView(){
        switch (currentState_header){
            case DOWN_PULL_REFRESH://默认要下拉的状态，开启箭头动画

                ivArrow.startAnimation(downAnimation);
                tvState.setText("下拉刷新");
                break;
            case RELEASE_REFRESH://松开刷新箭头动画

                ivArrow.startAnimation(upAnimation);
                tvState.setText("松开刷新");
                break;
            case REFRESHING://正在刷新中
                //切换视图
                headerView.setPadding(0,0,0,0);
                ivArrow.clearAnimation();
                ivArrow.setVisibility(GONE);
                mProgressBar.setVisibility(VISIBLE);
                tvState.setText("正在刷新中...");
                if(mOnRefreshListener!=null){
                    mOnRefreshListener.onDownPullRefresh();
                }
                break;
        }
    }

    public void hideHeaderView(){//隐藏头布局
        headerView.setPadding(0,-headerViewHeight,0,0);
        ivArrow.setVisibility(VISIBLE);
        mProgressBar.setVisibility(GONE);
        tvState.setText("下拉刷新");
        tvLastUpdateTime.setText("最后刷新时间：" + getLastUpdateTime());
        currentState_header=DOWN_PULL_REFRESH;
    }
    private void initFooterView(){
        footerView=mLayoutInflater.inflate(R.layout.footer_layout,null);
        footerView.measure(0, 0); //测量footerView的高度
        footerViewHeight=footerView.getMeasuredHeight();
        footerView.setPadding(0, -footerViewHeight, 0, 0); //初始化隐藏脚布局
        this.addFooterView(footerView); //添加到myListView上
    }
    public void hideFooterView(){
        footerView.setPadding(0,-footerViewHeight,0,0);
        isLoadingMore=false;
    }


    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener=onRefreshListener;
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState==SCROLL_STATE_IDLE||scrollState==SCROLL_STATE_FLING){//当滑动停止或惯性滑动时
            if (isScrollToBottom&&!isLoadingMore){//判断是否已经到达了底部,若在底部，并且之前没有加载，则加载更多
                isLoadingMore=true;  //即将加载更多(在每次加载完成之后才重置标识位为false)
                footerView.setPadding(0,0,0,0); //将脚布局显示出来
                //获得listView的数量，显示新加载的那一项（原有的最后一个为getCount-1）
                this.setSelection(this.getCount());
                if (mOnRefreshListener!=null){
                    mOnRefreshListener.onLoadingMore();
                }
            }

        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        firstVisibleItemPosition=firstVisibleItem;     //时刻监视可见视图中的第一个item
        if (firstVisibleItem+visibleItemCount==totalItemCount){//到达了底部
            isScrollToBottom=true;
        }else {
            isScrollToBottom=false;
        }
    }}


