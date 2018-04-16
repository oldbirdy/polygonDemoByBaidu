package baidumapsdk.demo.map;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


import com.baidu.mapapi.VersionInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.SpatialRelationUtil;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;
import baidumapsdk.demo.mapCeshi.CombinationOverlay;

/**
 * 演示覆盖物的用法
 */
public class OverlayDemo extends Activity {

    /**
     * MapView 是地图主控件
     */
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private List<CombinationOverlay> combinationOverlayList;
    private  List<LatLng> list;

    private InfoWindow infoWindow;
    private CombinationOverlay tempOverlay = null;// 当前选中的覆盖物
    private float lastx,lasty;
    private float offsetx,offsety;

    private boolean isDrag = false;  //marker是否正在拖拽
    private View popupView;  //弹出View
    private Button popup_btn_left;
    private TextView popup_tv_name;
    private Button popup_btn_right;
    private boolean isPopviewShow = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlay);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(14.0f);
        mBaiduMap.setMapStatus(msu);
        combinationOverlayList = new ArrayList<>();
        initOverlay();

    }

    public void initOverlay() {
        LatLng llA = new LatLng(39.963175, 116.400244);
        LatLng llB = new LatLng(39.942821, 116.369199);
        LatLng llC = new LatLng(39.939723, 116.425541);
        LatLng llD = new LatLng(39.906965, 116.401394);
        list = new ArrayList<>();
        list.add(llA);
        list.add(llB);
        list.add(llD);
        list.add(llC);

        final CombinationOverlay combinationOverlay = new CombinationOverlay(mMapView,list);
        combinationOverlayList.add(combinationOverlay);

        LatLng southwest = new LatLng(39.92235, 116.380338);
        LatLng northeast = new LatLng(39.947246, 116.414977);
        LatLngBounds bounds = new LatLngBounds.Builder().include(northeast)
                .include(southwest).build();

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);

        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {

                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){   //按下的时候 做处理
                    tempOverlay = null;
                    lastx =  motionEvent.getX();
                    lasty = motionEvent.getY();
                    Point point = new Point( (int)(motionEvent.getX()),(int) (motionEvent.getY()));
                    LatLng latlng = mBaiduMap.getProjection().fromScreenLocation(point);
//                    MapStatus.Builder builder = new MapStatus.Builder();

                    for(int i=0;i<combinationOverlayList.size();i++){
                       List<LatLng> list  = combinationOverlayList.get(i).getLatLngList();
                        if(SpatialRelationUtil.isPolygonContainsPoint(list,latlng)){   //判断是否在多边形里面
                            //在多边形内部
//                            createPopupView("提示消息", new OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                }
//                            }, new OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                }
//                            });
//                            infoWindow = new InfoWindow(popupView,latlng,0);
//                            mBaiduMap.showInfoWindow(infoWindow);
                            tempOverlay = combinationOverlayList.get(i);
                            mBaiduMap.getUiSettings().setScrollGesturesEnabled(false);
                            mBaiduMap.hideInfoWindow();
                            break;
                        }
                    }

                }else if(motionEvent.getAction()==MotionEvent.ACTION_MOVE){
                    if(tempOverlay!=null){
                        //全部根据手指的移动将其转化成百度坐标
                        if(!isDrag){
                            offsetx = motionEvent.getX() - lastx;
                            offsety = motionEvent.getY() - lasty;
                            lastx =  motionEvent.getX();
                            lasty = motionEvent.getY();
                            tempOverlay.updateOverlayByPolygon(offsetx,offsety);
                        }

                    }
                }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    if( tempOverlay != null){
                        mBaiduMap.getUiSettings().setScrollGesturesEnabled(true);
                    }
                }
            }
        });

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        //点击事件
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                return updateMarkerClick(marker);
            }
        });

        mBaiduMap.setOnMarkerDragListener(new OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
                updateMarkerDrag(marker);
            }

            public void onMarkerDragEnd(Marker marker) {
                isDrag = false;
            }

            public void onMarkerDragStart(Marker marker) {
                isDrag = true;
            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mBaiduMap.hideInfoWindow();

            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        mBaiduMap.setOnPolylineClickListener(new BaiduMap.OnPolylineClickListener() {
            @Override
            public boolean onPolylineClick(Polyline polyline) {
                mBaiduMap.hideInfoWindow();
                return updateLineClick(polyline);
            }
        });
    }

    /**
     * 更新marker点击
     * @param marker
     * @return
     */
    private boolean updateMarkerClick(final Marker marker) {
       for(int i=0;i < combinationOverlayList.size();i++){
           if(combinationOverlayList.get(i).getMarkerList().contains(marker)){

               Button button = new Button(getApplicationContext());
               button.setBackgroundResource(R.drawable.popup);
               LatLng ll = marker.getPosition();
               button.setText("删除当前点");
               button.setTextColor(Color.BLACK);
               final int finalI = i;
               button.setOnClickListener(new OnClickListener() {
                   public void onClick(View v) {
                       combinationOverlayList.get(finalI).updateOverlayByRemoveOneMarker(marker);

                   }
               });
               infoWindow = new InfoWindow(button,ll,-47);

               mBaiduMap.showInfoWindow(infoWindow);
               return true;
           }
       }
        return false;
    }

    /**
     * 更新线段点击
     * @param polyline
     * @return
     */
    private boolean updateLineClick(Polyline polyline){
        for(int i=0;i<combinationOverlayList.size();i++){
            if(combinationOverlayList.get(i).getPolylineList().contains(polyline)){
                combinationOverlayList.get(i).updateOverlayByLineClick(polyline);
                return true;
            }
        }
        return false;
    }

    private void updateMarkerDrag(Marker marker) {
        for(int i=0;i<combinationOverlayList.size();i++){
            if(combinationOverlayList.get(i).getMarkerList().contains(marker)){
                combinationOverlayList.get(i).updateOverlayByMarker(marker);
            }
        }

    }

    /**
     * 清除所有Overlay
     *
     * @param view
     */
    public void clearOverlay(View view) {
        mBaiduMap.clear();
    }

    /**
     * 重新添加Overlay
     *
     * @param view
     */
    public void resetOverlay(View view) {
        clearOverlay(null);
        initOverlay();
    }


    /**
     * 创建popupView
     * @param title
     * @param left
     * @param right
     */
    private void createPopupView(String title, View.OnClickListener left,View.OnClickListener right) {
        if (popupView == null) {
            popupView = LayoutInflater.from(this).inflate(R.layout.listformat_popview_new, null);
            popup_btn_left = (Button) popupView.findViewById(R.id.popup_btn_left);
            popup_btn_right = (Button) popupView.findViewById(R.id.popup_btn_right);
            popup_tv_name = (TextView) popupView.findViewById(R.id.popup_tv_name);
            popup_btn_left.setOnClickListener(left);

            popup_btn_right.setOnClickListener(right);

        }
        popup_btn_left.setText(title);
        popup_btn_left.setOnClickListener(left);
        popup_btn_right.setOnClickListener(right);
    }


    @Override
    protected void onPause() {
        // MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        // MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        super.onDestroy();


    }

}
