package com.hippo.vectorold;

import android.content.Context;
import android.os.Build;

public final class Vector {

    public static Context wrapContext(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? new VectorContext(context) : context;
    }
}
