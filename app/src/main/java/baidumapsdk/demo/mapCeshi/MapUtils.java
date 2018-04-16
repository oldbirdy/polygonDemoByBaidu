package baidumapsdk.demo.mapCeshi;

import android.graphics.Point;
import android.support.annotation.Size;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Projection;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DELL on 2018/4/13.
 */

public class MapUtils {

    /**
     * 获取线段中心点坐标
     * @param mPoints
     * @return
     */
    public static LatLng getCenterOfLines(MapView mapView, @Size(2) List<LatLng> mPoints){
        if(mPoints.size()!=2){
            throw new IllegalArgumentException("线段点个数应为2个");
        }
        Projection projection = mapView.getMap().getProjection();
        Point point0 = projection.toScreenLocation(mPoints.get(0));
        Point point1 = projection.toScreenLocation(mPoints.get(1));
        Point point =  getCenterOfScrrenTwoPoint(point0,point1);
        return projection.fromScreenLocation(point);
    }

    /**
     * 获取屏幕上两点的中心坐标
     * @return
     */
    public static Point getCenterOfScrrenTwoPoint(Point point0,Point point1){
        return new Point((int)((point0.x+point1.x)/2),(int)((point0.y+point1.y)/2));
    }

    /**
     * 根据偏移量获取新的坐标值
     * @param list
     * @param offsetx
     * @param offsety
     * @return
     */
    public static List<LatLng> getLatLngByOffset(MapView mapView,List<LatLng> list,float offsetx,float offsety){
        Projection projection = mapView.getMap().getProjection();

        for(int i=0;i<list.size();i++){
            Point tempPoint = projection.toScreenLocation(list.get(i));
            tempPoint.offset((int)offsetx,(int)offsety);
            list.set(i,projection.fromScreenLocation(tempPoint));
        }
        return list;
    }

    //计算多边形重心  也可计算面积
    public static LatLng getCenterOfGravityPoint(List<LatLng> mPoints) {
        double area = 0.0;//多边形面积
        double Gx = 0.0, Gy = 0.0;// 重心的x、y
        for (int i = 1; i <= mPoints.size(); i++) {
            double iLat = mPoints.get(i % mPoints.size()).latitude;
            double iLng = mPoints.get(i % mPoints.size()).longitude;
            double nextLat = mPoints.get(i - 1).latitude;
            double nextLng = mPoints.get(i - 1).longitude;
            double temp = (iLat * nextLng - iLng * nextLat) / 2.0;
            area += temp;
            Gx += temp * (iLat + nextLat) / 3.0;
            Gy += temp * (iLng + nextLng) / 3.0;
        }
        Gx = Gx / area;
        Gy = Gy / area;
        return new LatLng(Gx, Gy);
    }
}
