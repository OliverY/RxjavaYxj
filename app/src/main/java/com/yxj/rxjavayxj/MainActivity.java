package com.yxj.rxjavayxj;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.yxj.rxjavayxj.api.Api;
import com.yxj.rxjavayxj.rxjava.Downstream;
import com.yxj.rxjavayxj.rxjava.Function;
import com.yxj.rxjavayxj.rxjava.RxEditText;
import com.yxj.rxjavayxj.rxjava.Upstream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_simple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simple();
            }
        });
        findViewById(R.id.btn_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map();
            }
        });
        findViewById(R.id.btn_onobserve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                observe();
            }
        });
        findViewById(R.id.btn_onsubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribe();
            }
        });
        findViewById(R.id.btn_compose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compose();
            }
        });
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        EditText et = findViewById(R.id.et);
        addEditTextWatcher(et);
    }

    private void simple() {
        Upstream.createUpstream(new Upstream<String>() {
            @Override
            public void subscribe(Downstream<String> downstream) {
                downstream.onNext("hello");
            }
        })
        .subscribe(new Downstream<String>() {
            @Override
            public void onNext(String s) {
                Log.e(TAG, s);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 模拟map操作符
     */
    private void map() {
        Upstream.createUpstream(new Upstream<String>() {
            @Override
            public void subscribe(Downstream<String> downstream) {
                downstream.onNext("hello");
                downstream.onNext("world");
                downstream.onNext("i love you");
            }
        })
        .map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) throws Exception {
                return s.length() > 5;
            }
        })
        .subscribe(new Downstream<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.e(TAG, "result:" + aBoolean);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    /**
     * 模拟observeOn操作符
     */
    private void observe() {
        Upstream.createUpstream(new Upstream<String>() {
            @Override
            public void subscribe(Downstream<String> downstream) {
                downstream.onNext("hello");
                downstream.onNext("world");
                downstream.onNext("i love you");
            }
        })
        .map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) throws Exception {
                return s.length() > 5;
            }
        })
        .observeOnMainThread() // 相当于 observeOn(AndroidThread.mainThread())
        .subscribe(new Downstream<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.e(TAG, "result:" + aBoolean);
            }

            @Override
            public void onComplete() {

            }
        });

    }

    /**
     * 模拟subscribeOn操作符
     */
    private void subscribe() {
        Upstream.createUpstream(new Upstream<String>() {
            @Override
            public void subscribe(Downstream<String> downstream) {
                downstream.onNext("hello world");
                downstream.onNext("good boy");
                downstream.onNext("see u");
                downstream.onComplete();
            }

        })
        .map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) throws Exception {
                return s.length() > 6;
            }
        })
        .observeOnMainThread() // 相当于 observeOn(AndroidThread.mainThread())
        .subscribe(new Downstream<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.e(TAG, "result:" + aBoolean);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        });
    }

    /**
     * 模拟compose操作符
     */
    private void compose() {

        Upstream.createUpstream(new Upstream<String>() {
            @Override
            public void subscribe(Downstream<String> downstream) {
                downstream.onNext("hello world");
                downstream.onNext("good boy");
                downstream.onNext("see u");
                downstream.onComplete();
            }

        })
        .map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) throws Exception {
                return s.length() > 6;
            }
        })
        .compose()
        .subscribe(new Downstream<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                Log.e(TAG, "result:" + aBoolean);
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete");
            }
        });
    }

    /**
     * 模拟用Rxjava封装网络访问
     */
    private void login() {
        String userName = "yxj";
        String password = "123456";

        Api.login(userName,password)
                .subscribe(new Downstream<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.e(TAG,s);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void addEditTextWatcher(final EditText editText){
        RxEditText.textChanges(editText)
                .subscribeOnNewThread()
                .observeOnMainThread()
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) throws Exception {
                        return s.length()>6;
                    }
                })
                .subscribe(new Downstream<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        editText.setTextColor(aBoolean? Color.RED:Color.GREEN);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

}
