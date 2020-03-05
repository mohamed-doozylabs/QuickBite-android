package com.griffsoft.tsadadelivery;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import androidx.databinding.DataBinderMapper;
import androidx.databinding.DataBindingComponent;
import androidx.databinding.ViewDataBinding;
import com.griffsoft.tsadadelivery.databinding.FragmentAccountContainerBindingImpl;
import com.griffsoft.tsadadelivery.databinding.FragmentDeliveryContainerBindingImpl;
import com.griffsoft.tsadadelivery.databinding.FragmentOrdersContainerBindingImpl;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
  private static final int LAYOUT_FRAGMENTACCOUNTCONTAINER = 1;

  private static final int LAYOUT_FRAGMENTDELIVERYCONTAINER = 2;

  private static final int LAYOUT_FRAGMENTORDERSCONTAINER = 3;

  private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(3);

  static {
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.griffsoft.tsadadelivery.R.layout.fragment_account_container, LAYOUT_FRAGMENTACCOUNTCONTAINER);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.griffsoft.tsadadelivery.R.layout.fragment_delivery_container, LAYOUT_FRAGMENTDELIVERYCONTAINER);
    INTERNAL_LAYOUT_ID_LOOKUP.put(com.griffsoft.tsadadelivery.R.layout.fragment_orders_container, LAYOUT_FRAGMENTORDERSCONTAINER);
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View view, int layoutId) {
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = view.getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
        case  LAYOUT_FRAGMENTACCOUNTCONTAINER: {
          if ("layout/fragment_account_container_0".equals(tag)) {
            return new FragmentAccountContainerBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_account_container is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTDELIVERYCONTAINER: {
          if ("layout/fragment_delivery_container_0".equals(tag)) {
            return new FragmentDeliveryContainerBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_delivery_container is invalid. Received: " + tag);
        }
        case  LAYOUT_FRAGMENTORDERSCONTAINER: {
          if ("layout/fragment_orders_container_0".equals(tag)) {
            return new FragmentOrdersContainerBindingImpl(component, view);
          }
          throw new IllegalArgumentException("The tag for fragment_orders_container is invalid. Received: " + tag);
        }
      }
    }
    return null;
  }

  @Override
  public ViewDataBinding getDataBinder(DataBindingComponent component, View[] views, int layoutId) {
    if(views == null || views.length == 0) {
      return null;
    }
    int localizedLayoutId = INTERNAL_LAYOUT_ID_LOOKUP.get(layoutId);
    if(localizedLayoutId > 0) {
      final Object tag = views[0].getTag();
      if(tag == null) {
        throw new RuntimeException("view must have a tag");
      }
      switch(localizedLayoutId) {
      }
    }
    return null;
  }

  @Override
  public int getLayoutId(String tag) {
    if (tag == null) {
      return 0;
    }
    Integer tmpVal = InnerLayoutIdLookup.sKeys.get(tag);
    return tmpVal == null ? 0 : tmpVal;
  }

  @Override
  public String convertBrIdToString(int localId) {
    String tmpVal = InnerBrLookup.sKeys.get(localId);
    return tmpVal;
  }

  @Override
  public List<DataBinderMapper> collectDependencies() {
    ArrayList<DataBinderMapper> result = new ArrayList<DataBinderMapper>(1);
    result.add(new androidx.databinding.library.baseAdapters.DataBinderMapperImpl());
    return result;
  }

  private static class InnerBrLookup {
    static final SparseArray<String> sKeys = new SparseArray<String>(2);

    static {
      sKeys.put(0, "_all");
    }
  }

  private static class InnerLayoutIdLookup {
    static final HashMap<String, Integer> sKeys = new HashMap<String, Integer>(3);

    static {
      sKeys.put("layout/fragment_account_container_0", com.griffsoft.tsadadelivery.R.layout.fragment_account_container);
      sKeys.put("layout/fragment_delivery_container_0", com.griffsoft.tsadadelivery.R.layout.fragment_delivery_container);
      sKeys.put("layout/fragment_orders_container_0", com.griffsoft.tsadadelivery.R.layout.fragment_orders_container);
    }
  }
}
