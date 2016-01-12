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
package basilisk.exceptions;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class BasiliskException extends RuntimeException {
    private static final long serialVersionUID = 2745800851610086563L;

    public BasiliskException() {
        super();
    }

    public BasiliskException(String arg0) {
        super(arg0);
    }

    public BasiliskException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public BasiliskException(Throwable arg0) {
        super(arg0);
    }

    protected static <T> T checkNonNull(T arg, String argName) {
        return requireNonNull(arg, "Argument '" + argName + "' must not be null");
    }

    protected static String checkNonBlank(String arg, String argName) {
        return requireNonBlank(arg, "Argument '" + argName + "' must not be blank");
    }
}
