package com.example.dapindao.Presenter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.dapindao.API.DaPinDaoAPI;
import com.example.dapindao.Adapter.AlertAdapter;
import com.example.dapindao.Adapter.FilmCriticsAdapter;
import com.example.dapindao.Adapter.HotListAdapter;
import com.example.dapindao.App.DapinDaoApp;
import com.example.dapindao.Interface.AlertsInterface;
import com.example.dapindao.Interface.HotListInterface;
import com.example.dapindao.Model.VideobyModel;
import com.example.dapindao.View.AlertsFragment;
import com.example.dapindao.View.BannerViewPager;
import com.example.dapindao.View.FilmCriticsFragment;
import com.example.dapindao.View.VideoDetailsActivity;
import com.example.dapindao.retrofit.HttpHelper;
import com.example.dapindao.utils.RecyclerViewEmptySupport;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilmCriticsPresenter implements AlertsInterface.Presenter{
    private Context context;
    private AlertsInterface.View view;
    private BannerViewPager banner;
    private FilmCriticsAdapter adapter;
    private RecyclerViewEmptySupport recyclerViewEmptySupport;
    private List<String> listurl = new ArrayList<>();
    private List<String> title = new ArrayList<>();
    public FilmCriticsPresenter(Context context, BannerViewPager banner, FilmCriticsAdapter adapter, RecyclerViewEmptySupport recyclerViewEmptySupport,AlertsInterface.View view){
        this.context = context;
        this.view = view;
        this.banner = banner;
        this.adapter = adapter;
        this.recyclerViewEmptySupport = recyclerViewEmptySupport;

    }


    public void queryVedioesIsRecAll(){
        Call<VideobyModel> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).queryVedioesIsRecAll("1");
        call.enqueue(new Callback<VideobyModel>() {
            @Override
            public void onResponse(Call<VideobyModel> call, Response<VideobyModel> response) {
                if(response.body()!=null){
                    if(response.body().getCode() == 0){
                        for(int i = 0;i<response.body().getResult().getRows().size();i++){
                            listurl.add(response.body().getResult().getRows().get(i).getImgPath());
                            title.add(response.body().getResult().getRows().get(i).getTitle());
                        }
                        banner.initBanner(listurl, false,title)//图片地址，关闭3D画廊效果
                                .addPageMargin(0, 0)//参数1page之间的间距,参数2中间item距离边界的间距
                                .addPoint(3)
                                .addPointBottom(7)
                                .addStartTimer(5)//自动轮播5秒间隔
                                .finishConfig()//这句必须加
                                .addBannerListener(new BannerViewPager.OnClickBannerListener() {
                                    @Override
                                    public void onBannerClick(int position){
                                        //点击item
                                       Intent intent = new Intent(context, VideoDetailsActivity.class);
                                         intent.putExtra("id",response.body().getResult().getRows().get(position).getId());
                                        intent.putExtra("secondType","3");
                                        intent.putExtra("articleUuid",response.body().getResult().getRows().get(position).getUuid());
                                        context.startActivity(intent);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onFailure(Call<VideobyModel> call, Throwable t) {

            }
        });
    }

    @Override
    public void recFront(int type, int pageNum, int pageSize) {
        Call<ResponseBody> call = HttpHelper.getInstance().create(DaPinDaoAPI.class).recFront(type,0,0);
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
                            adapter = new FilmCriticsAdapter(context,jsonElements);
                            recyclerViewEmptySupport.setAdapter(adapter);
                            adapter.setOnitemClickListener(new FilmCriticsAdapter.OnitemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                    JsonObject object1 = jsonElements.get(position).getAsJsonObject();
                                    Log.e("id", "onItemClick: "+object1.get("id").getAsString());
                                    Intent intent = new Intent(context, VideoDetailsActivity.class);
                                    intent.putExtra("secondType","3");
                                    intent.putExtra("id",object1.get("id").getAsString());
                                    context.startActivity(intent);
                                }
                            });
                            int total = object.get("total").getAsInt();
                            int totalPage = object.get("totalPage").getAsInt();
                            if(total != 0){
                                view.onRefresh();
                            }else {
                                view.failed();
                            }
                            if(totalPage>pageNum){
                                jsonElements.addAll(jsonElements);
                                view.onLoadMore();
                            }else {
                                view.onNothingData();
                            }

                        }else {
                            Toast.makeText(context,jsonObject.get("msg").getAsString(),Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context,t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
