package com.alibaba.weex.amap.component;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.alibaba.weex.amap.R;
import com.alibaba.weex.amap.util.Constant;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.common.Constants;
import com.taobao.weex.dom.WXDomObject;
import com.taobao.weex.ui.component.WXComponentProp;
import com.taobao.weex.ui.component.WXVContainer;
import com.taobao.weex.utils.WXUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

/**
 * Created by budao on 2017/2/9.
 */

public class WXMapInfoWindowComponent extends WXVContainer<LinearLayout> {
  private Marker mMarker;
  private MapView mMapView;
  private WXMapViewComponent mWxMapViewComponent;

  public WXMapInfoWindowComponent(WXSDKInstance instance, WXDomObject dom, WXVContainer parent) {
    super(instance, dom, parent);
  }

  @Override
  protected LinearLayout initComponentHostView(@NonNull Context context) {
//    frameLayout = new LinearLayout(context);
//    // frameLayout.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
//    // frameLayout.setBackgroundColor(Color.TRANSPARENT);
    if (getParent() != null && getParent() instanceof WXMapViewComponent) {
      mWxMapViewComponent = (WXMapViewComponent) getParent();
      mMapView = ((WXMapViewComponent) getParent()).getHostView();
      boolean open = false;
      if ((getDomObject().getAttrs().get(Constant.Name.OPEN)) instanceof Boolean) {
        open = (boolean) getDomObject().getAttrs().get(Constant.Name.OPEN);
      } else if (getDomObject().getAttrs().get(Constant.Name.OPEN) instanceof String) {
        open =  Boolean.valueOf((String) getDomObject().getAttrs().get(Constant.Name.OPEN));
      }
      String offset = (String) getDomObject().getAttrs().get(Constant.Name.ICON);
      Object pos = getDomObject().getAttrs().get(Constant.Name.POSITION);
      String position = "";
      if (pos != null) {
        position = pos.toString();
      }
      initMarker(open, position, offset);
    }
    // FixMe： 只是为了绕过updateProperties中的逻辑检查
    LinearLayout linearLayout = new LinearLayout(context);
    linearLayout.setBackgroundColor(Color.TRANSPARENT);
    return linearLayout;
  }

  @Override
  protected boolean setProperty(String key, Object param) {
    switch (key) {
      case Constants.Name.POSITION:
        String position = WXUtils.getString(param, null);
        if (position != null)
          setPosition(position);
        return true;
    }
    return super.setProperty(key, param);
  }

  @WXComponentProp(name = Constant.Name.POSITION)
  public void setPosition(String position) {
    setMarkerPosition(position);
  }

  @WXComponentProp(name = Constant.Name.OFFSET)
  public void setOffset(String offset) {
    setMarkerInfoWindowOffset(offset);
  }

  @WXComponentProp(name = Constant.Name.OPEN)
  public void setOpened(Boolean opened) {
    if (opened) {
      mMarker.showInfoWindow();
    } else {
      mMarker.hideInfoWindow();
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    if (mMarker != null) {
      if (mWxMapViewComponent != null) {
        mWxMapViewComponent.getCachedInfoWindow().remove(mMarker.getId());
      }
      mMarker.remove();
    }
  }

  private void initMarker(boolean open, String position, String icon) {
    final MarkerOptions markerOptions = new MarkerOptions();
    //设置Marker可拖动, 将Marker设置为贴地显示，可以双指下拉地图查看效果
    markerOptions.setFlat(true);
    markerOptions.infoWindowEnable(true);
    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.infowindow_marker_icon));
    markerOptions.title(" ");
    AMap mMap = mMapView.getMap();
    List<Marker> mapScreenMarkers = mMap.getMapScreenMarkers();
    JSONArray jsonArray = null;
    try {
      jsonArray = new JSONArray(position);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    LatLng posLatLng = null;
    if (jsonArray != null) {
      posLatLng = new LatLng(jsonArray.optDouble(1), jsonArray.optDouble(0));
    }

    for (Marker marker : mapScreenMarkers) {
      LatLng latLng = marker.getPosition();
      if (posLatLng != null && latLng != null &&
              (posLatLng.latitude == latLng.latitude) && (posLatLng.longitude == posLatLng.longitude)) {
        mMarker = marker;
        break;
      }
    }
    if (mMarker == null) {
      mMarker = mMap.addMarker(markerOptions);
      setMarkerPosition(position);
    }
    mWxMapViewComponent.getCachedInfoWindow().put(mMarker.getId(), this);
    //mMarker.setClickable(false);
    if (open) {
      mMarker.showInfoWindow();
    } else {
      mMarker.hideInfoWindow();
    }
  }

  private void setMarkerInfoWindowOffset(String position) {
    try {
      JSONArray jsonArray = new JSONArray(position);
      if (mMarker != null) {
        MarkerOptions markerOptions = mMarker.getOptions();
        markerOptions.setInfoWindowOffset(jsonArray.optInt(0), jsonArray.optInt(1));
        mMarker.setMarkerOptions(markerOptions);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void setMarkerPosition(String position) {
    if (TextUtils.isEmpty(position)) return;
    try {
      JSONArray jsonArray = new JSONArray(position);
      LatLng latLng = new LatLng(jsonArray.optDouble(1), jsonArray.optDouble(0));
      MarkerOptions markerOptions = mMarker.getOptions();
      markerOptions.position(latLng);
      mMarker.setMarkerOptions(markerOptions);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void setMarkerPosition(double lat, double lng) {
    LatLng latLng = new LatLng(lat, lng);
    MarkerOptions markerOptions = mMarker.getOptions();
    markerOptions.position(latLng);
    mMarker.setMarkerOptions(markerOptions);
  }
}
