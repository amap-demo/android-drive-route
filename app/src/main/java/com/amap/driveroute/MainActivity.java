package com.amap.driveroute;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.driveroute.util.AMapUtil;
import com.amap.driveroute.util.ToastUtil;

public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, RouteSearch.OnRouteSearchListener {

    private AMap aMap;
    private MapView mapView;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
    private LatLonPoint mStartPoint_bus = new LatLonPoint(40.818311, 111.670801);//起点，111.670801,40.818311
    private LatLonPoint mEndPoint_bus = new LatLonPoint(44.433942, 125.184449);//终点，
    private String mCurrentCityName = "北京";
    private final int ROUTE_TYPE_BUS = 1;
    private final int ROUTE_TYPE_DRIVE = 2;
    private final int ROUTE_TYPE_WALK = 3;
    private final int ROUTE_TYPE_CROSSTOWN = 4;

    private RelativeLayout mBottomLayout;
    private TextView mRotueTimeDes, mRouteDetailDes;
    private ListView mBusResultList;
    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init();
//		getIntentData();
        setfromandtoMarker();


    }

    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);
    }

    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);

    }

    public void onDriveClick(View view) {
        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
        mapView.setVisibility(View.VISIBLE);
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        if (routeType == ROUTE_TYPE_BUS) {// 公交路径规划
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo, mode,
                    mCurrentCityName, 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        } else if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        } else if (routeType == ROUTE_TYPE_WALK) {// 步行路径规划
            RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
            mRouteSearch.calculateWalkRouteAsyn(query);// 异步路径规划步行模式查询
        } else if (routeType == ROUTE_TYPE_CROSSTOWN) {
            RouteSearch.FromAndTo fromAndTo_bus = new RouteSearch.FromAndTo(
                    mStartPoint_bus, mEndPoint_bus);
            RouteSearch.BusRouteQuery query = new RouteSearch.BusRouteQuery(fromAndTo_bus, mode,
                    "呼和浩特市", 0);// 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
//            query.setCityd("农安县");
            mRouteSearch.calculateBusRouteAsyn(query);// 异步路径规划公交模式查询
        }
    }




    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == 1000) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    final DrivePath drivePath = mDriveRouteResult.getPaths()
                            .get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            mContext, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                    mBottomLayout.setVisibility(View.VISIBLE);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur)+"("+AMapUtil.getFriendlyLength(dis)+")";
                    mRotueTimeDes.setText(des);
                    mRouteDetailDes.setVisibility(View.VISIBLE);
                    int taxiCost = (int) mDriveRouteResult.getTaxiCost();
                    mRouteDetailDes.setText("打车约"+taxiCost+"元");
                    mBottomLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    DriveRouteDetailActivity.class);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result",
                                    mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    ToastUtil.show(mContext, R.string.no_result);
                }

            } else {
                ToastUtil.show(mContext, R.string.no_result);
            }
        } else {
            ToastUtil.showerror(this.getApplicationContext(), errorCode);
        }

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索...");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
