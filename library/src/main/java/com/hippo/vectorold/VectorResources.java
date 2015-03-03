package com.hippo.vectorold;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.LongSparseArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class VectorResources {
    private static final String TAG = VectorResources.class.getSimpleName();

    private static final boolean GET_OLD_DRAWABLE = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

    private final Context mContext;
    private final Resources mResources;

    // These are protected by mAccessLock.
    private final Object mAccessLock = new Object();

    private TypedValue mTmpValue = new TypedValue();

    private final LongSparseArray<WeakReference<Drawable.ConstantState>> mDrawableCache
            = new LongSparseArray<>(0);

    private static final SparseArray<WeakReference<VectorResources>> sInstanceCache
            = new SparseArray<>(0);

    public static VectorResources getVectorResources(Context context) {
        Resources resources = context.getResources();
        int key = resources.hashCode();
        WeakReference<VectorResources> wr = sInstanceCache.get(key);
        if (wr != null) {   // we have the key
            VectorResources entry = wr.get();
            if (entry != null) {
                //Log.i(TAG, "Returning cached drawable @ #" +
                //        Integer.toHexString(((Integer)key).intValue())
                //        + " in " + this + ": " + entry);
                return entry;
            }
            else {  // our entry has been purged
                sInstanceCache.delete(key);
            }
        }

        VectorResources vr = new VectorResources(context);
        sInstanceCache.put(key, new WeakReference<>(vr));
        return vr;
    }

    private VectorResources(Context context) {
        mContext = context;
        mResources = context.getResources();
    }

    public Drawable getDrawable(int id) throws Resources.NotFoundException {
        if (GET_OLD_DRAWABLE) {
            return getOldDrawable(id);
        } else {
            return mResources.getDrawable(id);
        }
    }

    private Drawable getOldDrawable(int id) throws Resources.NotFoundException {
        TypedValue value;
        synchronized (mAccessLock) {
            value = mTmpValue;
            if (value == null) {
                value = new TypedValue();
            } else {
                mTmpValue = null;
            }
            mResources.getValue(id, value, true);
        }
        Drawable res = loadDrawable(value, id);
        synchronized (mAccessLock) {
            if (mTmpValue == null) {
                mTmpValue = value;
            }
        }
        return res;
    }

    private Drawable loadDrawable(TypedValue value, int id)
            throws Resources.NotFoundException {

        final LongSparseArray<WeakReference<Drawable.ConstantState>> caches = mDrawableCache;
        final long key = (((long) value.assetCookie) << 32) | value.data;

        Drawable dr = getCachedDrawable(caches, key);

        if (dr != null) {
            return dr;
        }

        dr = createDrawable(id);

        synchronized (mAccessLock) {
            //Log.i(TAG, "Saving cached drawable @ #" +
            //        Integer.toHexString(key.intValue())
            //        + " in " + this + ": " + cs);
            mDrawableCache.put(key, new WeakReference<>(dr.getConstantState()));
        }

        return dr;
    }

    private Drawable getCachedDrawable(
            LongSparseArray<WeakReference<Drawable.ConstantState>> drawableCache,
            long key) {
        synchronized (mAccessLock) {
            WeakReference<Drawable.ConstantState> wr = drawableCache.get(key);
            if (wr != null) {   // we have the key
                Drawable.ConstantState entry = wr.get();
                if (entry != null) {
                    //Log.i(TAG, "Returning cached drawable @ #" +
                    //        Integer.toHexString(((Integer)key).intValue())
                    //        + " in " + this + ": " + entry);
                    return entry.newDrawable(mResources);
                }
                else {  // our entry has been purged
                    drawableCache.delete(key);
                }
            }
        }
        return null;
    }

    private Drawable createDrawable(int id) {
        try {
            final Drawable drawable;
            final XmlPullParser parser = mResources.getXml(id);
            final AttributeSet attrs = Xml.asAttributeSet(parser);
            int type;
            while ((type=parser.next()) != XmlPullParser.START_TAG &&
                    type != XmlPullParser.END_DOCUMENT) {
                // Empty loop
            }
            if (type != XmlPullParser.START_TAG) {
                throw new XmlPullParserException("No start tag found");
            }

            final String name = parser.getName();
            if (name.equals("vector")) {
                VectorDrawable vectorDrawable = new VectorDrawable();
                vectorDrawable.inflate(mResources, parser, attrs);
                drawable = vectorDrawable;
            } else if (name.equals("animated-vector")) {
                AnimatedVectorDrawable animatedVectorDrawable = new AnimatedVectorDrawable();
                animatedVectorDrawable.inflate(mContext, parser, attrs);
                drawable = animatedVectorDrawable;
            } else {
                throw new XmlPullParserException(parser.getPositionDescription() +
                        ": invalid drawable tag " + name);
            }

            return drawable;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "parser error", e);
        } catch (IOException e) {
            Log.e(TAG, "parser error", e);
        }
        return null;
    }
}
