package com.yxj.rxjavayxj.rxjava;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Author:  Yxj
 * Time:    2019/3/22 上午11:37
 * -----------------------------------------
 * Description:
 */
public class RxEditText {

    public static Upstream<String> textChanges(final EditText editText){
        return Upstream.createUpstream(new Upstream<String>() {
            @Override
            public void subscribe(final Downstream<String> downstream) {
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        downstream.onNext(s.toString());
                    }
                });
            }
        });
    }
}
