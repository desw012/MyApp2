package com.pdi.test;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * GA3 를 위한 이벤트를 function call을 활용하여 GA4로 한번 더 전송하기 위한 클래스
 * "event_name" 키를 이벤트 이름으로 사용합니다.
 * (필수) firebase 전송을 위해 mFirebaseAnalytics 를 FirebaseAnalytics.getInstance 함수를 통해 초기화 해야 합니다.
 * (필수) userproperty1 은 firebase가 정의한 ga4 cid로 설정됩니다.
 * 이 설정을 위해 전역변수 String[] ga4_cid 의 값을 설정하기 위한 getAppInstanceId task 실행이 필요합니다.
 * (필수) ga3 cid 활용을 위해 ga3_cid 값을 mTracker.get("&cid") 함수를 통해 초기화 시켜야 합니다.
 */
@Keep
public class FunctionCall implements com.google.android.gms.tagmanager.CustomTagProvider, Serializable {
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static String[] ga4_cid = new String[1];
    public static String[] ga3_cid = new String[1];
    public static String event_name;

    Bundle params;
    @Override
    public void execute(@NonNull Map<String, Object> map) {
        params = new Bundle();
        FunctionCallDataSend(map);
    }
    public void FunctionCallDataSend(Map<String, Object> GAInfo) {
        try {
            Iterator<String> sIterator = GAInfo.keySet().iterator();
            while (sIterator.hasNext()) {
                String key = sIterator.next();
                if (GAInfo.get(key) != null && GAInfo.get(key).toString().length() > 0) {

                    if (key.toLowerCase().contains("up_")) {
                        mFirebaseAnalytics.setUserProperty(key, GAInfo.get(key).toString());
                    }
                    else if (key.toLowerCase().contains("event_name")) {
                        event_name = GAInfo.get("event_name").toString();
                        params.remove(event_name);
                    }
                    else if(key.toLowerCase().contains("ep_page_HybridURL")){
                        String path = GAInfo.get("screen_class").toString();
                        String pathsplit = path.substring(0, 100);
                        params.putString(key, pathsplit);
                    }
                    else if(key.toLowerCase().contains("screen_class")){
                        String location = GAInfo.get("screen_class").toString();
                        if(location.length() > 100) {
                            String screen_class = location.substring(0, 100);
                            params.putString(key, screen_class);
                        }else{
                            params.putString(key, location);
                        }
                    }
                    else if(key.toLowerCase().contains("user_id")){
                        mFirebaseAnalytics.setUserId(GAInfo.get(key).toString());
                    }
                    else if (key.toLowerCase().contains("items")) {
                        String products_string = GAInfo.get(key).toString();
                        add_products(products_string);
                    }
                    else if (key.toLowerCase().contains(FirebaseAnalytics.Param.VALUE)) {   //value 값은 double로 추가
                        params.putDouble(FirebaseAnalytics.Param.VALUE, Double.parseDouble(GAInfo.get(key).toString()));
                    }
                    else if (key.toLowerCase().contains(FirebaseAnalytics.Param.TAX)) {   //tax 값은 double로 추가
                        params.putDouble(FirebaseAnalytics.Param.TAX, Double.parseDouble(GAInfo.get(key).toString()));
                    }
                    else if (key.toLowerCase().contains(FirebaseAnalytics.Param.SHIPPING)) {   //shipping 값은 double로 추가
                        params.putDouble(FirebaseAnalytics.Param.SHIPPING, Double.parseDouble(GAInfo.get(key).toString()));
                    }
                    else if (key.toLowerCase().contains(FirebaseAnalytics.Param.QUANTITY)) {   //quantity 값은 long로 추가
                        params.putLong(FirebaseAnalytics.Param.QUANTITY, Long.parseLong(GAInfo.get(key).toString()));
                    }
                    else{                   //userproperty 외 key / value 값 추가
                        params.putString(key, GAInfo.get(key).toString());
                    }
                }
            }
            mFirebaseAnalytics.setUserProperty("up_CID", ga4_cid[0]);      //up_CID을 firebase cid로 설정.

            //////////////////////////////////// 전송 /////////////////////////////////////
            mFirebaseAnalytics.logEvent(event_name, params);
        } catch (Exception e) {
            Log.e("GAv4_function_call", e.getMessage());
        }
    }
    public void add_products(String products_string) {
        try {
            if(products_string.contains("Bundle")){
                params.putString(FirebaseAnalytics.Param.ITEMS, products_string);
            }
            else {
                JSONArray product_array = new JSONArray(products_string);
                List product_list = ConvertJsonArray(product_array);
                ArrayList items = new ArrayList();
                for (int i = 0; i < product_list.size(); i++) {
                    HashMap<String, Object> product_Hashmap = (HashMap<String, Object>) product_list.get(i);
                    Bundle item = new Bundle();
                    Iterator<String> keys = product_Hashmap.keySet().iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (key.contains("id")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_ID, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("name")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_NAME, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("brand")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_BRAND, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("category")) {
                            String item_category = product_Hashmap.get(key).toString();
                            if (item_category.contains("/")) {
                                String[] item_categorys = item_category.split("/");
                                item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, item_categorys[0]);
                                for (int item_category_iter = 1; item_category_iter < item_categorys.length; item_category_iter++) {
                                    item.putString("item_category" + Integer.toString(1 + item_category_iter), item_categorys[item_category_iter]);
                                }
                            } else {
                                item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, item_category);
                            }
                            continue;
                        }
                        if (key.contains("price")) {
                            item.putDouble(FirebaseAnalytics.Param.PRICE, Double.parseDouble(product_Hashmap.get(key).toString()));
                            continue;
                        }
                        if (key.contains("quantity")) {
                            item.putLong(FirebaseAnalytics.Param.QUANTITY, Long.parseLong(product_Hashmap.get(key).toString()));
                            continue;
                        }
                        if (key.contains("variant")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_VARIANT, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("coupon")) {
                            item.putString(FirebaseAnalytics.Param.COUPON, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("metric1")) {//metric1(UA)을 discount(GA4)로 변환
                            params.putString(FirebaseAnalytics.Param.DISCOUNT, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("position")) {
                            Object position_value = product_Hashmap.get(key);
                            String position = String.valueOf(position_value);
                            item.putLong(FirebaseAnalytics.Param.INDEX, Integer.parseInt(position));
                            continue;
                        }
                    }
                    items.add(item);
                }
                params.putParcelableArrayList(FirebaseAnalytics.Param.ITEMS, items);
            }
        } catch (Exception ex) {
            Log.i("GAv4_fc_Products", ex.getMessage());
        }
    }
    private static Object ConvertObjectData(Object json) throws JSONException {
        try {
            if (json == JSONObject.NULL) return null;
            else if (json instanceof JSONObject) return ConvertJsonObject((JSONObject) json);
            else if (json instanceof JSONArray) return ConvertJsonArray((JSONArray) json);
            else return json;
        } catch (Exception e) {
            Log.e("GAv4_ConvertObjectData", e.getMessage());
            return null;
        }
    }
    public static Map<String, Object> ConvertJsonObject(JSONObject object) throws JSONException {
        try {
            Map<String, Object> map = new HashMap();
            Iterator keys = object.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                map.put(key, ConvertObjectData(object.get(key)));
            }
            return map;
        } catch (Exception e) {
            Log.e("GAv4_ConvertJsonObject", e.getMessage());
            return null;
        }
    }

    public static List ConvertJsonArray(JSONArray array) throws JSONException {
        try {
            List list = new ArrayList();
            for (int i = 0; i < array.length(); i++) {
                list.add(ConvertObjectData(array.get(i)));
            }
            return list;
        } catch (Exception e) {
            Log.e("GAv4_ConvertJsonArray", e.getMessage());
            return null;
        }
    }
    public static JSONArray ConvertItemToJSONArray(Bundle item){
        JSONArray items_update = new JSONArray();
        JSONObject item_JSONObject = new JSONObject();
        Set<String> keys = item.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                item_JSONObject.put(key, JSONObject.wrap(item.get(key)));
            } catch(JSONException e) {
            }
        }
        items_update.put(item_JSONObject);

        return items_update;
    }
    public static JSONArray ConvertItemToJSONArray(ArrayList <Bundle>items ){
        JSONArray items_update = new JSONArray();
        for(int i = 0; i < items.size();i++){
            Bundle tmp_item = items.get(i);
            JSONObject item_JSONObject = new JSONObject();
            Set<String> keys = tmp_item.keySet();
            for (String key : keys) {
                try {
                    // json.put(key, bundle.get(key)); see edit below
                    item_JSONObject.put(key, JSONObject.wrap(tmp_item.get(key)));
                } catch(JSONException e) {
                }
            }
            items_update.put(item_JSONObject);
        }
        return items_update;
    }
}