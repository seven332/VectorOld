package com.hippo.vectorold;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class InterpolatorInflater {

    private static final boolean USE_CUSTOM_PATH_INTERPOLATOR = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;

    /**
     * Loads an {@link Interpolator} object from a resource
     *
     * @param context The Context
     * @param id The resource id of the animation to load
     * @return The interpolator object reference by the specified id
     * @throws Resources.NotFoundException
     */
    public static Interpolator loadInterpolator(Context context, int id) throws Resources.NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createInterpolatorFromXml(context, parser);
        } catch (XmlPullParserException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } catch (IOException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x" +
                    Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } finally {
            if (parser != null)
                parser.close();
        }

    }

    private static Interpolator createInterpolatorFromXml(Context context, XmlPullParser parser)
            throws XmlPullParserException, IOException {

        Interpolator interpolator = null;

        // Make sure we are on a start tag.
        int type;
        int depth = parser.getDepth();

        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            AttributeSet attrs = Xml.asAttributeSet(parser);

            String name = parser.getName();

            if (name.equals("linearInterpolator")) {
                interpolator = new LinearInterpolator();
            } else if (name.equals("accelerateInterpolator")) {
                interpolator = new AccelerateInterpolator(context, attrs);
            } else if (name.equals("decelerateInterpolator")) {
                interpolator = new DecelerateInterpolator(context, attrs);
            } else if (name.equals("accelerateDecelerateInterpolator")) {
                interpolator = new AccelerateDecelerateInterpolator();
            } else if (name.equals("cycleInterpolator")) {
                interpolator = new CycleInterpolator(context, attrs);
            } else if (name.equals("anticipateInterpolator")) {
                interpolator = new AnticipateInterpolator(context, attrs);
            } else if (name.equals("overshootInterpolator")) {
                interpolator = new OvershootInterpolator(context, attrs);
            } else if (name.equals("anticipateOvershootInterpolator")) {
                interpolator = new AnticipateOvershootInterpolator(context, attrs);
            } else if (name.equals("bounceInterpolator")) {
                interpolator = new BounceInterpolator();
            } else if (name.equals("pathInterpolator")) {
                interpolator = USE_CUSTOM_PATH_INTERPOLATOR ? new PathInterpolator(context, attrs) :
                        new android.view.animation.PathInterpolator(context, attrs);
            } else {
                throw new RuntimeException("Unknown interpolator name: " + parser.getName());
            }

        }

        return interpolator;
    }
}
