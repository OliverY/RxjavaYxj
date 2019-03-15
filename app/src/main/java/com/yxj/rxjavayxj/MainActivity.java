package com.yxj.rxjavayxj;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.yxj.rxjavayxj.rxjava.Downstream;
import com.yxj.rxjavayxj.rxjava.Upstream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });

    }

    private void test() {

        Upstream.createUpstream(new Upstream.UpstreamSource<String>() {
            @Override
            public void call(Downstream<String> downstream) {
                downstream.onNext("hello world");
                downstream.onComplete();
            }
        })
        .subscribe(new Downstream<String>() {
            @Override
            public void onNext(String s) {
                Log.e(TAG,s);
            }

            @Override
            public void onComplete() {
                Log.e(TAG,"onComplete");
            }
        });

    }
}
