package com.pdwy.baidumap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @说明:点击marker弹出的自定义view
 * @作者:zry
 * @时间:2016/8/26
 */
public class InfoView extends LinearLayout {

    private TextView tv1,tv2,tv3;

    public InfoView(Context context) {
        this(context,null);
    }

    public InfoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public InfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);

    }

    private void initView(Context context){
        View.inflate(context, R.layout.view_info,this);
        tv1= (TextView) findViewById(R.id.textView);
        tv2= (TextView) findViewById(R.id.textView2);
        tv3= (TextView) findViewById(R.id.textView3);

    }

    public void setTv1(String text, float size, int color){
        tv1.setText(text);
        tv1.setTextSize(size);
        tv1.setTextColor(color);
    }

    public void setTv2(String text, float size, int color){
        tv2.setText(text);
        tv2.setTextSize(size);
        tv2.setTextColor(color);
    }

    public void setTv3(String text, float size, int color){
        tv3.setText(text);
        tv3.setTextSize(size);
        tv3.setTextColor(color);
    }

}
