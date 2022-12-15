package com.pdi.test;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

//import com.gp.ga.webview.GAWebViewInterface;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pdi.test.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {
    private ActivityWebViewBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //모바일 앱에서 호출 여부를 체크하기 위한 UserAgent 추가
        String userAgent = binding.webview.getSettings().getUserAgentString();
        binding.webview.getSettings().setUserAgentString(userAgent + "/GA_Android");

        //webview에서 javascript를 사용 할 수 있도록 설정
        binding.webview.getSettings().setJavaScriptEnabled(true);

        //웹뷰와 네이티브간의 통신을 위한 javascript interface 를 추가.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            binding.webview.addJavascriptInterface(new WebAppInterface(this), "gascriptAndroid");
        } else {
            Log.w(this.getClass().getName(), "Not adding JavaScriptInterface, API Version: " + Build.VERSION.SDK_INT);
        }

        binding.webview.loadUrl("http://10.0.2.2:3001");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "TEST_SCREEN_NAME");
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "TEST_CLASS_NAME");
        FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }
}