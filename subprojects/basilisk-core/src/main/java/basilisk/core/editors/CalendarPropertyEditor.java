/*
 * Copyright 2008-2015 the original author or authors.
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

import basilisk.core.formatters.CalendarFormatter;
import basilisk.core.formatters.Formatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import static basilisk.util.BasiliskNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public class CalendarPropertyEditor extends AbstractPropertyEditor {
    protected void setValueInternal(Object value) {
        if (null == value) {
            super.setValueInternal(null);
        } else if (value instanceof CharSequence) {
            handleAsString(String.valueOf(value));
        } else if (value instanceof Calendar) {
            super.setValueInternal(value);
        } else if (value instanceof Date) {
            Calendar c = Calendar.getInstance();
            c.setTime((Date) value);
            super.setValueInternal(c);
        } else if (value instanceof Number) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(((Number) value).longValue()));
            super.setValueInternal(c);
        } else if (value instanceof LocalDate) {
            handleAsLocalDate((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            handleAsLocalDateTime(((LocalDateTime) value));
        } else {
            throw illegalValue(value, Calendar.class);
        }
    }

    protected void handleAsString(String str) {
        if (isBlank(str)) {
            super.setValueInternal(null);
            return;
        }

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(new Date(Long.parseLong(str)));
            super.setValueInternal(c);
            return;
        } catch (NumberFormatException nfe) {
            // ignore, let's try parsing the date in a locale specific format
        }

        try {
            c.setTime(new SimpleDateFormat().parse(str));
            super.setValueInternal(c);
        } catch (ParseException e) {
            throw illegalValue(str, Calendar.class, e);
        }
    }

    protected Formatter<Calendar> resolveFormatter() {
        return isBlank(getFormat()) ? null : new CalendarFormatter(getFormat());
    }

    protected void handleAsLocalDate(LocalDate value) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(value.toEpochDay()));
        super.setValueInternal(c);
    }

    protected void handleAsLocalDateTime(LocalDateTime value) {
        LocalDate localDate = value.toLocalDate();
        LocalTime localTime = value.toLocalTime();

        Calendar c = Calendar.getInstance();
        c.set(
            localDate.getYear(),
            localDate.getMonthValue() - 1,
            localDate.getDayOfMonth(),
            localTime.getHour(),
            localTime.getMinute(),
            localTime.getSecond()
        );

        super.setValueInternal(c);
    }
}
