package android.content.res;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LongSparseArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.util.Xml;

import com.hippo.vectorold.drawable.AnimatedVectorDrawable;
import com.hippo.vectorold.drawable.VectorDrawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

/**
 * It is a wrap
 */
public class VectorResources extends Resources {

    private Context mContext;
    private Resources mBase;

    // These are protected by mAccessLock.
    final Object mAccessLock = new Object();
    final LongSparseArray<WeakReference<Drawable.ConstantState> > mDrawableCache
            = new LongSparseArray<>(0);
    final LongSparseArray<WeakReference<Drawable.ConstantState> > mColorDrawableCache
            = new LongSparseArray<>(0);

    public VectorResources(Context context, Resources res) {
        super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
        mContext = context;
        mBase = res;
    }

    public boolean isBase(Resources res) {
        return mBase == res;
    }

    @Override
    Drawable loadDrawable(TypedValue value, int id)
            throws NotFoundException {
        boolean isColorDrawable = false;
        if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
                value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            isColorDrawable = true;
        }
        final long key = isColorDrawable ? value.data :
                (((long) value.assetCookie) << 32) | value.data;

        Drawable dr = getCachedDrawable(isColorDrawable ? mColorDrawableCache : mDrawableCache, key);

        if (dr != null) {
            return dr;
        }

        if (isColorDrawable) {
            dr = new ColorDrawable(value.data);
        }

        if (dr == null) {
            if (value.string == null) {
                throw new NotFoundException(
                        "Resource is not a Drawable (color or path): " + value);
            }

            String file = value.string.toString();

            if (file.endsWith(".xml")) {
                try {
                    XmlResourceParser rp = getXml(id);
                    dr = createDrawableFromXml(rp);
                    rp.close();
                } catch (Exception e) {
                    NotFoundException rnf = new NotFoundException(
                            "File " + file + " from drawable resource ID #0x"
                                    + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }

            } else {
                try {
                    InputStream is = openRawResource(id, value);
                    dr = Drawable.createFromResourceStream(this, value, is,
                            file, null);
                    is.close();
                    //                System.out.println("Created stream: " + dr);
                } catch (Exception e) {
                    NotFoundException rnf = new NotFoundException(
                            "File " + file + " from drawable resource ID #0x"
                                    + Integer.toHexString(id));
                    rnf.initCause(e);
                    throw rnf;
                }
            }
        }
        Drawable.ConstantState cs;
        if (dr != null) {
            dr.setChangingConfigurations(value.changingConfigurations);
            cs = dr.getConstantState();
            if (cs != null) {
                synchronized (mAccessLock) {
                    if (isColorDrawable) {
                        mColorDrawableCache.put(key, new WeakReference<>(cs));
                    } else {
                        mDrawableCache.put(key, new WeakReference<>(cs));
                    }
                }
            }
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
                    return entry.newDrawable(this);
                } else {  // our entry has been purged
                    drawableCache.delete(key);
                }
            }
        }
        return null;
    }

    private Drawable createDrawableFromXml(XmlResourceParser parser)
            throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);

        int type;
        while ((type=parser.next()) != XmlPullParser.START_TAG &&
                type != XmlPullParser.END_DOCUMENT) {
            // Empty loop
        }

        if (type != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        Drawable drawable = createDrawableFromXmlInner(parser, attrs);

        if (drawable == null) {
            throw new RuntimeException("Unknown initial tag: " + parser.getName());
        }

        return drawable;
    }

    private Drawable createDrawableFromXmlInner(XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException {
        final Drawable drawable;
        final String name = parser.getName();
        switch (name) {
            case "vector":
                VectorDrawable vectorDrawable = new VectorDrawable();
                vectorDrawable.inflate(this, parser, attrs);
                drawable = vectorDrawable;
                break;
            case "animated-vector":
                AnimatedVectorDrawable animatedVectorDrawable = new AnimatedVectorDrawable();
                animatedVectorDrawable.inflate(mContext, parser, attrs);
                drawable = animatedVectorDrawable;
                break;
            default:
                drawable = Drawable.createFromXmlInner(this, parser, attrs);
        }
        return drawable;
    }
}
