package me.geed.animationbanner;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import me.geed.widget.animationbanner.AnimationBanner;

public class ExampleActivity extends AppCompatActivity {
    private AnimationBanner mBanner;
    private List<ImageView> mImageViews;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Author Email：yourswee@gmail.com", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        showAnimationBanner();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_example, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示自定义Banner控件
     */
    public void showAnimationBanner() {
        /**
         * 注意各方法的调用顺序
         */
        mBanner = (AnimationBanner) findViewById(R.id.banner);
        mBanner.setBannerNum(4);

        /**
         * Indicator默认位置为右下方
         */
        //mBanner.setIndicatorPosition(AnimationBanner.IndicatorPosition.CENTER_BOTTOM);

        /**
         * 设置Indicator小圆点半径
         */
        //mBanner.setIndicatorRaidus(20);

        /**
         * 设置Indicator小圆点处于当前位置时的颜色
         */
        //mBanner.setSelectedColor(Color.RED);

        /**
         * 设置Indicator小圆点非当前位置时的颜色
         */
        //mBanner.setUnSelectedColor(Color.BLUE);

        /**
         * 动画持续时间默认为500毫秒
         */
        //mBanner.setAnimDuration(400);

        mBanner.initView();

        /**
         * 根据所下载图片的实际宽高来设置Banner的宽高,按比例显示图片；默认宽度为屏幕宽度
         * 需在initView之后，下载图片之前调用，否则无效
         * 本例中的图片为640*210
         */
        mBanner.setBannerSize(640 / 210);

        /**
         * 根据所下载图片的实际宽高来设置Banner的宽高,按比例显示图片；设置自定义宽度
         * 需在initView之后，下载图片之前调用，否则无效
         */
        //mBanner.setBannerSize(640 / 210, 800);

        /**
         * 先初始化才能获取里面的ImageViews
         * 此处的示例为本地图片模拟，通常情况下为网络下载图片，如使用Glide等图片库，同时会将图片缓存机制添加到其中
         */
        mImageViews = mBanner.getBannerImageViews();
        mImageViews.get(0).setImageResource(R.mipmap.banner1);
        mImageViews.get(1).setImageResource(R.mipmap.banner2);
        mImageViews.get(2).setImageResource(R.mipmap.banner3);
        mImageViews.get(3).setImageResource(R.mipmap.banner4);

        /**
         * Banner点击响应交给使用者来实现
         */
        mBanner.setOnBannerClickListener(new AnimationBanner.OnBannerClickListener() {
            @Override
            public void onClick(int bannerIndex) {
                Toast.makeText(ExampleActivity.this, "Banner Clicked! index:" + bannerIndex, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
