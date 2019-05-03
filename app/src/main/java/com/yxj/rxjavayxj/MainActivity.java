package com.yxj.rxjavayxj;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.yxj.rxjavayxj.api.Api;
import com.yxj.rxjavayxj.rxjava.Function;
import com.yxj.rxjavayxj.rxjava.Observable;
import com.yxj.rxjavayxj.rxjava.Observer;
import com.yxj.rxjavayxj.rxjava.RxView;

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
        findViewById(R.id.btn_null_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nullmap();
            }
        });
        findViewById(R.id.btn_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map();
            }
        });
        findViewById(R.id.btn_subscribeon_and_observeon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeOnAndObservOn();
            }
        });
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        findViewById(R.id.btn_flatmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flatMap();
            }
        });

        EditText et = findViewById(R.id.et);
        addEditTextWatcher(et);
    }

    private void simple() {
        Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        })
        .subscribe(new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.e("yxj",s);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });
    }

    /**
     * 空map 操作符
     */
    private void nullmap() {
        Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        })
        .nullMap()
        .subscribe(new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.e("yxj",s);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });

    }

    /**
     * map操作符
     */
    private void map(){
        Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        })
        .map(new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return s.length();
            }
        })
        .subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer i) {
                Log.e("yxj","i:"+i);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });
    }

    /**
     * 模拟subscribeOn、ObservOn操作符
     */
    private void subscribeOnAndObservOn() {
        Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onNext("i love you");
            }
        })
        .subscribeOn()
        .observeOn()
        .map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String s) {
                return s.length() > 5;
            }
        })
        .subscribe(new Observer<Boolean>() {
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
                .subscribeOn()// 上游切换到子线程
                .observeOn() // 下游切换到主线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.e(TAG,s);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });
    }

    /**
     * 模拟简版的flatMap
     */
    private void flatMap(){
        final String userName = "yxj";
        String password = "123456";
        Api.login(userName,password)
                .subscribeOn()// 上游切换到子线程
                .flatMap(new Function<String, Observable<String>>() {
                    @Override
                    public Observable<String> apply(String s) {
                        String userName2 = "zsh";
                        String password2 = "123456";
                        return Api.login(userName2,password2);
                    }
                })
                .observeOn()
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                        Log.e(TAG, "result:" + s);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete");
                    }
                });

    }

    /**
     * 给edittext添加观察者
     * @param editText
     */
    private void addEditTextWatcher(final EditText editText){
        RxView.textChanges(editText)
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) {
                        return s.length()>4;
                    }
                })
                .subscribe(new Observer<Boolean>() {
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
