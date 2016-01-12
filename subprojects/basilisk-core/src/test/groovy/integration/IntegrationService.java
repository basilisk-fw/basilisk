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
package integration;

import basilisk.core.i18n.MessageSource;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskService;

import static basilisk.util.BasiliskNameUtils.isBlank;
import static java.util.Arrays.asList;

public class IntegrationService extends AbstractBasiliskService {
    public String sayHello(String input) {
        MessageSource messageSource = getApplication().getMessageSource();
        if (isBlank(input)) {
            return messageSource.getMessage("greeting.default");
        } else {
            return messageSource.getMessage("greeting.parameterized", asList(input));
        }
    }
}