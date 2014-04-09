/*
 * Copyright (C) 2014 The Light Open Source Project
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

package us.shandian.launcher;

import android.animation.TimeInterpolator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;

import static android.view.View.VISIBLE;
import static android.view.View.INVISIBLE;

public abstract class TransitionEffect {
    
    static class ZInterpolator implements TimeInterpolator {
        private float focalLength;

        public ZInterpolator(float foc) {
            focalLength = foc;
        }

        public float getInterpolation(float input) {
            return (1.0f - focalLength / (focalLength + input)) /
                (1.0f - focalLength / (focalLength + 1.0f));
        }
    }

    /*
     * The exact reverse of ZInterpolator.
     */
    static class InverseZInterpolator implements TimeInterpolator {
        private ZInterpolator zInterpolator;
        public InverseZInterpolator(float foc) {
            zInterpolator = new ZInterpolator(foc);
        }
        public float getInterpolation(float input) {
            return 1 - zInterpolator.getInterpolation(1 - input);
        }
    }
    
    public static final String TRANSITION_EFFECT_NONE = "none";
    public static final String TRANSITION_EFFECT_ZOOM_IN = "zoom-in";
    public static final String TRANSITION_EFFECT_ZOOM_OUT = "zoom-out";
    public static final String TRANSITION_EFFECT_ROTATE_UP = "rotate-up";
    public static final String TRANSITION_EFFECT_ROTATE_DOWN = "rotate-down";
    public static final String TRANSITION_EFFECT_CUBE_IN = "cube-in";
    public static final String TRANSITION_EFFECT_CUBE_OUT = "cube-out";
    public static final String TRANSITION_EFFECT_STACK = "stack";
    public static final String TRANSITION_EFFECT_ACCORDION = "accordion";
    public static final String TRANSITION_EFFECT_FLIP = "flip";
    public static final String TRANSITION_EFFECT_CYLINDER_IN = "cylinder-in";
    public static final String TRANSITION_EFFECT_CYLINDER_OUT = "cylinder-out";
    public static final String TRANSITION_EFFECT_CAROUSEL = "carousel";
    public static final String TRANSITION_EFFECT_OVERVIEW = "overview";
    
    public static float CAMERA_DISTANCE = 6500;
    public static final float TRANSITION_SCALE_FACTOR = 0.74f;
    public static final float TRANSITION_SCREEN_ROTATION = 12.5f;

    protected final PagedView mPagedView;
    private final String mName;

    public TransitionEffect(PagedView pagedView, String name) {
        mPagedView = pagedView;
        mName = name;
    }

    public abstract void screenScrolled(View v, int i, float scrollProgress);

    public final String getName() {
        return mName;
    }

    public static void setFromString(PagedView pagedView, String effect) {
        if (effect.equals(TransitionEffect.TRANSITION_EFFECT_NONE)) {
            pagedView.setTransitionEffect(null);
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_ZOOM_IN)) {
            pagedView.setTransitionEffect(new TransitionEffect.Zoom(pagedView, true));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_ZOOM_OUT)) {
            pagedView.setTransitionEffect(new TransitionEffect.Zoom(pagedView, false));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_CUBE_IN)) {
            pagedView.setTransitionEffect(new TransitionEffect.Cube(pagedView, true));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_CUBE_OUT)) {
            pagedView.setTransitionEffect(new TransitionEffect.Cube(pagedView, false));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_ROTATE_UP)) {
            pagedView.setTransitionEffect(new TransitionEffect.Rotate(pagedView, true));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_ROTATE_DOWN)) {
            pagedView.setTransitionEffect(new TransitionEffect.Rotate(pagedView, false));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_STACK)) {
            pagedView.setTransitionEffect(new TransitionEffect.Stack(pagedView));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_ACCORDION)) {
            pagedView.setTransitionEffect(new TransitionEffect.Accordion(pagedView));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_FLIP)) {
            pagedView.setTransitionEffect(new TransitionEffect.Flip(pagedView));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_CYLINDER_IN)) {
            pagedView.setTransitionEffect(new TransitionEffect.Cylinder(pagedView, true));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_CYLINDER_OUT)) {
            pagedView.setTransitionEffect(new TransitionEffect.Cylinder(pagedView, false));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_CAROUSEL)) {
            pagedView.setTransitionEffect(new TransitionEffect.Carousel(pagedView));
        } else if (effect.equals(TransitionEffect.TRANSITION_EFFECT_OVERVIEW)) {
            pagedView.setTransitionEffect(new TransitionEffect.Overview(pagedView));
        }
    }

    public static class Zoom extends TransitionEffect {
        private boolean mIn;

        public Zoom(PagedView pagedView, boolean in) {
            super(pagedView, in ? TRANSITION_EFFECT_ZOOM_IN : TRANSITION_EFFECT_ZOOM_OUT);
            mIn = in;
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float scale = 1.0f + (mIn ? -0.2f : 0.1f) * Math.abs(scrollProgress);

            // Extra translation to account for the increase in size
            if (!mIn) {
                float translationX = v.getMeasuredWidth() * 0.1f * -scrollProgress;
                v.setTranslationX(translationX);
            }

            v.setScaleX(scale);
            v.setScaleY(scale);
        }
    }

    public static class Rotate extends TransitionEffect {
        private boolean mUp;

        public Rotate(PagedView pagedView, boolean up) {
            super(pagedView, up ? TRANSITION_EFFECT_ROTATE_UP : TRANSITION_EFFECT_ROTATE_DOWN);
            mUp = up;
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float rotation =
                (mUp ? TRANSITION_SCREEN_ROTATION : -TRANSITION_SCREEN_ROTATION) * scrollProgress;

            float translationX = v.getMeasuredWidth() * scrollProgress;

            float rotatePoint =
                (v.getMeasuredWidth() * 0.5f) /
                (float) Math.tan(Math.toRadians((double) (TRANSITION_SCREEN_ROTATION * 0.5f)));

            v.setPivotX(v.getMeasuredWidth() * 0.5f);
            if (mUp) {
                v.setPivotY(-rotatePoint);
            } else {
                v.setPivotY(v.getMeasuredHeight() + rotatePoint);
            }
            v.setRotation(rotation);
            v.setTranslationX(translationX);
        }
    }

    public static class Cube extends TransitionEffect {
        private boolean mIn;

        public Cube(PagedView pagedView, boolean in) {
            super(pagedView, in ? TRANSITION_EFFECT_CUBE_IN : TRANSITION_EFFECT_CUBE_OUT);
            mIn = in;
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float rotation = (mIn ? 90.0f : -90.0f) * scrollProgress;

            if (mIn) {
                v.setCameraDistance(mPagedView.mDensity * CAMERA_DISTANCE);
            }

            v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
            v.setPivotY(v.getMeasuredHeight() * 0.5f);
            v.setRotationY(rotation);
        }
    }

    public static class Stack extends TransitionEffect {
        private ZInterpolator mZInterpolator = new ZInterpolator(0.5f);
        private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4);
        protected AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);

        public Stack(PagedView pagedView) {
            super(pagedView, TRANSITION_EFFECT_STACK);
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            final boolean isRtl = mPagedView.isLayoutRtl();
            float interpolatedProgress;
            float translationX;
            float maxScrollProgress = Math.max(0, scrollProgress);
            float minScrollProgress = Math.min(0, scrollProgress);

            if (mPagedView.isLayoutRtl()) {
                translationX = maxScrollProgress * v.getMeasuredWidth();
                interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(maxScrollProgress));
            } else {
                translationX = minScrollProgress * v.getMeasuredWidth();
                interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(minScrollProgress));
            }
            float scale = (1 - interpolatedProgress) +
                interpolatedProgress * TRANSITION_SCALE_FACTOR;

            float alpha;
            if (isRtl && (scrollProgress > 0)) {
                alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(maxScrollProgress));
            } else if (!isRtl && (scrollProgress < 0)) {
                alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(scrollProgress));
            } else {
                //  On large screens we need to fade the page as it nears its leftmost position
                alpha = mLeftScreenAlphaInterpolator.getInterpolation(1 - scrollProgress);
            }

            v.setTranslationX(translationX);
            v.setScaleX(scale);
            v.setScaleY(scale);
            if (v instanceof CellLayout) {
                ((CellLayout) v).getShortcutsAndWidgets().setAlpha(alpha);
            } else {
                v.setAlpha(alpha);
            }

            // If the view has 0 alpha, we set it to be invisible so as to prevent
            // it from accepting touches
            if (alpha == 0) {
                v.setVisibility(INVISIBLE);
            } else if (v.getVisibility() != VISIBLE) {
                v.setVisibility(VISIBLE);
            }
        }
    }

    public static class Accordion extends TransitionEffect {
        public Accordion(PagedView pagedView) {
            super(pagedView, TRANSITION_EFFECT_ACCORDION);
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float scale = 1.0f - Math.abs(scrollProgress);

            v.setScaleX(scale);
            v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
            v.setPivotY(v.getMeasuredHeight() / 2f);
        }
    }

    public static class Flip extends TransitionEffect {
        public Flip(PagedView pagedView) {
            super(pagedView, TRANSITION_EFFECT_FLIP);
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float rotation = -180.0f * Math.max(-1f, Math.min(1f, scrollProgress));

            v.setCameraDistance(mPagedView.mDensity * CAMERA_DISTANCE);
            v.setPivotX(v.getMeasuredWidth() * 0.5f);
            v.setPivotY(v.getMeasuredHeight() * 0.5f);
            v.setRotationY(rotation);

            if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
                v.setTranslationX(v.getMeasuredWidth() * scrollProgress);
                if (v.getVisibility() != VISIBLE) {
                    v.setVisibility(VISIBLE);
                }
            } else {
                v.setTranslationX(0f);
                v.setVisibility(INVISIBLE);
            }
        }
    }

    public static class Cylinder extends TransitionEffect {
        private boolean mIn;

        public Cylinder(PagedView pagedView, boolean in) {
            super(pagedView, in ? TRANSITION_EFFECT_CYLINDER_IN : TRANSITION_EFFECT_CYLINDER_OUT);
            mIn = in;
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float rotation = (mIn ? TRANSITION_SCREEN_ROTATION : -TRANSITION_SCREEN_ROTATION) * scrollProgress;

            v.setPivotX((scrollProgress + 1) * v.getMeasuredWidth() * 0.5f);
            v.setPivotY(v.getMeasuredHeight() * 0.5f);
            v.setRotationY(rotation);
        }
    }

    public static class Carousel extends TransitionEffect {
        public Carousel(PagedView pagedView) {
            super(pagedView, TRANSITION_EFFECT_CAROUSEL);
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float rotation = 90.0f * scrollProgress;

            v.setCameraDistance(mPagedView.mDensity * CAMERA_DISTANCE);
            v.setTranslationX(v.getMeasuredWidth() * scrollProgress);
            v.setPivotX(!mPagedView.isLayoutRtl() ? 0f : v.getMeasuredWidth());
            v.setPivotY(v.getMeasuredHeight() / 2);
            v.setRotationY(-rotation);
        }
    }

    public static class Overview extends TransitionEffect {
        private AccelerateDecelerateInterpolator mScaleInterpolator = new AccelerateDecelerateInterpolator();

        public Overview(PagedView pagedView) {
            super(pagedView, TRANSITION_EFFECT_OVERVIEW);
        }

        @Override
        public void screenScrolled(View v, int i, float scrollProgress) {
            float scale = 1.0f - 0.1f *
                mScaleInterpolator.getInterpolation(Math.min(0.3f, Math.abs(scrollProgress)) / 0.3f);

            v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
            v.setPivotY(v.getMeasuredHeight() * 0.5f);
            v.setScaleX(scale);
            v.setScaleY(scale);
            mPagedView.setChildAlpha(v, scale);
        }
    }
}
