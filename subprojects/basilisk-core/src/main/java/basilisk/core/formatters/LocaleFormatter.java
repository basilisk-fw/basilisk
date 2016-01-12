/*
 * Copyright 2008-2016 the original author or authors.
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
package basilisk.core.formatters;

import javax.annotation.Nullable;
import java.util.Locale;

import static basilisk.util.BasiliskApplicationUtils.parseLocale;
import static basilisk.util.BasiliskNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public class LocaleFormatter extends AbstractFormatter<Locale> {
    @Nullable
    public String format(@Nullable Locale locale) {
        if (locale == null) return null;
        StringBuilder b = new StringBuilder();
        b.append(locale.getLanguage());
        if (!isBlank(locale.getCountry())) {
            b.append("_").append(locale.getCountry());
            if (!isBlank(locale.getVariant())) {
                b.append("_").append(locale.getVariant());
            }
        }

        return b.toString();
    }

    @Nullable
    @Override
    public Locale parse(@Nullable String str) throws ParseException {
        return parseLocale(str);
    }
}
