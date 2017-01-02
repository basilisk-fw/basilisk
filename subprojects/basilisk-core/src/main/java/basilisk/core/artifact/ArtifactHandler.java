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
package basilisk.core.artifact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * The ArtifactHandler interface's purpose is to allow the analysis of conventions within a Basilisk application.<p>
 * An artifact is represented by the BasiliskClass interface and this interface provides methods that allow artifacts to
 * be identified, created and initialized.
 *
 * @author Andres Almiray
 */
public interface ArtifactHandler<A extends BasiliskArtifact> {
    @Nonnull
    Class<A> getArtifactType();

    /**
     * Get the type of artifact this handler processes.
     *
     * @return the type of artifacts this handler can handle, e.g. 'service'
     */
    @Nonnull
    String getType();

    /**
     * Get the trailing suffix that identifies the artifact.<p>
     * May be empty but non-null.
     *
     * @return the trailing name suffix (if any), e.g. 'Service'
     */
    @Nonnull
    String getTrailing();

    /**
     * Returns true if the target Class is a class artifact
     * handled by this object.
     *
     * @param clazz a Class instance
     * @return true if this handler is capable of handling the artifact class, false otherwise.
     */
    boolean isArtifact(@Nonnull Class<A> clazz);

    /**
     * Returns true if the target BasiliskClass is a class artifact
     * handled by this object.
     *
     * @param clazz a BasiliskClass instance
     * @return true if this handler is capable of handling the clazz parameter, false otherwise.
     */
    boolean isArtifact(@Nonnull BasiliskClass clazz);

    /**
     * Initializes the handler with a collection of all available
     * classes this handler can process.<p>
     * This is a good time to pre-emptively instantiate beans or
     * perform additional checks on artifacts.
     *
     * @param classes an array of all classes this handler should manage
     */
    void initialize(@Nonnull Class<A>[] classes);

    /**
     * Returns the set of all artifact classes this handler manages.
     *
     * @return an array of all BasiliskClasses managed by this handler. Never returns null.
     */
    @Nonnull
    BasiliskClass[] getClasses();

    /**
     * Finds an artifact by its property name.<p>
     * Examples: findClassfor("fooService") returns an artifact class
     * that can handle FooService.<p>
     * <p>
     * Should {@code propertyName} contain any dots then the portion
     * after the last dot will be considered only.
     *
     * @param propertyName the property representation of an artifact, e.g. 'fooService'
     * @return a BasiliskClass instance if there's a match, null otherwise.
     */
    @Nullable
    BasiliskClass findClassFor(@Nonnull String propertyName);

    /**
     * Finds an artifact if the target {@code clazz} is handled by this
     * ArtifactHandler.<p>
     *
     * @param clazz a class object, i.e, BookController
     * @return a BasiliskClass that can handle the target class or null
     * if the clazz is not handled by this ArtifactHandler.
     */
    @Nullable
    BasiliskClass getClassFor(@Nonnull Class<A> clazz);

    /**
     * Finds an artifact by class name if it represents a class that
     * is handled by this ArtifactHandler.<p>
     *
     * @param fqnClassName a full qualified class name, i.e, "book.BookController"
     * @return a BasiliskClass that can handle the target class or null
     * if the clazz is not handled by this ArtifactHandler.
     */
    @Nullable
    BasiliskClass getClassFor(@Nonnull String fqnClassName);

    @Nonnull
    BasiliskClass newBasiliskClassInstance(@Nonnull Class<A> clazz);

    @Nonnull
    Map<String, BasiliskClass> getClassesByName();
}
