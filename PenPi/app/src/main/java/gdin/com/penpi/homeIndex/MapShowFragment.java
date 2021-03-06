package gdin.com.penpi.homeIndex;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.HashMap;
import java.util.Map;

import gdin.com.penpi.R;
import gdin.com.penpi.commonUtils.Parameter;

public class MapShowFragment extends Fragment implements LocationSource, AMapLocationListener {

    private TextureMapView mapView;
    private static AMap aMap;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private static Map<String, String> addressMap;

    public static AMap getaMap() {
        return aMap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_map_show, container, false);//设置对应的XML布局文件

        mapView = (TextureMapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

        UiSettings mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        mUiSettings.setZoomControlsEnabled(false);

        // --------------------设置样式------------------------
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));    // 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.map_location)));
        aMap.setMyLocationStyle(myLocationStyle);
        return view;
    }

    /**
     * LocationSource接口
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            //初始化定位
            mLocationClient = new AMapLocationClient(getActivity());
            //设置定位回调监听
            mLocationClient.setLocationListener(this);

            //初始化定位参数
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            //设置定位模式为Hight_Accuracy高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置是否只定位一次,默认为false
            mLocationOption.setOnceLocation(false);
            //设置是否允许模拟位置,默认为false，不允许模拟位置
            mLocationOption.setMockEnable(false);
            //设置定位间隔,单位毫秒,默认为2000ms
            mLocationOption.setInterval(300000);

            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * AMapLocationListener接口
     * 定位成功后回调函数
     *
     * @param aMapLocation aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
     *                     aMapLocation.getLatitude();//获取纬度
     *                     aMapLocation.getLongitude();//获取经度
     *                     aMapLocation.getAccuracy();//获取精度信息
     *                     aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息
     *                     aMapLocation.getCountry();//国家信息
     *                     aMapLocation.getProvince();//省信息
     *                     aMapLocation.getCity();//城市信息
     *                     aMapLocation.getDistrict();//城区信息
     *                     aMapLocation.getStreet();//街道信息
     *                     aMapLocation.getStreetNum();//街道门牌号信息
     *                     aMapLocation.getCityCode();//城市编码
     *                     aMapLocation.getAdCode();//地区编码
     *                     aMapLocation.getAoiName();//获取当前定位点的AOI信息
     *                     aMapLocation.getBuildingId();//获取当前室内定位的建筑物Id
     *                     aMapLocation.getFloor();//获取当前室内定位的楼层
     *                     aMapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
     *                     //获取定位时间
     *                     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     *                     Date tv_date = new Date(aMapLocation.getTime());
     *                     df.format(tv_date);
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点->点击定位按钮 能够将地图的中心移动到定位点

                //设置缩放级别
                aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                //将地图移动到定位点
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude())));

                mListener.onLocationChanged(aMapLocation);
                //获取定位信息
                String buffer = (aMapLocation.getCountry() + ""
                        + aMapLocation.getProvince() + ""
                        + aMapLocation.getCity() + ""
                        + aMapLocation.getProvince() + ""
                        + aMapLocation.getDistrict() + ""
                        + aMapLocation.getStreet() + ""
                        + aMapLocation.getStreetNum());
                //Toast.makeText(getActivity().getApplicationContext(), buffer, Toast.LENGTH_LONG).show();

                // 提供一个Map给其他类用
                addressMap = new HashMap<>();
                addressMap.put("Latitude", String.valueOf(aMapLocation.getLatitude()));
                addressMap.put("Longitude", String.valueOf(aMapLocation.getLongitude()));
                addressMap.put("Address", aMapLocation.getAddress());
                addressMap.put("Country", aMapLocation.getCountry());
                addressMap.put("Province", aMapLocation.getProvince());
                addressMap.put("City", aMapLocation.getCity());
                addressMap.put("District", aMapLocation.getDistrict());
                addressMap.put("Street", aMapLocation.getStreet());
                Parameter.addressMap = addressMap;
            }
        } else {
            assert aMapLocation != null;
            String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
            Log.e("AmapErr", errText);
        }
    }

    /**
     * 提供一个接口给其他类用，以获得一些地图信息
     */
    public static Map<String, String> getAddressMap() {
        return addressMap;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

}
