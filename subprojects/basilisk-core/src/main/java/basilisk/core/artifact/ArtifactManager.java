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
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Helper class capable of dealing with artifacts and their handlers.
 *
 * @author Andres Almiray
 */
@SuppressWarnings("rawtypes")
public interface ArtifactManager {
    List<BasiliskClass> EMPTY_BASILISK_CLASS_LIST = Collections.emptyList();

    /**
     * Registers an ArtifactHandler by type.<p>
     * Should call initialize() on the handler.
     *
     * @param handler an ArtifactHandler
     */
    void registerArtifactHandler(@Nonnull ArtifactHandler handler);

    /**
     * Removes an ArtifactHandler by type.
     *
     * @param handler an ArtifactHandler
     */
    void unregisterArtifactHandler(@Nonnull ArtifactHandler handler);

    /**
     * Reads the artifacts definitions file from the classpath.<p>
     * Should call initialize() on artifact handlers if there are any
     * registered already.
     */
    void loadArtifactMetadata();

    /**
     * Finds an artifact by name and type.<p>
     * Example: findBasiliskClass("Book", "controller") will return an
     * artifact class that describes BookController.
     *
     * @param name the name of the artifact, e.g. 'Book'
     * @param type the type of the artifact, e.g. 'controller'
     * @return the BasiliskClass associated with the artifact is there's a match, null otherwise.
     */
    @Nullable
    BasiliskClass findBasiliskClass(@Nonnull String name, @Nonnull String type);

    /**
     * Finds an artifact by class and type.<p>
     * Example: findBasiliskClass(BookController, "controller") will return an
     * artifact class that describes BookController.
     *
     * @param clazz the name of the artifact, e.g. com.acme.BookController
     * @param type  the type of the artifact, e.g. 'controller'
     * @return the BasiliskClass associated with the artifact is there's a match, null otherwise.
     */
    @Nullable
    BasiliskClass findBasiliskClass(@Nonnull Class<? extends BasiliskArtifact> clazz, @Nonnull String type);

    /**
     * Finds an artifact by class.<p>
     * Example: findBasiliskClass(aBookControllerInstance) will return an
     * artifact class that describes BookController.
     *
     * @param artifact an artifact instance
     * @return the BasiliskClass associated with the artifact is there's a match, null otherwise.
     */
    @Nullable
    <A extends BasiliskArtifact> BasiliskClass findBasiliskClass(@Nonnull A artifact);

    /**
     * Finds an artifact by class.<p>
     * Example: findBasiliskClass(BookController) will return an
     * artifact class that describes BookController.
     *
     * @param clazz a Class instance
     * @return the BasiliskClass associated with the artifact is there's a match, null otherwise.
     */
    @Nullable
    BasiliskClass findBasiliskClass(@Nonnull Class<? extends BasiliskArtifact> clazz);

    /**
     * Finds an artifact by name.<p>
     * Example: findBasiliskClass("BookController") will return an
     * artifact class that describes BookController.
     *
     * @param fqClassName full qualified class name
     * @return the BasiliskClass associated with the artifact is there's a match, null otherwise.
     */
    @Nullable
    BasiliskClass findBasiliskClass(@Nonnull String fqClassName);

    /**
     * Finds all artifacts of an specific type.<p>
     * Example: getClassesOfType("controller") will return all
     * artifact classes that describe controllers.
     *
     * @param type an artifact type, e.g. 'controller'
     * @return a List of matching artifacts or an empty List if no match. Never returns null.
     */
    @Nonnull
    List<BasiliskClass> getClassesOfType(@Nonnull String type);

    /**
     * Finds all supported artifact types.<p>
     *
     * @return a Set of all available artifact types. Never returns null.
     */
    @Nonnull
    Set<String> getAllTypes();

    /**
     * Finds all artifact classes.<p>
     *
     * @return a List of all available BasiliskClass instances. Never returns null.
     */
    @Nonnull
    List<BasiliskClass> getAllClasses();

    /**
     * Creates a new instance of the specified class and type.<br/>
     * Triggers the ApplicationEvent.NEW_INSTANCE with the following parameters
     * <ul>
     * <li>clazz - the Class of the object</li>
     * <li>instance -> the object that was created</li>
     * </ul>
     *
     * @param basiliskClass the BasiliskClass for which an instance must be created
     * @return a newly instantiated object of type <tt>clazz</tt>. Implementations must be sure
     * to trigger an event of type ApplicationEvent.NEW_INSTANCE.
     * @throws basilisk.exceptions.ArtifactNotFoundException if there's no artifact configured
     *                                                       matching the given criteria
     */
    @Nonnull
    <A extends BasiliskArtifact> A newInstance(@Nonnull BasiliskClass basiliskClass);

    /**
     * Creates a new instance of the specified class and type.<br/>
     * Triggers the ApplicationEvent.NEW_INSTANCE with the following parameters
     * <ul>
     * <li>clazz - the Class of the object</li>
     * <li>instance -> the object that was created</li>
     * </ul>
     *
     * @param clazz the Class for which an instance must be created
     * @return a newly instantiated object of type <tt>clazz</tt>. Implementations must be sure
     * to trigger an event of type ApplicationEvent.NEW_INSTANCE.
     * @throws basilisk.exceptions.ArtifactNotFoundException if there's no artifact configured
     *                                                       matching the given criteria
     */
    @Nonnull
    <A extends BasiliskArtifact> A newInstance(@Nonnull Class<A> clazz);
}
