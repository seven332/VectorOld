/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hippo.vectorold;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.animation.Interpolator;

/**
 * An interpolator that can traverse a Path that extends from <code>Point</code>
 * <code>(0, 0)</code> to <code>(1, 1)</code>. The x coordinate along the <code>Path</code>
 * is the input value and the output is the y coordinate of the line at that point.
 * This means that the Path must conform to a function <code>y = f(x)</code>.
 *
 * <p>The <code>Path</code> must not have gaps in the x direction and must not
 * loop back on itself such that there can be two points sharing the same x coordinate.
 * It is alright to have a disjoint line in the vertical direction:</p>
 * <p><blockquote><pre>
 *     Path path = new Path();
 *     path.lineTo(0.25f, 0.25f);
 *     path.moveTo(0.25f, 0.5f);
 *     path.lineTo(1f, 1f);
 * </pre></blockquote></p>
 */
public class PathInterpolator implements Interpolator {

    // This governs how accurate the approximation of the Path is.
    private static final float PRECISION = 0.002f;
    private static final float ERROR_SQUARED = PRECISION * PRECISION;

    private float[] mX; // x coordinates in the line

    private float[] mY; // y coordinates in the line

    /**
     * Create an interpolator for an arbitrary <code>Path</code>. The <code>Path</code>
     * must begin at <code>(0, 0)</code> and end at <code>(1, 1)</code>.
     *
     * @param path The <code>Path</code> to use to make the line representing the interpolator.
     */
    /* // TODO I think I do not need it
    public PathInterpolator(Path path) {
        initPath(path);
    }
    */

    /**
     * Create an interpolator for a quadratic Bezier curve. The end points
     * <code>(0, 0)</code> and <code>(1, 1)</code> are assumed.
     *
     * @param controlX The x coordinate of the quadratic Bezier control point.
     * @param controlY The y coordinate of the quadratic Bezier control point.
     */
    public PathInterpolator(float controlX, float controlY) {
        initQuad(controlX, controlY);
    }

    /**
     * Create an interpolator for a cubic Bezier curve.  The end points
     * <code>(0, 0)</code> and <code>(1, 1)</code> are assumed.
     *
     * @param controlX1 The x coordinate of the first control point of the cubic Bezier.
     * @param controlY1 The y coordinate of the first control point of the cubic Bezier.
     * @param controlX2 The x coordinate of the second control point of the cubic Bezier.
     * @param controlY2 The y coordinate of the second control point of the cubic Bezier.
     */
    public PathInterpolator(float controlX1, float controlY1, float controlX2, float controlY2) {
        initCubic(controlX1, controlY1, controlX2, controlY2);
    }

    public PathInterpolator(Context context, AttributeSet attrs) {
        this(context.getResources(), context.getTheme(), attrs);
    }

    /** @hide */
    public PathInterpolator(Resources res, Theme theme, AttributeSet attrs) {
        TypedArray a;
        if (theme != null) {
            a = theme.obtainStyledAttributes(attrs, R.styleable.PathInterpolator, 0, 0);
        } else {
            a = res.obtainAttributes(attrs, R.styleable.PathInterpolator);
        }
        parseInterpolatorFromTypeArray(a);

        a.recycle();
    }

    private void parseInterpolatorFromTypeArray(TypedArray a) {
        // If there is pathData defined in the xml file, then the controls points
        // will be all coming from pathData.
        if (a.hasValue(R.styleable.PathInterpolator_pathData)) {
            /* TODO I think I do not need it
            String pathData = a.getString(R.styleable.PathInterpolator_pathData);
            Path path = PathParser.createPathFromPathData(pathData);
            if (path == null) {
                throw new InflateException("The path is null, which is created"
                        + " from " + pathData);
            }
            initPath(path);
            */
        } else {
            if (!a.hasValue(R.styleable.PathInterpolator_controlX1)) {
                throw new InflateException("pathInterpolator requires the controlX1 attribute");
            } else if (!a.hasValue(R.styleable.PathInterpolator_controlY1)) {
                throw new InflateException("pathInterpolator requires the controlY1 attribute");
            }
            float x1 = a.getFloat(R.styleable.PathInterpolator_controlX1, 0);
            float y1 = a.getFloat(R.styleable.PathInterpolator_controlY1, 0);

            boolean hasX2 = a.hasValue(R.styleable.PathInterpolator_controlX2);
            boolean hasY2 = a.hasValue(R.styleable.PathInterpolator_controlY2);

            if (hasX2 != hasY2) {
                throw new InflateException(
                        "pathInterpolator requires both controlX2 and controlY2 for cubic Beziers.");
            }

            if (!hasX2) {
                initQuad(x1, y1);
            } else {
                float x2 = a.getFloat(R.styleable.PathInterpolator_controlX2, 0);
                float y2 = a.getFloat(R.styleable.PathInterpolator_controlY2, 0);
                initCubic(x1, y1, x2, y2);
            }
        }
    }

    private void initQuad(float controlX, float controlY) {
        float[] points = bezierApproximate(new float[]{controlX, controlY}, ERROR_SQUARED, false);
        loadPointComponents(points);
    }

    private void initCubic(float x1, float y1, float x2, float y2) {
        float[] points = bezierApproximate(new float[]{x1, y1, x2, y2}, ERROR_SQUARED, true);
        loadPointComponents(points);
    }

    private void loadPointComponents(float[] pointComponents) {
        int numPoints = pointComponents.length / 3;
        if (pointComponents[1] != 0 || pointComponents[2] != 0
                || pointComponents[pointComponents.length - 2] != 1
                || pointComponents[pointComponents.length - 1] != 1) {
            throw new IllegalArgumentException("The Path must start at (0,0) and end at (1,1)");
        }

        mX = new float[numPoints];
        mY = new float[numPoints];
        float prevX = 0;
        float prevFraction = 0;
        int componentIndex = 0;
        for (int i = 0; i < numPoints; i++) {
            float fraction = pointComponents[componentIndex++];
            float x = pointComponents[componentIndex++];
            float y = pointComponents[componentIndex++];
            if (fraction == prevFraction && x != prevX) {
                throw new IllegalArgumentException(
                        "The Path cannot have discontinuity in the X axis.");
            }
            if (x < prevX) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
            mX[i] = x;
            mY[i] = y;
            prevX = x;
            prevFraction = fraction;
        }
    }

    /**
     * Using the line in the Path in this interpolator that can be described as
     * <code>y = f(x)</code>, finds the y coordinate of the line given <code>t</code>
     * as the x coordinate. Values less than 0 will always return 0 and values greater
     * than 1 will always return 1.
     *
     * @param t Treated as the x coordinate along the line.
     * @return The y coordinate of the Path along the line where x = <code>t</code>.
     * @see android.view.animation.Interpolator#getInterpolation(float)
     */
    @Override
    public float getInterpolation(float t) {
        if (t <= 0) {
            return 0;
        } else if (t >= 1) {
            return 1;
        }
        // Do a binary search for the correct x to interpolate between.
        int startIndex = 0;
        int endIndex = mX.length - 1;

        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }

        float xRange = mX[endIndex] - mX[startIndex];
        if (xRange == 0) {
            return mY[startIndex];
        }

        float tInRange = t - mX[startIndex];
        float fraction = tInRange / xRange;

        float startY = mY[startIndex];
        float endY = mY[endIndex];
        return startY + (fraction * (endY - startY));
    }

    // Divides Bezier curves until linear interpolation is very close to accurate, using
    // errorSquared as a metric. Cubic Bezier curves can have an inflection point that improperly
    // short-circuit subdivision. If you imagine an S shape, the top and bottom points being the
    // starting and end points, linear interpolation would mark the center where the curve places
    // the point. It is clearly not the case that we can linearly interpolate at that point.
    // doubleCheckDivision forces a second examination between subdivisions to ensure that linear
    // interpolation works.
    private static float[] bezierApproximate(float[] control, float errorSquared,
            boolean isCubic) {
        PointMap pointMap = new PointMap();

        pointMap.add(0f, 0f, 0f);
        pointMap.add(1f, 1f, 1f);

        PointMap.Iterator iter = pointMap.iterator();
        PointMap.Iterator next = iter.nextIterator();

        boolean doubleCheckDivision = isCubic;
        while (next != null) {
            boolean needsSubdivision;

            do {
                PointMap.Link iterLink = iter.link;
                PointMap.Link nextLink = next.link;

                float midT = (iterLink.t + nextLink.t) / 2;
                float midX = (iterLink.x + nextLink.x) / 2;
                float midY = (iterLink.y + nextLink.y) / 2;

                float midPointX = bezierX(midT, control, isCubic);
                float midPointY = bezierY(midT, control, isCubic);

                needsSubdivision = isOutOfErrorSquared(midX, midY, midPointX, midPointY, errorSquared);

                if (!needsSubdivision && doubleCheckDivision) {
                    float quarterT = (iterLink.t + midT) / 2;
                    float quarterX = (iterLink.x + midPointX) / 2;
                    float quarterY = (iterLink.y + midPointY) / 2;

                    float quarterPointX = bezierX(quarterT, control, isCubic);
                    float quarterPointY = bezierY(quarterT, control, isCubic);

                    needsSubdivision = isOutOfErrorSquared(quarterX, quarterY, quarterPointX, quarterPointY, errorSquared);
                    if (needsSubdivision) {
                        // Found an inflection point. No need to double-check.
                        doubleCheckDivision = false;
                    }
                }
                if (needsSubdivision) {
                    next.addBefore(midT, midPointX, midPointY);
                } else {
                    float length = dist(iterLink.x, iterLink.y, nextLink.x, nextLink.y);
                    iterLink.f = pointMap.totalLength;
                    pointMap.totalLength += length;
                }
            } while (needsSubdivision);

            iter = next;
            next = next.nextIterator();
        }

        iter.link.f = pointMap.totalLength;

        return pointMap.generate();
    }

    private static float dist(float x1, float y1, float x2, float y2) {
        final float x = (x2 - x1);
        final float y = (y2 - y1);
        return (float) Math.hypot(x, y);
    }

    private static boolean isOutOfErrorSquared(float midX, float midY,
            float midPointX, float midPointY, float errorSquared) {
        float xError = midPointX - midX;
        float yError = midPointY - midY;
        float midErrorSquared = (xError * xError) + (yError * yError);
        return midErrorSquared > errorSquared;
    }

    private static float bezierX(float t, float[] control, boolean isCubic) {
        if (isCubic) {
            return cubicCoordinateCalculation(t, 0f, control[0], control[2], 1f);
        } else {
            return quadraticCoordinateCalculation(t, 0f, control[0], 1f);
        }
    }

    private static float bezierY(float t, float[] control, boolean isCubic) {
        if (isCubic) {
            return cubicCoordinateCalculation(t, 0f, control[1], control[3], 1f);
        } else {
            return quadraticCoordinateCalculation(t, 0f, control[1], 1f);
        }
    }

    private static float quadraticCoordinateCalculation(float t, float p0, float p1, float p2) {
        float oneMinusT = 1 - t;
        return oneMinusT * ((oneMinusT * p0) + (t * p1)) + t * ((oneMinusT * p1) + (t * p2));
    }

    private static float cubicCoordinateCalculation(float t, float p0, float p1, float p2, float p3) {
        float oneMinusT = 1 - t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float oneMinusTCubed = oneMinusTSquared * oneMinusT;
        float tSquared = t * t;
        float tCubed = tSquared * t;
        return (oneMinusTCubed * p0) + (3 * oneMinusTSquared * t * p1)
                + (3 * oneMinusT * tSquared * p2) + (tCubed * p3);
    }

    private static final class PointMap {

        private int size = 0;

        private Link firstLink;
        private Link lastLink;

        float totalLength = 0f;

        private static final class Link {
            float t;
            float x;
            float y;
            float f = -1;

            private Link previous, next;

            Link(float t, float x, float y, Link p, Link n) {
                this.t = t;
                this.x = x;
                this.y = y;
                this.previous = p;
                this.next = n;
            }
        }

        public static final class Iterator {

            final PointMap parent;
            Link link;

            private Iterator(PointMap p, Link l) {
                parent = p;
                link = l;
            }

            boolean hasNext() {
                return link.next != null;
            }

            Iterator nextIterator() {
                if (hasNext()) {
                    return new Iterator(parent, link.next);
                } else {
                    return null;
                }
            }

            // Move iterator to the new data
            void addBefore(float t, float x, float y) {
                Link p = link.previous;
                Link l = new Link(t, x, y, p, link);
                link.previous = l;
                p.next = l;
                link = l;
                parent.size++;
            }
        }

        public void add(float t, float x, float y) {
            Link link = new Link(t, x, y, null, null);
            if (lastLink != null) {
                link.previous = lastLink;
                lastLink.next = link;
            }
            lastLink = link;
            if (firstLink == null) {
                firstLink = link;
            }
            size++;
        }

        // Get a iterator at first data or null
        public Iterator iterator() {
            if (firstLink != null) {
                return new Iterator(this, firstLink);
            } else {
                return null;
            }
        }

        public float[] generate() {
            int index = 0;
            float[] result = new float[size * 3];
            for (Link link = firstLink; link != null; link = link.next) {
                result[index++] = link.f / totalLength;
                result[index++] = link.x;
                result[index++] = link.y;
            }
            return result;
        }
    }
}
