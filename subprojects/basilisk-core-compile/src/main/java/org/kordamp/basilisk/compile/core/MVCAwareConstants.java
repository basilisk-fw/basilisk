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
package org.kordamp.basilisk.compile.core;

import static org.kordamp.basilisk.compile.core.MethodDescriptor.annotatedMethod;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.annotatedType;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.annotations;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.args;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.method;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.type;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.typeParam;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.typeParams;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.typeWithParams;
import static org.kordamp.basilisk.compile.core.MethodDescriptor.wildcard;

/**
 * @author Andres Almiray
 */
public interface MVCAwareConstants extends BaseConstants {
    String MVC_GROUP_MANAGER_PROPERTY = "mvcGroupManager";
    String MVC_FUNCTION_TYPE = "basilisk.core.mvc.MVCFunction";
    String MVC_GROUP_FUNCTION_TYPE = "basilisk.core.mvc.MVCGroupFunction";
    String BASILISK_MODEL_TYPE = "basilisk.core.artifact.BasiliskModel";
    String BASILISK_VIEW_TYPE = "basilisk.core.artifact.BasiliskView";
    String BASILISK_CONTROLLER_TYPE = "basilisk.core.artifact.BasiliskController";
    String MVC_GROUP = "basilisk.core.mvc.MVCGroup";
    String MVC_HANDLER_TYPE = "basilisk.core.mvc.MVCHandler";
    String MVC_GROUP_MANAGER_TYPE = "basilisk.core.mvc.MVCGroupManager";
    String BASILISK_MVC_ARTIFACT_TYPE = "basilisk.core.artifact.BasiliskMvcArtifact";

    String METHOD_CREATE_MVC = "createMVC";
    String METHOD_CREATE_MVC_GROUP = "createMVCGroup";
    String METHOD_WITH_MVC_GROUP = "withMVCGroup";
    String METHOD_WITH_MVC = "withMVC";
    String METHOD_DESTROY_MVC_GROUP = "destroyMVCGroup";

    String M = "M";
    String V = "V";
    String C = "C";

    MethodDescriptor[] METHODS = new MethodDescriptor[]{
        method(
            type(VOID),
            METHOD_DESTROY_MVC_GROUP,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(MVC_GROUP),
            METHOD_CREATE_MVC_GROUP,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(MVC_GROUP),
            METHOD_CREATE_MVC_GROUP,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING), type(JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(MVC_GROUP),
            METHOD_CREATE_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(MVC_GROUP),
            METHOD_CREATE_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(MVC_GROUP),
            METHOD_CREATE_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            type(MVC_GROUP),
            METHOD_CREATE_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT))
        ),

        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            typeWithParams(JAVA_UTIL_LIST, wildcard(BASILISK_MVC_ARTIFACT_TYPE)),
            METHOD_CREATE_MVC,
            args(annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            typeWithParams(JAVA_UTIL_LIST, wildcard(BASILISK_MVC_ARTIFACT_TYPE)),
            METHOD_CREATE_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            typeWithParams(JAVA_UTIL_LIST, wildcard(BASILISK_MVC_ARTIFACT_TYPE)),
            METHOD_CREATE_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            typeWithParams(JAVA_UTIL_LIST, wildcard(BASILISK_MVC_ARTIFACT_TYPE)),
            METHOD_CREATE_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            typeWithParams(JAVA_UTIL_LIST, wildcard(BASILISK_MVC_ARTIFACT_TYPE)),
            METHOD_CREATE_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING))
        ),
        annotatedMethod(
            annotations(JAVAX_ANNOTATION_NONNULL),
            typeWithParams(JAVA_UTIL_LIST, wildcard(BASILISK_MVC_ARTIFACT_TYPE)),
            METHOD_CREATE_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT))
        ),

        method(
            type(VOID),
            typeParams(
                typeParam(M, BASILISK_MODEL_TYPE),
                typeParam(V, BASILISK_VIEW_TYPE),
                typeParam(C, BASILISK_CONTROLLER_TYPE)
            ),
            METHOD_WITH_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_FUNCTION_TYPE, M, V, C))
        ),
        method(
            type(VOID),
            typeParams(
                typeParam(M, BASILISK_MODEL_TYPE),
                typeParam(V, BASILISK_VIEW_TYPE),
                typeParam(C, BASILISK_CONTROLLER_TYPE)
            ),
            METHOD_WITH_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_FUNCTION_TYPE, M, V, C))
        ),
        method(
            type(VOID),
            typeParams(
                typeParam(M, BASILISK_MODEL_TYPE),
                typeParam(V, BASILISK_VIEW_TYPE),
                typeParam(C, BASILISK_CONTROLLER_TYPE)
            ),
            METHOD_WITH_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_FUNCTION_TYPE, M, V, C))
        ),
        method(
            type(VOID),
            typeParams(
                typeParam(M, BASILISK_MODEL_TYPE),
                typeParam(V, BASILISK_VIEW_TYPE),
                typeParam(C, BASILISK_CONTROLLER_TYPE)
            ),
            METHOD_WITH_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_FUNCTION_TYPE, M, V, C))
        ),
        method(
            type(VOID),
            typeParams(
                typeParam(M, BASILISK_MODEL_TYPE),
                typeParam(V, BASILISK_VIEW_TYPE),
                typeParam(C, BASILISK_CONTROLLER_TYPE)
            ),
            METHOD_WITH_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_FUNCTION_TYPE, M, V, C))
        ),
        method(
            type(VOID),
            typeParams(
                typeParam(M, BASILISK_MODEL_TYPE),
                typeParam(V, BASILISK_VIEW_TYPE),
                typeParam(C, BASILISK_CONTROLLER_TYPE)
            ),
            METHOD_WITH_MVC,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_FUNCTION_TYPE, M, V, C))
        ),

        method(
            type(VOID),
            METHOD_WITH_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_GROUP_FUNCTION_TYPE))
        ),
        method(
            type(VOID),
            METHOD_WITH_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_GROUP_FUNCTION_TYPE))
        ),
        method(
            type(VOID),
            METHOD_WITH_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_GROUP_FUNCTION_TYPE))
        ),
        method(
            type(VOID),
            METHOD_WITH_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_GROUP_FUNCTION_TYPE))
        ),
        method(
            type(VOID),
            METHOD_WITH_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_GROUP_FUNCTION_TYPE))
        ),
        method(
            type(VOID),
            METHOD_WITH_MVC_GROUP,
            args(
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_UTIL_MAP, JAVA_LANG_STRING, JAVA_LANG_OBJECT),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), JAVA_LANG_STRING),
                annotatedType(annotations(JAVAX_ANNOTATION_NONNULL), MVC_GROUP_FUNCTION_TYPE))
        )
    };
}
