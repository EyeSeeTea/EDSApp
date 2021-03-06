/*
 * Copyright (c) 2015.
 *
 * This file is part of Health Network QIS App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.test.utils;

import android.view.View;

import org.eyeseetea.malariacare.views.CustomTextView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by arrizabalaga on 25/05/15.
 */
public class TextCardScaleMatcher extends TypeSafeMatcher<View> {
    private final String scale;

    private TextCardScaleMatcher(String scale) {
        this.scale = checkNotNull(scale);
    }

    @Override
    public boolean matchesSafely(View view) {
        if (!(view instanceof CustomTextView)) {
            return false;
        }
        CustomTextView customTextView = (CustomTextView) view;
        return scale.equals(customTextView.getmScale());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("with scale: " + scale);
    }

    public static Matcher<? super View> hasTextCardScale(String scale) {
        return new TextCardScaleMatcher(scale);
    }
}