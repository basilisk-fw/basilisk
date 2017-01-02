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
package basilisk.transform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Annotates a class.</p>
 * <p/>
 * <p>When annotating a class it indicates that it will be able to
 * handle MVC groups, that is, instantiate them. The class will have
 * the ability to create new instances of MVC groups, including short-lived
 * ones.</p>
 * <p>
 * The following methods will be added to classes annotated with &#064;MVCAware
 * <ul>
 * <p>
 * <li><code>public MVCGroup createMVCGroup(String mvcType)</code></li>
 * <li><code>public MVCGroup createMVCGroup(String mvcType, String mvcId)</code></li>
 * <li><code>public MVCGroup createMVCGroup(Map&lt;String, Object&gt; args, String mvcType)</code></li>
 * <li><code>public MVCGroup createMVCGroup(String mvcType, Map&lt;String, Object&gt; args)</code></li>
 * <li><code>public MVCGroup createMVCGroup(Map&lt;String, Object&gt; args, String mvcType, String mvcId)</code></li>
 * <li><code>public MVCGroup createMVCGroup(String mvcType, String mvcId, Map&lt;String, Object&gt; args)</code></li>
 * <li><code>public List&lt;? extends BasiliskMvcArtifact&gt; createMVC(String mvcType)</code></li>
 * <li><code>public List&lt;? extends BasiliskMvcArtifact&gt; createMVC(Map&lt;String, Object&gt; args, String mvcType)</code></li>
 * <li><code>public List&lt;? extends BasiliskMvcArtifact&gt; createMVC(String mvcType, Map&lt;String, Object&gt; args)</code></li>
 * <li><code>public List&lt;? extends BasiliskMvcArtifact&gt; createMVC(String mvcType, String mvcId)</code></li>
 * <li><code>public List&lt;? extends BasiliskMvcArtifact&gt; createMVC(Map&lt;String, Object&gt; args, String mvcType, String mvcId)</code></li>
 * <li><code>public List&lt;? extends BasiliskMvcArtifact&gt; createMVC(String mvcType, String mvcId, Map&lt;String, Object&gt; args)</code></li>
 * <li><code>public void destroyMVCGroup(String mvcId)</code></li>
 * <li><code>public &lt;M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController&gt; void withMVC(String mvcType, MVCCallable&lt;M, V, C&gt; handler)</code></li>
 * <li><code>public &lt;M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController&gt; void withMVC(String mvcType, String mvcId, MVCCallable&lt;M, V, C&gt; handler)</code></li>
 * <li><code>public &lt;M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController&gt; void withMVC(String mvcType, String mvcId, Map&lt;String, Object&gt; args, MVCCallable&lt;M, V, C&gt; handler)</code></li>
 * <li><code>public &lt;M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController&gt; void withMVC(Map&lt;String, Object&gt; args, String mvcType, String mvcId, MVCCallable&lt;M, V, C&gt; handler)</code></li>
 * <li><code>public &lt;M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController&gt; void withMVC(String mvcType, Map&lt;String, Object&gt; args, MVCCallable&lt;M, V, C&gt; handler)</code></li>
 * <li><code>public &lt;M extends BasiliskModel, V extends BasiliskView, C extends BasiliskController&gt; void withMVC(Map&lt;String, Object&gt; args, String mvcType, MVCCallable&lt;M, V, C&gt; handler)</code></li>
 * <li><code>public void withMVCGroup(String mvcType, MVCGroupCallable handler)</code></li>
 * <li><code>public void withMVCGroup(String mvcType, String mvcId, MVCGroupCallable handler)</code></li>
 * <li><code>public void withMVCGroup(String mvcType, String mvcId, Map&lt;String, Object&gt; args, MVCGroupCallable handler)</code></li>
 * <li><code>public void withMVCGroup(Map&lt;String, Object&gt; args, String mvcType, String mvcId, MVCGroupCallable handler)</code></li>
 * <li><code>public void withMVCGroup(String mvcType, Map&lt;String, Object&gt; args, MVCGroupCallable handler)</code></li>
 * <li><code>public void withMVCGroup(Map&lt;String, Object&gt; args, String mvcType, MVCGroupCallable handler)</code></li>
 * </ul>
 *
 * @author Andres Almiray
 * @see basilisk.core.mvc.MVCHandler
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface MVCAware {
}
