package com.yxj.rxjavayxj.rxjava;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Author:  Yxj
 * Time:    2019/5/2 下午9:12
 * -----------------------------------------
 * Description:
 */
public class RxView {

    public static Observable<String> textChanges(final EditText editText){
        return Observable.create(new Observable<String>() {
            @Override
            public void subscribe(final Observer<String> observer) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        observer.onNext(s.toString());
                    }
                });
            }
        });
    }
}
