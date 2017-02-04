package com.pdwy.baidumap;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private List<LatLng> latLngsList;
    private MarkerOptions option;
    private List<Double> latitudeList = new ArrayList<Double>();
    private List<Double> longitudeList = new ArrayList<Double>();
    private double maxLatitude;
    private double minLatitude;
    private double maxLongitude;
    private double minLongitude;
    private double distance;
    private float level;
    private LatLng center;
    private InfoWindow infoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_baidumap);
        init();     //初始化相关配置
        start();    //开始获取坐标点定位
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    private void init() {
        mMapView = (MapView) findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //设置logo位置,默认在左下角显示，不可以移除。使用枚举类型控制显示的位置，共支持6个显示位置(左下，中下，右下，左上，中上，右上)。
        mMapView.setLogoPosition(LogoPosition.logoPostionleftBottom);
    }

    private void start() {
        //获取坐标点(模拟，实际为后台返回坐标，需异步加载)
        getPoints();
        //根据坐标点绘制Marker
        showLineMarker();
        //比较选出集合中最大经纬度
        getMax();
        //计算两个Marker之间的距离
        calculateDistance();
        //根据距离判断地图级别
        getLevel();
        //计算中心点经纬度，将其设为启动时地图中心
        setCenter();
        //设置marker点击事件
        setMarkerClick();
    }


    /**
     * 获取坐标点的方法
     */
    private void getPoints() {
        //获取坐标点
        latLngsList = new ArrayList<LatLng>();

        //模拟解析得到的坐标点
        final LatLng point = new LatLng(39.913175, 116.400244);
        final LatLng point2 = new LatLng(39.913375, 116.400644);
        final LatLng point3 = new LatLng(39.912175, 116.402270);

        // maxLatitude==39.963175;minLatitude==39.913375;maxLongitude==116.40227;minLongitude==116.200244

        latLngsList.add(point);
        latLngsList.add(point2);
        latLngsList.add(point3);

    }

    /**
     * 根据坐标点绘制Marker
     */
    private void showLineMarker() {
        //构建marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);

        for (int i = 0; i < latLngsList.size(); i++) {
            //构建MarkerOption，用于在地图上添加Marker
            option = new MarkerOptions().icon(bitmap).position(latLngsList.get(i));
            //生长动画
            option.animateType(MarkerOptions.MarkerAnimateType.grow);
            //在地图上添加Marker，并显示
            mBaiduMap.addOverlay(option);
            //设置Marker覆盖物的ZIndex
            option.zIndex(i);
        }
    }

    /**
     * 比较选出集合中最大经纬度
     */
    private void getMax() {
        for (int i = 0; i < latLngsList.size(); i++) {
            double latitude = latLngsList.get(i).latitude;
            double longitude = latLngsList.get(i).longitude;
            latitudeList.add(latitude);
            longitudeList.add(longitude);
        }
        maxLatitude = Collections.max(latitudeList);
        minLatitude = Collections.min(latitudeList);
        maxLongitude = Collections.max(longitudeList);
        minLongitude = Collections.min(longitudeList);
    }

    /**
     * 计算两个Marker之间的距离
     */
    private void calculateDistance() {
        distance = GeoHasher.GetDistance(maxLatitude, maxLongitude, minLatitude, minLongitude);
    }

    /**
     *根据距离判断地图级别
     */
    private void getLevel() {
        int zoom[] = {10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 1000, 2000, 25000, 50000, 100000, 200000, 500000, 1000000, 2000000};
        Log.i("info", "maxLatitude==" + maxLatitude + ";minLatitude==" + minLatitude + ";maxLongitude==" + maxLongitude + ";minLongitude==" + minLongitude);
        Log.i("info", "distance==" + distance);
        for (int i = 0; i < zoom.length; i++) {
            int zoomNow = zoom[i];
            if (zoomNow - distance * 1000 > 0) {
                level = 18 - i + 6;
                //设置地图显示级别为计算所得level
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(level).build()));
                break;
            }
        }
    }

    /**
     * 计算中心点经纬度，将其设为启动时地图中心
     */
    private void setCenter() {
        center = new LatLng((maxLatitude + minLatitude) / 2, (maxLongitude + minLongitude) / 2);
        Log.i("info", "center==" + center);
        MapStatusUpdate status1 = MapStatusUpdateFactory.newLatLng(center);
        mBaiduMap.animateMapStatus(status1, 500);
    }

    /**
     * 设置Marker点击事件
     */
    private void setMarkerClick() {
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                final LatLng ll = marker.getPosition();

                //动态生成一个view对象，用户在地图中显示InfoWindow
                final InfoView infoView = new InfoView(getApplicationContext());
                infoView.setBackgroundResource(R.drawable.button);
                infoView.setTv1("1号车", 14, Color.GREEN);
                infoView.setTv2("温度：20", 10, Color.RED);
                infoView.setTv3("湿度：101", 10, Color.BLACK);

                infoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int j = 0; j < latLngsList.size(); j++) {
                            LatLng point = latLngsList.get(j);
                            if (ll.equals(point)) {
                                Toast.makeText(MainActivity.this, "point=" + point, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                //初始化infoWindow，最后那个参数表示显示的位置相对于覆盖物的竖直偏移量，这里也可以传入一个监听器
                infoWindow = new InfoWindow(infoView, ll, -100);
                mBaiduMap.showInfoWindow(infoWindow);//显示此infoWindow
                //让地图以被点击的覆盖物为中心
                MapStatusUpdate status = MapStatusUpdateFactory.newLatLng(ll);
//                mBaiduMap.setMapStatus(status);
                //以动画方式更新地图状态，动画耗时 500 ms
                mBaiduMap.animateMapStatus(status, 500);
                return true;
            }
        });
    }
}
