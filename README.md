> 9行代码诠释Rxjava运行原理。

你是不是看过了很多分析Rxjava源码的文章，但依旧无法在心中勾勒出Rxjava原理的样貌。是什么让我们阅读Rxjava源码变得如此艰难？是Rxjava的代码封装。本文中我把Rxjava的各种封装、抽象统统剥去，让最纯粹的Rxjava就赤裸裸的站在你面前，让你想不要都难。

本文与其它Rxjava源码分析文章最大的不同之处在于，不是拿着源码来分析，而是自己根据Rxjava源码思想，重新写一套极简版本的Rxjava实现。不做封装，仅保留了最核心的部分。你都能自己实现极简版的Rxjava了，何愁看不懂Rxjava源码。而其中最关键的是理解“事件变换的9行代码”，弄清楚这9行代码，Rxjava原理就变得清晰可见。

[简书](https://www.jianshu.com/p/ba1835f65f89)

---

#### 本文分为：
- 手写Rxjava核心代码，create，nullMap（核心）操作符
- map，observeOn，subscribeOn，flatMap操作符
- 响应式编程的思想

---
### 手写Rxjava核心代码，create，nullMap操作符


我们先来看一个最简单调用

```
MainActivity.java

Observable.create(new Observable<String>() {
            @Override
            public void subscribe(Observer<String> observer) {
                observer.onNext("hello");
                observer.onNext("world");
                observer.onComplete();
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onNext(String s) {
                Log.e("yxj",s);
            }

            @Override
            public void onComplete() {
                Log.e("yxj","onComplete");
            }
        });
```
```
Observable.java

public abstract class Observable<T> {

    public abstract void subscribe(Observer<T> observer);

    public static <T> Observable<T> create(Observable<T> observable){
        return observable;
    }

}
```

```
Observer.java

public interface Observer<T> {

    void onNext(T t);
    void onComplete();
}
```
######  本篇文章我把Observable称为“节点”，Observer称为“处理者”，一是因为我被观察者、被观察者、谁订阅谁给绕晕了，再一个是我觉得这个名称比较符合Rxjava的设计思想。

我们先来看看Observable类，create方法是一个工厂方法，创建一个自己的对象。重写subscribe方法说：**如果** 我有一个处理者Observer，我就把“hello”，“world”交给它处理。

Observable调用了subscribe方法，真的找到了Observer。于是兑现承诺，完成整个调用逻辑。
> 请记住1点：
**这里是“如果”有处理者，要subscribe方法被调用时，“如果”才成立。这一点很重要，因为Rxjava就是建立在一系列的“如果”（回调）操作上的。**

#### “nullMap”操作符
```
1.创建一个observable
2.调用空map操作符做变换
3.交给observer处理

MainActivity.java

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

```


```
nullMap()等价于 下面这段代码
即把上个节点的数据不做任何修改的传递给下一节点的map操作
 
.map(new Function<String, String>() {
    @Override
    public String apply(String s) throws Exception {
        return s;
    }
})

```
**"nullMap"操作符在Rxjava源码里并不存在**，是我方便大家理解Rxjava运行机制写出来的。
nullMap操作是一个 **base变换操作符**，map，flatMap，subscribeOn，observeOn操作符都是基于nullMap修改而来。所以Rxjava真正的核心就是nullMap操作符，也就9行代码。

下面我们看看nullMap的内部实现，这9行代码都干了什么。

```
Observable.java
// 这就是Rxjava的核心

public Observable<T> nullMap() {

        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observerC) {

                Observer<T> observerB = new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        observerC.onNext(t);
                    }

                    @Override
                    public void onComplete() {
                        observerC.onComplete();
                    }
                };
                Observable.this.subscribe(observerB);
            }
        };
    }
```


“nullMap”操作符做了件什么事情：

1. 上一个节点Observable A调用nullMap(),在内部new一个新的节点Observable B。
2. 节点B重写subscribe方法，说"如果"自己有操作者Observer C，就new一个操作者Observer B，然后让节点A subscribe 操作者B。
3. 节点A subscribe 操作者B，让操作者B执行onNext方法。操作者B的onNext方法内部，调用了操作者C的onNext。从而完成了整个调用。

请注意2中的”如果“。意味着，当节点B中的subscribe方法没有被调用的时候，2，3步骤都不会执行（他们都是回调），没有Observer B，节点A也不会调用subscribe方法。
接下来分两种情况：

- 节点B调用了subscribe方法，则执行2，3，完成整个流程。
- 节点B调用nullMap，从新走一遍1，2，3步骤，相当于节点B把任务交给了下一个节点C。

> 概括一下就是：
> 
> **Observable的每用一个操作符，其实就是创建一个新的节点，把任务传递给了新的节点。新节点内部通过subscribe方法“逆向的”与上一节点关联。新节点中的new出来的Observer内的onNext方法中做了和最终处理者（或下一个节点的处理者）之间的关联。**

#### 图文详细解说nullMap调用过程

##### 第一阶段：许诺阶段
![part1.png](https://upload-images.jianshu.io/upload_images/1538674-3fa6105a6d816f8a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


##### 注：虚线代表还未执行到，方法体或对象 仅存在于某个回调函数中，是一个“如果”。不好意思，图里面的Observable C应该改成 Observer C，下面的图也是。

代表还未最终subscribe一个Observer，图中步骤1

1. 节点A调用map方法，在内部创建了一个新的节点B
2. 节点B这时候对节点A做了个承诺：**“如果”**我有处理者Observer C，那我就内部new一个 Observer B给你（节点A）用”
3. 节点B中的操作者Observer B内部做了与Observer C的衔接工作（function操作）

###### 这一阶段：主要就是节点与节点之间做连接，之间有各种“如果”（回调）的承诺。


##### 第二阶段：兑现承诺

![part2.png](https://upload-images.jianshu.io/upload_images/1538674-6c0400e1be014e14.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


这一阶段是subscribe方法被调用，传入了最终的Observer。图中步骤2、3、4，步骤3有2个箭头，代表的就是传入了一个ObserverC

1. 节点B调用subscribe方法，找到了处理者Observer C
2. 此时兑现对节点A的承诺：**“如果”**我有处理者Observer C，那我就内部new一个 Observer B给你（节点A）用”
3. 通知节点A在subscribe方法调用Observer B

###### 这一阶段：是把原来各个环节的“如果”一一兑现的过程，从最末一个Observable的subscribe方法开始，按节点顺序逆向的兑现承诺。每个subscribe方法内部都会新建一个处理者Observer，然后将Observer交给上一个节点Observable的subcriber方法。这是一个逆序的过程。

##### 第三阶段：执行业务

![part3.png](https://upload-images.jianshu.io/upload_images/1538674-c3a9c46acef55fb8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

图中步骤5、6

1. 节点A调用subscribe（这个subscribe方法是我们调用create方法创建Observable中重写的），开始执行subscribe方法，把一开始的任务“hello”，“world”交给Observer B
2. Observer B的onNext()方法中，调用了Observer C执行onNext()
3. 然后ObserverC中执行了onNext方法（ObserverC是程序员自己传入的）

###### 这一阶段：是通过各个节点的Observer顺序执行具体的业务操作的过程，只有这个阶段是与具体业务相关的阶段。大家可以先思考一下，如果是一个普通的map(Function function)，这个变换发生在哪？

答案是：Observer B的内部的onNext方法中。

[github上有nullMap详细注释版的代码](https://github.com/OliverY/RxjavaYxj)


##### 整个Rxjava就是这9行核心变换代码了，你能不能根据nullMap源码先自己写出map，observeOn，subscribeOn，flatMap呢


---

### map，observeOn，subscribeOn，flatMap操作符

让我们瞬间搞明白这4个操作符的原理。仅仅是在nullMap中添加了几行代码而已。
[操作符源码](https://github.com/OliverY/RxjavaYxj/blob/master/app/src/main/java/com/yxj/rxjavayxj/rxjava/Observable.java)

#### map操作符

```
Observable.java

public <R> Observable<R> map(final Function<T, R> function) {

        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> observer1) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        R r = function.apply(t); // 仅仅在这里加了变换操作
                        observer1.onNext(r);
                    }

                    @Override
                    public void onComplete() {
                        observer1.onComplete();
                    }
                });
            }
        };
    }
```

和“nullMap”相比，仅仅加了一行代码function.apply() 方法的调用。


#### observeOn操作符

```
Observable.java

public Observable<T> observeOn() {
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(final T t) {
							//模拟切换到主线程（通常上个节点是运行在子线程的情况）
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                observer.onNext(t);
                            }
                        });
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
            }
        };
    }
```

与“nullMap”相比，修改了最内部的onNext方法执行所在的线程。Rxjava源码会更加灵活，observerOn方法参数让你可以指定切换到的线程，其实就是传入了一个线程调度器，用于指定observer.onNext()方法要在哪个线程执行。原理是一样的。我这里就简写，直接写了切换到主线程，这你肯定能看明白。

#### subscribeOn操作符
```
Observable.java

public Observable<T> subscribeOn() {
        return new Observable<T>() {
            @Override
            public void subscribe(final Observer<T> observer) {
                
                new Thread() {
                    @Override
                    public void run() {
                    // 这里简写了，没有new Observer做中转，github上有完整代码
                        Observable.this.subscribe(observer);
                    }
                }.start();
            }
        };
    }
```
将上一个节点切换到新的线程，修改了Observable.this.subscribe()运行的线程，Observable.this指的是调用subscribeOn()的Observable，即上一个节点。因此subscribeOn操作符修改了上一个节点的运行所在的线程

#### flatMap操作符
```
public <R> Observable<R> flatMap(final Function<T, Observable<R>> function) {

        return new Observable<R>() {
            @Override
            public void subscribe(final Observer<R> observer) {
                Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T t) {
                        try {
                            Observable<R> observable = function.apply(t);
                            observable.subscribe(observer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }
        };

    }
```


其实flatmap和map极为相似，只不过function.apply()的返回值是一个Observable。

Observable是一个节点，既可以用来封装异步操作，也可以用来封装同步操作（封装同步操作 == map操作符）。所以这样就可以很方便的写出一个
耗时1操作 —> 耗时2操作 —> 耗时3操作...的操作

##### 是不是很简单，弄懂nullMap的9行代码，剩下的都是在nullMap中修改而来的。理解nullMap，自己也可以写一个Rxjava，然后再去重新看Rxjava源码，你会发现源码会变得如此清晰。
---

### 响应式编程的思想

> 响应式编程是一种面向数据流和变化传播的编程范式。

直接看这句话其实不太容易理解。让我们换个说法，实际编程中是什么会干扰我们，使我们无法专注于数据流和变化传播呢？答案是：**异步**，它会让我们的代码形成嵌套，不够顺序化。

> 因为异步，我们的业务逻辑会写成回调嵌套的形式，导致过一段时间看自己代码看不懂，语义化不强，不是按着顺序一个节点一个节点的往下执行的。
>
> **Rxjava（RxDart也是、ReactiveX应该都是这种思想）将所有的业务操作变成一步一步，每一步不管你是同步、异步，统统用一个节点包裹起来，节点与节点之间是同步调用的关系。如此，整个代码的节点都是按顺序执行的。**
>
> 所以代码看起来语义性非常强，**所写即所思**。
