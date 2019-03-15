package com.yxj.rxjavayxj.rxjava;

/**
 * Author:  Yxj
 * Time:    2019/3/15 下午3:42
 * -----------------------------------------
 * Description:
 */
public interface UpstreamSource<T>{
    void subscribe(Downstream<T> downstream);
}