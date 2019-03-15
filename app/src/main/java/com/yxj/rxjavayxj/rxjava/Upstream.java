package com.yxj.rxjavayxj.rxjava;

/**
 * Author:  Yxj
 * Time:    2019/3/15 上午9:50
 * -----------------------------------------
 * Description: 上游
 */
public abstract class Upstream<T> {

    public abstract void subscribe(Downstream<T> downstream);

    public static <T> Upstream<T> createUpstream(final UpstreamSource<T> source){
        return new Upstream<T>(){
            @Override
            public void subscribe(Downstream<T> downFlow) {
                source.call(downFlow);
            }
        };
    }

    public interface UpstreamSource<T>{
        void call(Downstream<T> downstream);
    }

}
