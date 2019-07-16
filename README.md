# RulerDemo

通过使用自定义 View 来实现刻度尺。

- **RulerView** (com.android.ruler.widget.RulerView)

  通过滑动指针调节频道

- **RulerView2** (com.android.ruler.widget.RulerView2)

  通过滑动刻度调节频道

具体实现见代码，有详细注释。

![RulerView 和 RulerView2](https://github.com/Edger/RulerDemo/blob/master/ScreenRecord/Screenrecorder.gif)

---

基础相关：

- 自定义 View 的实现和绘制流程

  - onMeasure()：对 View 进行测量

  - onDraw()：绘制自定义 View

  - onTouchEvent()：处理触摸事件

- Paint 的使用

- Canvas 的使用

---

参考文章如下：

- [带你实现漂亮的滑动卷尺](https://www.jianshu.com/p/06e65ef3f3f1)
- [带你实现漂亮的滑动卷尺 - 源码](https://github.com/jdqm/TapeView)
- [Android 自定义View：实现一个 FM 刻度尺](https://juejin.im/post/5d0afe1f51882508be27a504)
- [Android 自定义View：实现一个 FM 刻度尺 - 源码](https://github.com/gs666/RulerDemo)
- [drawText 之坐标、居中、绘制多行（推荐）](https://github.com/GcsSloop/AndroidNote/blob/master/CustomView/Advance/%5B99%5DDrawText.md)
- [Android 图解 Canvas drawText 文字居中的那些事](https://blog.csdn.net/kong_gu_you_lan/article/details/78927930)
