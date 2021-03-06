package com.example.dapindao;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dapindao.API.DaPinDaoAPI;
import com.example.dapindao.App.DapinDaoApp;
import com.example.dapindao.Model.UserModel;
import com.example.dapindao.View.CollegeFragment;
import com.example.dapindao.View.FoundFragment;
import com.example.dapindao.View.HomeFragment;
import com.example.dapindao.View.LoginActivity;
import com.example.dapindao.View.MyFragment;
import com.example.dapindao.retrofit.HttpHelper;
import com.example.dapindao.utils.BaseActivity;
import com.example.dapindao.utils.ExampleUtil;
import com.example.dapindao.utils.Utils;

import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import cn.jzvd.JzvdStd;
import me.majiajie.pagerbottomtabstrip.NavigationController;
import me.majiajie.pagerbottomtabstrip.PageBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.item.BaseTabItem;
import me.majiajie.pagerbottomtabstrip.item.NormalItemView;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectedListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private TextView tvToolTitle;
    Toolbar toolbar;
    private FragmentManager manager;
    private NavigationController navigationController;
    private HomeFragment homeFragment;//首页
    private CollegeFragment collegeFragment;//学院
    private FoundFragment foundFragment;//发现
    private MyFragment myFragment;//我的
    private String token;
    public static boolean isForeground = false;
    private MessageReceiver mMessageReceiver;
    public static final String MESSAGE_RECEIVED_ACTION = "com.example.dapindao.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_EXTRAS = "extras";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_main);
        initt();
        registerMessageReceiver();  // used for receive msg
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setVisibility(View.GONE);

        Log.e("token是：", "onCreate: "+token );
        ButterKnife.bind(this);

        initUI();//初始化界面

    }

    @Override
    protected void onResume() {
        isForeground = true;
        super.onResume();
        toolbar.setVisibility(View.GONE);
        //home back
        JzvdStd.goOnPlayOnResume();
        token = Utils.getShared2(getApplicationContext(),"token");
        Log.e("token是：", "onCreate: "+token );
        if(!token.equals("")){
            DapinDaoApp.LoadUserModel(token);
        }
    }

    @Override
    protected void onPause() {
        isForeground = false;
        super.onPause();
        //Jzvd.clearSavedProgress(this, null);
        //home back
        JzvdStd.goOnPlayOnPause();
    }


    // 初始化 JPush。如果已经初始化，但没有登录成功，则执行重新登录。
    private void initt(){
        JPushInterface.init(getApplicationContext());
    }

    @Override
    protected void init() {

    }
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        filter.addAction(MESSAGE_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
    }
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
                    String messge = intent.getStringExtra(KEY_MESSAGE);
                    String extras = intent.getStringExtra(KEY_EXTRAS);
                    StringBuilder showMsg = new StringBuilder();
                    showMsg.append(KEY_MESSAGE + " : " + messge + "\n");
                    if (!ExampleUtil.isEmpty(extras)) {
                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n");
                    }
                   // setCostomMsg(showMsg.toString());
                }
            } catch (Exception e){
            }
        }
    }
/*    private void setCostomMsg(String msg){
        if (null != msgText) {
            msgText.setText(msg);
            msgText.setVisibility(View.VISIBLE);
        }
    }*/

    private void initUI(){
        tvToolTitle = (TextView) findViewById(R.id.toolbar_title);
        tvToolTitle.setText(getResources().getString(R.string.homepage));
        toolbar.setVisibility(View.GONE);
        tvToolTitle.setTextColor(Color.BLACK);
        PageBottomTabLayout tab = (PageBottomTabLayout) findViewById(R.id.tab);
        navigationController =tab.custom()
                .addItem(newItem(R.drawable.home,R.drawable.homeselect,getResources().getString(R.string.homepage)))
                .addItem(newItem(R.drawable.college,R.drawable.collegeselect,getResources().getString(R.string.college)))
                .addItem(newItem(R.drawable.found,R.drawable.foundselect,getResources().getString(R.string.found)))
                .addItem(newItem(R.drawable.my,R.drawable.myselect,getResources().getString(R.string.my)))
                .build();
        navigationController.addTabItemSelectedListener(listener);

        homeFragment=new HomeFragment();
        collegeFragment=new CollegeFragment();
        foundFragment=new FoundFragment();
        myFragment=new MyFragment();

        manager=getSupportFragmentManager();
        //初次登陆，显示首页，隐藏其他
        FragmentTransaction transaction=manager.beginTransaction();
        //toolbar.setVisibility(View.GONE);
        transaction.add(R.id.main_content,homeFragment);
        transaction.add(R.id.main_content,collegeFragment);
        transaction.add(R.id.main_content,foundFragment);
        transaction.add(R.id.main_content,myFragment);
        transaction.show(homeFragment);
        transaction.hide(collegeFragment);
        transaction.hide(foundFragment);
        transaction.hide(myFragment);
        transaction.commit();
    }

    OnTabItemSelectedListener listener=new OnTabItemSelectedListener() {
        @Override
        public void onSelected(int index, int old) {
            FragmentTransaction transaction=manager.beginTransaction();
            switch (index){
                //当选中首页id时，显示framelayout加载首页fragment
                case 0:
                    transaction.show(homeFragment);
                    transaction.hide(collegeFragment);
                    transaction.hide(foundFragment);
                    transaction.hide(myFragment);
                    tvToolTitle.setText(getResources().getString(R.string.homepage));
                    toolbar.setVisibility(View.GONE);
                    transaction.commit();
                    break;

                case 1:
                    transaction.hide(homeFragment);
                    transaction.show(collegeFragment);
                    transaction.hide(foundFragment);
                    transaction.hide(myFragment);
                    tvToolTitle.setText(getResources().getString(R.string.college));
                    toolbar.setVisibility(View.GONE);
                    transaction.commit();
                    break;

                case 2:
                    transaction.hide(homeFragment);
                    transaction.hide(collegeFragment);
                    transaction.show(foundFragment);
                    transaction.hide(myFragment);
                    tvToolTitle.setText(getResources().getString(R.string.found));
                    toolbar.setVisibility(View.GONE);
                    transaction.commit();

                    break;

                case 3:
                    transaction.hide(homeFragment);
                    transaction.hide(collegeFragment);
                    transaction.hide(foundFragment);
                    transaction.show(myFragment);
                    tvToolTitle.setText(getResources().getString(R.string.my));
                    toolbar.setVisibility(View.GONE);
                    transaction.commit();

                    break;


            }
        }

        @Override
        public void onRepeat(int index) {

        }
    };

    private BaseTabItem newItem(int drawable, int checkedDrawable, String text){
        NormalItemView normalItemView =new NormalItemView(this);
        normalItemView.initialize(drawable,checkedDrawable,text);
        normalItemView.setTextDefaultColor(Color.GRAY);
        normalItemView.setTextCheckedColor(getResources().getColor(R.color.black));
        return  normalItemView;

    }
}
