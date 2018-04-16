package baidumapsdk.demo.mapCeshi;

import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Polygon;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

import baidumapsdk.demo.R;

/**
 *CombinationOverlay  组合覆盖 一个代表一个组
 */

public class CombinationOverlay {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private List<LatLng> latLngList;

    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);

    private List<List<LatLng>> lineListList;
    private Polygon polygonOverlay;
    private List<Marker> markerList;
    private List<Polyline> polylineList;

    private Stroke stroke = new Stroke(5, 0xAA00FF00);

    public CombinationOverlay(MapView mMapView, List<LatLng> latLngList) {
        this.mMapView = mMapView;
        this.latLngList = latLngList;
        if(latLngList.size()<3){
            throw new IllegalArgumentException("点数小于3，无法构成多边形");
        }
        mBaiduMap = mMapView.getMap();
        initZiyuan();
    }

    private void initZiyuan() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.points(latLngList);
        polygonOptions.stroke(stroke);
        polygonOptions.fillColor(0xAAFFFF00);
        polygonOverlay = (Polygon)mBaiduMap.addOverlay(polygonOptions);

        markerList = new ArrayList<>();
        for(int i = 0;i < latLngList.size();i++){
//            latLngList =
            MarkerOptions markerOptions = new MarkerOptions().position(latLngList.get(i)).icon(bdA).draggable(true);
            Marker marker= (Marker)mBaiduMap.addOverlay(markerOptions);
            markerList.add(marker);
        }

        Log.d("xulvcheng","width---->"+bdA.getBitmap().getWidth());
        Log.d("xulvcheng","height----->"+bdA.getBitmap().getHeight());
        lineListList = new ArrayList<>();
        polylineList = new ArrayList<>();
        for(int i=0;i<latLngList.size();i++){
            List<LatLng> latLngLineList = new ArrayList<>();
            latLngLineList.add(latLngList.get(i));
            if(i < latLngList.size()-1){
                latLngLineList.add(latLngList.get(i+1));
            }else{
                latLngLineList.add(latLngList.get(0));
            }
            lineListList.add(latLngLineList);
            PolylineOptions polylineOptions =  new PolylineOptions().points(latLngLineList).color(0xAAFFFF00).focus(true).width(10);
            Polyline polyline  = (Polyline)mBaiduMap.addOverlay(polylineOptions);
            polylineList.add(polyline);
        }
    }

    public Polygon getPolygonOverlay() {
        return polygonOverlay;
    }

    public void setPolygonOverlay(Polygon polygonOverlay) {
        this.polygonOverlay = polygonOverlay;
    }

    public List<Marker> getMarkerList() {
        return markerList;
    }

    public void setMarkerList(List<Marker> markerList) {
        this.markerList = markerList;
    }

    public List<Polyline> getPolylineList() {
        return polylineList;
    }

    public void setPolylineList(List<Polyline> polylineList) {
        this.polylineList = polylineList;
    }

    public List<LatLng> getLatLngList() {
        return latLngList;
    }

    /**
     * 更新覆盖物的位置
     */
    public void updateOverlayByMarker(Marker marker){
       int position =  markerList.indexOf(marker);
       if(position==-1){
           return;
       }
       latLngList.set(position,marker.getPosition());
       polygonOverlay.setPoints(latLngList);
       if(position==0){  //第一个点   更新第一条线和最后一条线
           lineListList.get(position).set(0,marker.getPosition());  //更新第一个点的坐标
           polylineList.get(position).setPoints(lineListList.get(position));

           lineListList.get(lineListList.size()-1).set(1,marker.getPosition()); //更新第二个点
           polylineList.get(polylineList.size()-1).setPoints(lineListList.get(polylineList.size()-1 ));
       }else{
           lineListList.get(position).set(0,marker.getPosition());  //更新第一个点
           polylineList.get(position).setPoints(lineListList.get(position));

           lineListList.get(position-1).set(1,marker.getPosition());
           polylineList.get(position-1).setPoints(lineListList.get(position-1));
       }
    }

    /**
     * 根据偏移量更新显示位置
     * @param offsetx
     * @param offsety
     */
    public void updateOverlayByPolygon(float offsetx,float offsety){
        latLngList =  MapUtils.getLatLngByOffset(mMapView,latLngList,offsetx,offsety);
        polygonOverlay.setPoints( latLngList);
        for(int i=0;i < markerList.size();i++){
            markerList.get(i).setPosition(latLngList.get(i));
        }

        lineListList.clear();
        for(int i=0;i<latLngList.size();i++){
            List<LatLng> latLngLineList = new ArrayList<>();
            latLngLineList.add(latLngList.get(i));
            if(i < latLngList.size()-1){
                latLngLineList.add(latLngList.get(i+1));
            }else{
                latLngLineList.add(latLngList.get(0));
            }
            lineListList.add(latLngLineList);

            polylineList.get(i).setPoints(latLngLineList);
        }
//

    }



    /**
     * 点击线条触发
     * @param polyline
     */
    public void updateOverlayByLineClick(Polyline polyline){
        int positon = polylineList.indexOf(polyline);
        if(-1==positon){
            return;
        }
        LatLng latLng = MapUtils.getCenterOfLines(mMapView,polyline.getPoints());  //得到中心点
        latLngList.add(positon+1,latLng);
        removeCombinationOverlay();
        initZiyuan();
    }


    public void updateOverlayByRemoveOneMarker(Marker marker){
        int positon = markerList.indexOf(marker);
        if(-1==positon){
            return;
        }
        if(markerList.size()<4){
            Toast.makeText(mMapView.getContext(), "不能移除当前点，移除后无法构成多边形", Toast.LENGTH_SHORT).show();
        }else{
            latLngList.remove(positon);
            removeCombinationOverlay();
            initZiyuan();
        }
        mBaiduMap.hideInfoWindow();
    }



    /**
     * 移除覆盖物
     */
    public void removeCombinationOverlay(){
        polygonOverlay.remove();

        for(int i=0;i<markerList.size();i++){
            markerList.get(i).remove();
        }

        for(int i=0;i<polylineList.size();i++){
            polylineList.get(i).remove();
        }
        markerList.clear();
        polylineList.clear();
        lineListList.clear();

    }

}
