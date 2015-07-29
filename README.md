# VectorOld

VectorDrawable，AnimatedVectorDrawable 和 PathInterpolator 的兼容库。API 14 以上应该都能用。<br>
注意：`R.styleable.PropertyAnimator_pathData`，`R.styleable.PropertyAnimator_propertyXName` 和 `R.styleable.PropertyAnimator_propertyYName` 未移植。

A backport of VectorDrawable, AnimatedVectorDrawable and PathInterpolator. It might work in API 14+.<br>
Note: `R.styleable.PropertyAnimator_pathData`, `R.styleable.PropertyAnimator_propertyXName` and `R.styleable.PropertyAnimator_propertyYName` is unavailable.

# Usage

## XML Resources

XML 资源的写法与在 Lollipop 下的写法基本相同，不过为了兼容 pre-Lollipop 系统需要添加一些内容。<br>
VectorDrawable 的例子如下
```xml
<vector
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:auto="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    tools:targetApi="21">

    auto:height="24dp"
    auto:width="24dp"
    auto:viewportWidth="24"
    auto:viewportHeight="24"

    android:height="24dp"
    android:width="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">

    <path
        auto:fillColor="#000"
        auto:pathData="@string/pd_lightbulb"

        android:fillColor="#000"
        android:pathData="@string/pd_lightbulb"/>

</vector>
```

AnimatedVectorDrawable 的例子如下
```xml
<animated-vector
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:auto="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    tools:targetApi="21"

    auto:drawable="@drawable/vector_drawable_progress_bar_large"
    android:drawable="@drawable/vector_drawable_progress_bar_large">

    <target
        auto:name="progressBar"
        auto:animation="@anim/progress_indeterminate_material"

        android:name="progressBar"
        android:animation="@anim/progress_indeterminate_material"/>

    <target
        auto:name="root"
        auto:animation="@anim/progress_indeterminate_rotation_material"

        android:name="root"
        android:animation="@anim/progress_indeterminate_rotation_material"/>

</animated-vector>
```

# Build
A package private API is uesd. Use this [android.jar](https://mega.co.nz/#!NpNlRDxY!fdpka-oqfHm2XoF1IDxrUFITn2xs5x3wQMT1jQ4TJT4) to build. The android.jar is base on API 22.

# Thanks
[MrVector](https://github.com/telly/MrVector)<br>
这是一个与 VectorOld 类似的项目。VectorOld 参考了其中的某些用法与技巧。
