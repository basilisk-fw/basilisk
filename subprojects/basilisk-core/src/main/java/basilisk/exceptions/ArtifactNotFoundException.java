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
package basilisk.exceptions;

import basilisk.core.artifact.BasiliskArtifact;
import basilisk.core.artifact.BasiliskClass;

import javax.annotation.Nonnull;

/**
 * @author Andres Almiray
 */
public class ArtifactNotFoundException extends BasiliskException {
    private static final long serialVersionUID = -7881105306242340254L;

    public ArtifactNotFoundException(@Nonnull Throwable cause) {
        super("Could not find artifact", checkNonNull(cause, "cause"));
    }

    public ArtifactNotFoundException(@Nonnull Class<?> clazz) {
        super(format(clazz));
    }

    public ArtifactNotFoundException(@Nonnull BasiliskClass basiliskClass, @Nonnull Throwable cause) {
        super(format(basiliskClass), checkNonNull(cause, "cause"));
    }

    public ArtifactNotFoundException(@Nonnull Class<? extends BasiliskArtifact> clazz, @Nonnull String name) {
        super(format(clazz, name));
    }

    private static String format(BasiliskClass basiliskClass) {
        return "Could not find artifact for " + checkNonNull(basiliskClass, "basiliskClass").getPropertyName();
    }

    private static String format(Class<?> clazz) {
        return "Could not find artifact for " + checkNonNull(clazz, "clazz").getName();
    }

    private static String format(Class<? extends BasiliskArtifact> clazz, String name) {
        return "Could not find artifact of type " + checkNonNull(clazz, "clazz").getName() + " named " + checkNonBlank(name, "name");
    }
}
