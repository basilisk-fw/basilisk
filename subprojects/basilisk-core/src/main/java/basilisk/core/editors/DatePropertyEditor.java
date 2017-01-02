/*
 * Copyright 2008-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package basilisk.core.editors;

import basilisk.core.formatters.DateFormatter;
import basilisk.core.formatters.Formatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import static basilisk.util.BasiliskNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public class DatePropertyEditor extends AbstractPropertyEditor {
    protected void setValueInternal(Object value) {
        if (null == value) {
            super.setValueInternal(null);
        } else if (value instanceof CharSequence) {
            handleAsString(String.valueOf(value));
        } else if (value instanceof Date) {
            super.setValueInternal(value);
        } else if (value instanceof Calendar) {
            super.setValueInternal(((Calendar) value).getTime());
        } else if (value instanceof Number) {
            super.setValueInternal(new Date(((Number) value).longValue()));
        } else if (value instanceof LocalDate) {
            handleAsLocalDate((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            handleAsLocalDate(((LocalDateTime) value).toLocalDate());
        } else {
            throw illegalValue(value, Date.class);
        }
    }

    protected void handleAsString(String str) {
        if (isBlank(str)) {
            super.setValueInternal(null);
            return;
        }

        try {
            super.setValueInternal(new Date(Long.parseLong(str)));
            return;
        } catch (NumberFormatException nfe) {
            // ignore, let's try parsing the date in a locale specific format
        }

        try {
            super.setValueInternal(new SimpleDateFormat().parse(str));
        } catch (ParseException e) {
            throw illegalValue(str, Date.class, e);
        }
    }

    protected Formatter<Date> resolveFormatter() {
        return isBlank(getFormat()) ? null : new DateFormatter(getFormat());
    }

    protected void handleAsLocalDate(LocalDate value) {
        super.setValueInternal(new Date(value.toEpochDay()));
    }
}
