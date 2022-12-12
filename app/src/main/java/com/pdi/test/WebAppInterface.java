package com.pdi.test;

import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WebAppInterface {
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static String[] ga4_cid = new String[1];

    @JavascriptInterface
    public void GA_DATA(String JsonData) {
        try {
            Bundle params = new Bundle();
            JSONObject data = new JSONObject(JsonData);

            String sType ="";
            String en = "";
            String location = "";
            String title = "";

            if (data.has("type")) sType = data.getString("type");
            if (data.has("event_name")) en = data.getString("event_name");
            if (data.has("location")) location = data.getString("location").substring(0,100);
            if (data.has("title")) title = data.getString("title");

            Iterator<String> sIterator = data.keys();
            while(sIterator.hasNext()){
                String key = sIterator.next();
                if(key.contains("ep_")) params.putString(key,data.getString(key));
                else if(key.contains("up_")) mFirebaseAnalytics.setUserProperty(key,data.getString(key));
            }

//            mFirebaseAnalytics.setUserProperty("up_cid", ga4_cid[0]);
//            mFirebaseAnalytics.setUserId(data.getString("up_uid"));

            if(sType.equals("P")){ // 스크린뷰일 때
                if(data.has("title")) params.putString(FirebaseAnalytics.Param.SCREEN_NAME, title);
                if(data.has("location")) params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, location);
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW,params);
            }else if(sType.equals("E")){ // 이벤트일 때
                mFirebaseAnalytics.logEvent(en,params);
            }else if(sType.equals("Ecommerce")){
                params = ecommerce_parse(params,data);
                en = data.getString("EcommerceStep").toLowerCase();
                mFirebaseAnalytics.logEvent(en,params);
            }
        } catch (Exception ex) {
            Log.i("GA4_WebInterface_Error", ex.getMessage());
        }
    }

    Bundle ecommerce_parse(Bundle params, JSONObject data) {
        JSONObject actionfield = null;
        JSONObject obj_ecommerce = data;
        String currencyCode = new String();
        try {
            if (obj_ecommerce.has("currencyCode")){
                currencyCode = obj_ecommerce.getString("currencyCode");
                params.putString(FirebaseAnalytics.Param.CURRENCY, currencyCode);
            }
            if (obj_ecommerce.has("transaction")) {
                actionfield = obj_ecommerce.getJSONObject("transaction");
                if (actionfield.has("currencyCode")) {
                    currencyCode = actionfield.getString("currencyCode");
                    params.putString(FirebaseAnalytics.Param.CURRENCY, currencyCode);
                }
            }
        } catch (Exception ex) {
            Log.i("GAv4_currencycode", ex.getMessage());
        }

        try{
            if (obj_ecommerce.has("transaction")) {
                actionfield = obj_ecommerce.getJSONObject("transaction");
                Iterator<String> keys = actionfield.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.contains("transaction_id"))
                        params.putString(FirebaseAnalytics.Param.TRANSACTION_ID, actionfield.get(key).toString());
                    if (key.contains("affiliation"))
                        params.putString(FirebaseAnalytics.Param.AFFILIATION, actionfield.get(key).toString());
                    if (key.contains("value"))
                        params.putDouble(FirebaseAnalytics.Param.VALUE, Double.parseDouble(actionfield.get(key).toString()));
                    if (key.contains("tax"))
                        params.putDouble(FirebaseAnalytics.Param.TAX, Double.parseDouble(actionfield.get(key).toString()));
                    if (key.equals("shipping"))
                        params.putDouble(FirebaseAnalytics.Param.SHIPPING, Double.parseDouble(actionfield.get(key).toString()));
                    if (key.contains("coupon"))
                        params.putString(FirebaseAnalytics.Param.COUPON, actionfield.get(key).toString());
                    if (key.contains("shipping_tier"))
                        params.putString(FirebaseAnalytics.Param.SHIPPING_TIER, actionfield.get(key).toString());
                    if (key.contains("payment_type"))
                        params.putString(FirebaseAnalytics.Param.PAYMENT_TYPE, actionfield.get(key).toString());
                }
            }
        } catch (Exception ex) {
            Log.i("GAv4_transaction", ex.getMessage());
        }

        if (obj_ecommerce.has("Products")) {
            try {
                JSONArray product_array = obj_ecommerce.getJSONArray("Products");
                List product_list = ConvertJsonArray(product_array);
                ArrayList items = new ArrayList();
                for (int i = 0; i < product_list.size(); i++) {
                    HashMap<String, Object> product_Hashmap = (HashMap<String, Object>) product_list.get(i);
                    Bundle item = new Bundle();
                    Iterator<String> keys = product_Hashmap.keySet().iterator();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        if (key.equals("item_id")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_ID, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.equals("item_name")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_NAME, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("item_brand")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_BRAND, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("item_category")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, product_Hashmap.get(key).toString());
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
                        if (key.contains("item_variant")) {
                            item.putString(FirebaseAnalytics.Param.ITEM_VARIANT, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("coupon")) {
                            item.putString(FirebaseAnalytics.Param.COUPON, product_Hashmap.get(key).toString());
                            continue;
                        }
                        if (key.contains("position")) {
                            Object position_value = product_Hashmap.get(key);
                            String position = String.valueOf(position_value);
                            item.putLong(FirebaseAnalytics.Param.INDEX, Integer.parseInt(position));
                            continue;
                        }
                        if(key.contains("list_name")){
                            item.putString(FirebaseAnalytics.Param.ITEM_LIST_NAME,product_Hashmap.get(key).toString());
                            continue;
                        }
                        if(key.contains("list_id")){
                            item.putString(FirebaseAnalytics.Param.ITEM_LIST_ID,product_Hashmap.get(key).toString());
                            continue;
                        }
                    }
                    items.add(item);
                }
                params.putParcelableArrayList(FirebaseAnalytics.Param.ITEMS, items);

            } catch (Exception ex) {
                Log.i("GAv4_Products", ex.getMessage());
            }
        }
        return params;
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
}