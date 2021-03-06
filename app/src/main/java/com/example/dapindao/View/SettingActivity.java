package com.example.dapindao.View;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.dapindao.API.DaPinDaoAPI;
import com.example.dapindao.R;
import com.example.dapindao.retrofit.Constants;
import com.example.dapindao.retrofit.HttpHelper;
import com.example.dapindao.utils.BaseActivity;
import com.example.dapindao.utils.CacheUtil;
import com.example.dapindao.utils.MyBottomSheetDialog;
import com.example.dapindao.utils.Utils;
import com.example.dapindao.utils.WxShareUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.devio.takephoto.model.CropOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseActivity implements View.OnClickListener, WbShareCallback {
    //设置界面
    @BindView(R.id.Account_Settings)
     RelativeLayout Account_Settings;//账号管理
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.share_btn)
    RelativeLayout share_btn;
    private Bitmap bitmap;
    WbShareHandler shareHandler;
    @BindView(R.id.video_list)
    RelativeLayout video_list;//一下载的视频
    @BindView(R.id.login_out)
    RelativeLayout login_out;//退出登录
    private String token;
    @BindView(R.id.cache)
    TextView cache;//缓存
    @BindView(R.id.clearcache)
    RelativeLayout clearcache;//清楚缓存
    @BindView(R.id.Useragreement)
    RelativeLayout Useragreement;//用户协议
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        ButterKnife.bind(this);
        token = Utils.getShared2(getApplicationContext(),"token");
        try {
            cache.setText(CacheUtil.getTotalCacheSize(getApplicationContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        initEvent();
        initWebiBo();
    }

    private void initEvent(){
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo, null);
        Account_Settings.setOnClickListener(this);
        back.setOnClickListener(this);
        share_btn.setOnClickListener(this);
        video_list.setOnClickListener(this);
        login_out.setOnClickListener(this);
        clearcache.setOnClickListener(this);
        Useragreement.setOnClickListener(this);
    }

    private void initWebiBo(){
        WbSdk.install(this,new AuthInfo(this, Constants.WeiBoAPP_KEY,Constants.REDIRECT_URL, Constants.SCOPE));//创建微博API接口类对象
        shareHandler = new WbShareHandler(this);
        shareHandler.registerApp();
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void init() {

    }
    private void showShare() {
        //头像
        final MyBottomSheetDialog dialog = new MyBottomSheetDialog(this);
        View box_view = LayoutInflater.from(this).inflate(R.layout.wxshare,null);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  //←重点在这里，来，都记下笔记
        dialog.setContentView(box_view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        LinearLayout wxbtn = (LinearLayout) box_view.findViewById(R.id.wxbtn);
        LinearLayout wxpyt_btn = (LinearLayout) box_view.findViewById(R.id.wxpyt_btn);
        LinearLayout wb_btn = (LinearLayout)box_view.findViewById(R.id.wb_btn);
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.wxbtn:
                        //分享到微信
                        WxShareUtils.shareWeb(getApplicationContext(), "wxedc39d2d62caf29e",
                                "www.baidu.com", "大频道APP", "这是一个测试",
                                bitmap,1);
                        break;
                    case R.id.wxpyt_btn:
                        //分享到朋友圈
                        WxShareUtils.shareWeb(getApplicationContext(), "wxedc39d2d62caf29e",
                                "www.baidu.com", "大频道APP", "这是一个测试",
                                bitmap,2);
                        break;
                    case R.id.wb_btn:
                        //分享到微博
                        sendMessageToWb(true,false);
                        break;
                }
                dialog.dismiss();
            }
        };

        wxbtn.setOnClickListener(listener);
        wxpyt_btn.setOnClickListener(listener);
        wb_btn.setOnClickListener(listener);
    }
    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     */
    private void sendMessageToWb(boolean hasText, boolean hasImage) {
        sendMultiMessage(hasText, hasImage);
    }
    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     */
    private void sendMultiMessage(boolean hasText, boolean hasImage) {

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        if (hasText) {
            weiboMessage.textObject = getTextObj();
        }
        if (hasImage) {
            weiboMessage.imageObject = getImageObj(this);
        }
        shareHandler.shareMessage(weiboMessage, false);
    }
    /**
     * 创建文本消息对象。
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = "我是大频道";
        textObject.title = "我是大频道";
        textObject.actionUrl = "http://www.baidu.com";
        return textObject;
    }

    /**
     * 创建图片消息对象。
     * @return 图片消息对象。
     */
    private ImageObject getImageObj(Context context) {
        ImageObject imageObject = new ImageObject();
        Bitmap  bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.logo);
        imageObject.setImageObject(bitmap);
        return imageObject;
    }



    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.Account_Settings:
                intent = new Intent(getApplicationContext(),AccountSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.share_btn:
                showShare();
                break;
            case R.id.video_list:
                intent = new Intent(getApplicationContext(),VideoListActivity.class);
                startActivity(intent);
                break;
            case R.id.clearcache:
                final AlertDialog.Builder Diaglog = new AlertDialog.Builder(SettingActivity.this);
                Diaglog.setTitle("提示");//文字
                Diaglog.setMessage("确定清楚缓存？");//提示消息
                Diaglog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CacheUtil.clearAllCache(getApplicationContext());
                        try {
                            cache.setText(CacheUtil.getTotalCacheSize(getApplicationContext()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                Diaglog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //显示
                Diaglog.show();

                break;
            case R.id.login_out:
                final AlertDialog.Builder alterDiaglog = new AlertDialog.Builder(SettingActivity.this);
                alterDiaglog.setTitle("提示");//文字
                alterDiaglog.setMessage("确定退出登录？");//提示消息
                alterDiaglog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //退出登录
                        Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).loginOut(token);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if(response.body()!=null){
                                    try {
                                        String jsonStr = new String(response.body().bytes());//把原始数据转为字符串
                                        JsonObject jsonObject = (JsonObject) new JsonParser().parse(jsonStr);
                                        if(jsonObject.get("code").getAsInt() == 0){
                                            Utils.ClearData(getApplicationContext());
                                           Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                                           startActivity(intent);
                                           finish();
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
                });
                alterDiaglog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                //显示
                alterDiaglog.show();

                break;
            case R.id.Useragreement:
                //用户协议
                intent = new Intent(getApplicationContext(),UserAgreementActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onWbShareSuccess() {
        Log.d("TAG","分享成功");
    }

    @Override
    public void onWbShareCancel() {
        Log.d("TAG","分享取消");
    }

    @Override
    public void onWbShareFail() {
        Log.d("TAG","分享失败");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        shareHandler.doResultIntent(intent,this);
    }

}
