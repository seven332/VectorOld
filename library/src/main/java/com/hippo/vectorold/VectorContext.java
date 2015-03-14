package com.hippo.vectorold;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.content.res.VectorResources;

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
}
