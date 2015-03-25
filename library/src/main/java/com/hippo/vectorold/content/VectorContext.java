package com.hippo.vectorold.content;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.content.res.VectorResources;
import android.os.Build;

public class VectorContext extends ContextWrapper {

    private VectorResources mVectorResources;

    public VectorContext(Context base) {
        super(base);
    }

    @Override
    public Resources getResources() {
        final Resources superResources = super.getResources();
        if (mVectorResources == null || mVectorResources.isBase(superResources)) {
            mVectorResources = new VectorResources(this, superResources);
        }
        return mVectorResources;
    }

    /**
     * In {@link android.content.ContextWrapper#attachBaseContext(Context)},
     * do <code>super.attachBaseContext(Vector.wrapContext(newBase));</code>.
     *
     * @param context the context
     * @return new context
     */
    public static Context wrapContext(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? new VectorContext(context) : context;
    }
}
