package com.example.dapindao.View;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.dapindao.API.DaPinDaoAPI;
import com.example.dapindao.Adapter.CommentExpandAdapter;
import com.example.dapindao.Adapter.ShadowCastAdapter;
import com.example.dapindao.App.DapinDaoApp;
import com.example.dapindao.Model.ArticleDetailsModel;
import com.example.dapindao.Model.ConmmentModel;
import com.example.dapindao.R;
import com.example.dapindao.retrofit.HttpHelper;
import com.example.dapindao.utils.BaseActivity;
import com.example.dapindao.utils.MyBottomSheetDialog;
import com.example.dapindao.utils.RecyclerViewEmptySupport;
import com.example.dapindao.utils.RelativeDateFormat;
import com.example.dapindao.utils.ShowPicRelation;
import com.example.dapindao.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.makeramen.roundedimageview.RoundedImageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//文章详情
public class ArticleDetailsActivity extends BaseActivity implements View.OnClickListener {
    public Intent intent;
    @BindView(R.id.iv_back)
    ImageView mIvBack;
    @BindView(R.id.wv_content)
    WebView mWvContent;
    private int UserId;
    private ArticleDetailsModel model;
    private ConmmentModel conmmentModel;
    @BindView(R.id.title_Tv)
    TextView title_Tv;//标题
    @BindView(R.id.avatar)
    RoundedImageView avatar;//作者头像
    @BindView(R.id.publishDate)
    TextView publishDate;//时间
    @BindView(R.id.userName)
    TextView userName;//作者名
    @BindView(R.id.recyclerView)
    RecyclerViewEmptySupport recyclerView;//相关新闻
    @BindView(R.id.detail_page_do_comment)
    TextView bt_comment;
    ShadowCastAdapter alertAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    @BindView(R.id.detail_page_lv_comment)
    CommentExpandableListView expandableListView;
    @BindView(R.id.conment_tv)
    TextView conment_tv;
    private BottomSheetDialog dialog;
    private CommentExpandAdapter adapter;
    private List<ArticleDetailsModel.CommentResultBean.ListBean> commentsList = new ArrayList<>();
    @BindView(R.id.comments)
    TextView comments;
    @BindView(R.id.comments_btn)
    ConstraintLayout comments_btn;
    @BindView(R.id.praise_btn)
    ConstraintLayout praise_btn;//点赞
    @BindView(R.id.praise)
    ImageView praise;//点赞图标
    @BindView(R.id.Collect)
    ImageView Collect;//受否收藏
    @BindView(R.id.likeCount)
    TextView likeCount;//点赞数量
    @BindView(R.id.scroll)
    ScrollView scroll;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;//下拉加载更多评论
    @BindView(R.id.Focus_check)
    CheckBox Focus_check;//关注
    private String secondType;//详细类型
    private static String articleUuid;//文章、视频、快讯的uuid
    private int articleUserId;//文章所属作者的用户id
    private String articleTitle;//标题
    private String avatarPath;//头像地址
    private String UserName;//个人昵称
    private String token;
    private int pageNum = 1;
    private int pageSize = 10;
    private int totalPage;
    private String articleUuids;
    private String articleImgPath;
    private int toUserName;
    private boolean hasLike;//是否点赞true-是，false-未
    private boolean hasCollect;//是否收藏true-是，false-未
    private String type;//1点赞，2取消点赞
    private String Collecttype;//1收藏，2取消收藏
    private static final String APP_CACAHE_DIRNAME = "/webcache";
    private int isFans;//是否关注




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        intent = getIntent();
        UserId = Utils.getShared(getApplicationContext(),"UserId");
        ButterKnife.bind(this);

        mIvBack.setOnClickListener(this);
        bt_comment.setOnClickListener(this);
        comments_btn.setOnClickListener(this);
        praise_btn.setOnClickListener(this);
        Collect.setOnClickListener(this);
        Focus_check.setOnClickListener(this);
        initUI();
        initData();





    }

    private void initUI(){
        refreshLayout.setEnableRefresh(false);
        refreshLayout.setEnableLoadMore(true);
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        avatarPath = Utils.getShared2(getApplicationContext(),"avatarPath");
        UserName=(Utils.getShared2(getApplicationContext(),"userName"));
        token = Utils.getShared2(getApplicationContext(),"token");
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
               //LoadMoreCommnent();
            }
        });

    }
    public void initData(){
        Call<ArticleDetailsModel>call = HttpHelper.getInstance().create(DaPinDaoAPI.class).getArticleDetailWeb(intent.getStringExtra("articleId"),"0",pageSize,UserId);
        Log.e("articleId", "articleId: "+intent.getStringExtra("articleId") );
        call.enqueue(new Callback<ArticleDetailsModel>() {
            @Override
            public void onResponse(Call<ArticleDetailsModel> call, Response<ArticleDetailsModel> response) {
                if(response.body()!=null){
                    if(response.body().getCode() == 0){
                        model = new ArticleDetailsModel();
                        model.setArticle(response.body().getArticle());
                        model.setCommentResult(response.body().getCommentResult());
                        articleUuid = response.body().getArticle().getUuid();
                        getArticleIsCollectDetail(articleUuid);
                        articleUserId = response.body().getArticle().getUserId();
                        ISFans(articleUserId);
                        articleTitle = response.body().getArticle().getTitle();
                        articleImgPath = response.body().getArticle().getImgPath();
                        comments.setText(""+model.getCommentResult().getTotal());
                        //likeCount.setText(""+model.getArticle().getLikeCount());
                        totalPage = model.getCommentResult().getTotalPage();
                        title_Tv.setText(model.getArticle().getTitle());
                        userName.setText(model.getArticle().getUser().getUserName());
                        expandableListView.setGroupIndicator(null);
                        //默认展开所有回复
                        commentsList= model.getCommentResult().getList();
                        adapter = new CommentExpandAdapter(ArticleDetailsActivity.this,getApplicationContext(),commentsList);
                        expandableListView.setAdapter(adapter);
                        for(int i = 0; i<commentsList.size(); i++){
                            expandableListView.expandGroup(i);
                        }

                        expandableListView.setOnGroupClickListener((expandableListView, view, groupPosition, l) -> {
                            Log.e("TAG", "onGroupClick: 当前的评论id>>>"+commentsList.get(groupPosition).getId());
                            showReplyDialog(groupPosition,commentsList.get(groupPosition).getId());
                            return true;
                        });
                        expandableListView.setOnChildClickListener((expandableListView, view, groupPosition, childPosition, l) -> {
                            Toast.makeText(getApplicationContext(),"点击了回复",Toast.LENGTH_SHORT).show();
                            return false;
                        });
                        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                            @Override
                            public void onGroupExpand(int groupPosition) {
                                //toast("展开第"+groupPosition+"个分组");

                            }
                        });
                        Glide.with(getApplicationContext()).load(model.getArticle().getUser().getAvatarpath()).into(avatar);
                        publishDate.setText(RelativeDateFormat.format(model.getArticle().getPublishDate()));
                        mWvContent.getSettings().setJavaScriptEnabled(true);//设置JS可用
                        mWvContent.addJavascriptInterface(new ShowPicRelation(getApplicationContext()),"chaychan");//绑定JS和Java的联系类，以及使用到的昵称
                        mWvContent.loadDataWithBaseURL(null, model.getArticle().getContent(), "text/html", "UTF-8", null);
                        mWvContent.setWebViewClient(new WebViewClient(){
                            @Override
                            public void onPageFinished(WebView view, String url) {
                                addJs(view);//添加多JS代码，为图片绑定点击函数
                            }
                        });
                        queryArticlePageWeb(model.getArticle().getArtTypeId());//相关新闻数据
                    }else {
                        Toast.makeText(getApplicationContext(),response.body().getMsg(),Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArticleDetailsModel> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ISFans(int articleUserId){
        Log.e("articleUserId", "ISFans: "+articleUserId );
        Call<ResponseBody> call2 = HttpHelper.getInstance().create(DaPinDaoAPI.class).judgeIsAttention(token,articleUserId);
        call2.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.body()!=null){
                    String jsonStr = null;//把原始数据转为字符串
                    try {
                        jsonStr = new String(response.body().bytes());
                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                        if(jsonObject.get("code").getAsInt() == 0){
                            isFans = Integer.parseInt(jsonObject.get("isFans").getAsString());
                            Log.e("TAG", "onResponse: "+isFans );
                            if(isFans == 1){
                                Focus_check.setText("已关注");
                                Focus_check.setChecked(true);
                            }else if(isFans == 0){
                                Focus_check.setText("未关注");
                                Focus_check.setChecked(false);
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWvContent.clearCache(true);
        clearWebViewCache();
    }

    private void getArticleIsCollectDetail(String articleUuid){
        //获取文章是否点赞和收藏
        Log.e("articleUuid", "articleUuid: "+articleUuid );
        Call<ResponseBody> call1 = HttpHelper.getInstance().create(DaPinDaoAPI.class).getArticleIsCollectDetail(token,articleUuid,"1","1");
        call1.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.body()!=null){
                    try {
                        String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                        if(jsonObject.get("code").getAsInt() == 0){
                            hasLike = jsonObject.get("hasLike").getAsBoolean();
                            hasCollect = jsonObject.get("hasCollect").getAsBoolean();
                            Log.e("hasLike", "hasLike: "+hasLike );
                            if(hasLike){
                                type ="2";
                                praise.setImageDrawable(getResources().getDrawable(R.mipmap.praise2));
                            }else {
                                type ="1";
                                praise.setImageDrawable(getResources().getDrawable(R.mipmap.praise1));
                            }

                            if(hasCollect){
                                Collecttype = "2";
                                Collect.setImageDrawable(getResources().getDrawable(R.mipmap.collection2));
                            }else {
                                Collecttype = "1";
                                Collect.setImageDrawable(getResources().getDrawable(R.mipmap.collection1));
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

/*
   public void LoadMoreCommnent(String articleUuids){
        Call<ConmmentModel> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).queryOneCommentPage(pageNum,pageSize,articleUuids);

        call.enqueue(new Callback<ConmmentModel>() {
            @Override
            public void onResponse(Call<ConmmentModel> call, Response<ConmmentModel> response) {
                if(response.body() != null){
                    if(response.body().getCode()== 0){
                        conmmentModel = new ConmmentModel();
                        conmmentModel.setResult(response.body().getResult());
                        expandableListView.setGroupIndicator(null);
                        //默认展开所有回复
                        commentsList= conmmentModel.getResult().getRows();
                        adapter = new CommentExpandAdapter(ArticleDetailsActivity.this,getApplicationContext(),commentsList);
                        expandableListView.setAdapter(adapter);
                        for(int i = 0; i<commentsList.size(); i++){
                            expandableListView.expandGroup(i);
                        }

                        expandableListView.setOnGroupClickListener((expandableListView, view, groupPosition, l) -> {
                            Log.e("TAG", "onGroupClick: 当前的评论id>>>"+commentsList.get(groupPosition).getId());
                            showReplyDialog(groupPosition,commentsList.get(groupPosition).getId());
                            return true;
                        });
                        expandableListView.setOnChildClickListener((expandableListView, view, groupPosition, childPosition, l) -> {
                            Toast.makeText(getApplicationContext(),"点击了回复",Toast.LENGTH_SHORT).show();
                            return false;
                        });
                        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                            @Override
                            public void onGroupExpand(int groupPosition) {
                                //toast("展开第"+groupPosition+"个分组");

                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ConmmentModel> call, Throwable t) {

            }
        });
    }*/

    private void queryArticlePageWeb(int artTypeId){
        Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).queryArticlePageWeb(1,5,"1",artTypeId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.body()!=null){
                    try {
                        String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                        if(jsonObject.get("code").getAsInt() == 0){
                            JsonObject object = jsonObject.get("result").getAsJsonObject();
                            JsonArray jsonElements = object.getAsJsonArray("rows");
                            alertAdapter = new ShadowCastAdapter(getApplicationContext(),jsonElements);
                            recyclerView.setAdapter(alertAdapter);
                        }else {
                            Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void init() {

    }

    /**添加JS代码，获取所有图片的链接以及为图片设置点击事件*/
    private void addJs(WebView wv) {
        wv.loadUrl("javascript:(function  pic(){"+
                "var imgList = \"\";"+
                "var imgs = document.getElementsByTagName(\"img\");"+
                "for(var i=0;i<imgs.length;i++){"+
                "var img = imgs[i];"+
                "imgList = imgList + img.src +\";\";"+
                "img.onclick = function(){"+
                "window.chaychan.openImg(this.src);"+
                "}"+
                "}"+
                "window.chaychan.getImgArray(imgList);"+
                "})()");
    }

    @Override
    public void onClick(View view) {
        if(view == mIvBack){
            finish();
        }
        if(view ==bt_comment){
            showCommentDialog();
        }
        if(view == comments_btn){
            scroll.post(new Runnable() {
                @Override
                public void run() {
                    scroll.smoothScrollTo(0, conment_tv.getTop());
                }
            });

        }

        if(view == praise_btn){
            //点赞
            Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).userLike(token,intent.getStringExtra("articleId"),articleUuid,type,"1",articleUserId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.body()!=null){
                        try {
                            String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                            JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                            if(jsonObject.get("code").getAsInt() == 0){
                                getArticleIsCollectDetail(articleUuid);
                               // Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }

        //关注
        if(view == Focus_check){
            if(isFans == 1){
                Call<ResponseBody> call2 = HttpHelper.getInstance().create(DaPinDaoAPI.class).deleteAttentionUser(token, String.valueOf(articleUserId));
                call2.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.body()!=null){
                            String jsonStr = null;//把原始数据转为字符串
                            try {
                                jsonStr = new String(response.body().bytes());
                                JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                                if(jsonObject.get("code").getAsInt() == 0){
                                        Focus_check.setChecked(false);
                                        Focus_check.setText("未关注");
                                }else {
                                    Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                Call<ResponseBody> call2 = HttpHelper.getInstance().create(DaPinDaoAPI.class).attentionUser(token, String.valueOf(articleUserId));
                call2.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.body()!=null){
                            String jsonStr = null;//把原始数据转为字符串
                            try {
                                jsonStr = new String(response.body().bytes());
                                JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                                if(jsonObject.get("code").getAsInt() == 0){
                                    Focus_check.setChecked(true);
                                    Focus_check.setText("已关注");
                                }else {
                                    Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        if(view == Collect){
            Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).userCollect(token,intent.getStringExtra("articleId"),articleUuid,Collecttype,"1","1",articleUserId);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.body()!=null){
                        try {
                            String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                            JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                            if(jsonObject.get("code").getAsInt() == 0){
                                getArticleIsCollectDetail(articleUuid);
                                Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                            }else {
                                Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showCommentDialog(){
        dialog = new MyBottomSheetDialog(this,R.style.BottomSheetEdit);
        View commentView = LayoutInflater.from(this).inflate(R.layout.comment_dialog_layout,null);
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        dialog.setContentView(commentView);
        /**
         * 解决bsd显示不全的情况
         */
        View parent = (View) commentView.getParent();
        BottomSheetBehavior behavior = BottomSheetBehavior.from(parent);
        commentView.measure(0,0);
        behavior.setPeekHeight(commentView.getMeasuredHeight());

        bt_comment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String commentContent = commentText.getText().toString().trim();
                if(!TextUtils.isEmpty(commentContent)){

                    //commentOnWork(commentContent);
                    dialog.dismiss();
                    Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).saveComment(token,"1","1",articleUuid,articleUserId,articleTitle,articleImgPath,commentContent,null,toUserName,null);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.body()!=null){
                                try {
                                    String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                                    if(jsonObject.get("code").getAsInt() == 0){
                                        ArticleDetailsModel.CommentResultBean.ListBean detailBean = new ArticleDetailsModel.CommentResultBean.ListBean(avatarPath,UserName, commentContent,Utils.getData(), DapinDaoApp.getUserId());
                                        adapter.addTheCommentData(detailBean);
                                        initData();
                                        Toast.makeText(getApplicationContext(),"评论成功",Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });


                }else {
                    Toast.makeText(getApplicationContext(),"评论内容不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence) && charSequence.length()>2){
                    bt_comment.setBackgroundColor(Color.parseColor("#FFB568"));
                }else {
                    bt_comment.setBackgroundColor(Color.parseColor("#D8D8D8"));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }


    /**
     * by moos on 2018/04/20
     * func:弹出回复框
     */
    private void showReplyDialog(final int position,final  String oneCommentId){
        dialog = new BottomSheetDialog(this,R.style.BottomSheetEdit);
        View commentView = LayoutInflater.from(this).inflate(R.layout.comment_dialog_layout,null);
        final EditText commentText = (EditText) commentView.findViewById(R.id.dialog_comment_et);
        final Button bt_comment = (Button) commentView.findViewById(R.id.dialog_comment_bt);
        commentText.setHint("回复 " + commentsList.get(position).getUserName() + " 的评论:");
        dialog.setContentView(commentView);
        bt_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String replyContent = commentText.getText().toString().trim();
                if(!TextUtils.isEmpty(replyContent)){
                    dialog.dismiss();
                    Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).saveComment(token,"2","1",articleUuid,articleUserId,articleTitle,articleImgPath,replyContent,null,toUserName,oneCommentId);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                            if(response.body()!=null){
                                try {
                                    String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                                    JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                                    if(jsonObject.get("code").getAsInt() == 0){
                                        ArticleDetailsModel.CommentResultBean.ListBean.SubListBean detailBean = new ArticleDetailsModel.CommentResultBean.ListBean.SubListBean(UserName,replyContent);
                                        adapter.addTheReplyData(detailBean, position);
                                        expandableListView.expandGroup(position);
                                        Toast.makeText(getApplicationContext(),"回复成功",Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(getApplicationContext(),jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    });

                }else {
                    Toast.makeText(getApplicationContext(),"回复内容不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        commentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence) && charSequence.length()>2){
                    bt_comment.setBackgroundColor(Color.parseColor("#FFB568"));
                }else {
                    bt_comment.setBackgroundColor(Color.parseColor("#D8D8D8"));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        dialog.show();
    }


    /**
     * 清除WebView缓存
     */
    public void clearWebViewCache(){

        //清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File(getFilesDir().getAbsolutePath()+APP_CACAHE_DIRNAME);
        Log.e("TAG", "appCacheDir path="+appCacheDir.getAbsolutePath());

        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()+"/webviewCache");
        Log.e("TAG", "webviewCacheDir path="+webviewCacheDir.getAbsolutePath());

        //删除webview 缓存目录
        if(webviewCacheDir.exists()){
            deleteFile(webviewCacheDir);
        }
        //删除webview 缓存 缓存目录
        if(appCacheDir.exists()){
            deleteFile(appCacheDir);
        }
    }

    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile(File file) {

        Log.i("TAG", "delete file path=" + file.getAbsolutePath());

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e("TAG", "delete file no exists " + file.getAbsolutePath());
        }
    }



}
