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
package basilisk.util

import basilisk.core.addon.BasiliskAddon
import basilisk.core.artifact.BasiliskArtifact
import basilisk.core.artifact.BasiliskMvcArtifact
import basilisk.core.event.EventPublisher
import basilisk.core.i18n.MessageSource
import basilisk.core.mvc.MVCHandler
import basilisk.core.resources.ResourceHandler
import basilisk.core.resources.ResourceResolver
import basilisk.core.threading.ThreadingHandler
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method

@Unroll
class BasiliskClassUtilsSpec extends Specification {
    void "isContributionMethod() returns #result for '#method' (name)"() {
        expect:
        assert result == BasiliskClassUtils.isContributionMethod(method)

        where:
        [result, method] << [[true, 'withSomething'], [false, 'something']]
    }

    void "isEventHandler() returns #result for '#method' (name)"() {
        expect:
        assert result == BasiliskClassUtils.isEventHandler(method)

        where:
        [result, method] << [[true, 'onSomething'], [false, 'something'], [false, 'onShutdown']]
    }

    void "isSetterMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isSetterMethod(method, true)

        where:
        [result, method] << methodsOf(MySetter, true).plus(methodsOf(MyGetter, false))
    }

    void "isGetterMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isGetterMethod(method, true)

        where:
        [result, method] << methodsOf(MyGetter, true).plus(methodsOf(MySetter, false))
    }

    void "isContributionMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isContributionMethod(method, true)

        where:
        [result, method] << methodsOf(MyContributor, true).plus(methodsOf(MyEventHandler, false))
    }

    void "isEventHandler() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isEventHandler(method, true)

        where:
        [result, method] << methodsOf(MyEventHandler, true).plus(methodsOf(MyContributor, false)).plus(methodsOf(MyAddon, false))
    }

    void "isResourceResolverMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isResourceResolverMethod(method, true)

        where:
        [result, method] << methodsOf(ResourceResolver, true).plus(methodsOf(ResourceHandler, false))
    }

    void "isResourceHandlerMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isResourceHandlerMethod(method, true)

        where:
        [result, method] << methodsOf(ResourceHandler, true).plus(methodsOf(ThreadingHandler, false))
    }

    void "isEventPublisherMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isEventPublisherMethod(method, true)

        where:
        [result, method] << methodsOf(EventPublisher, true).plus(methodsOf(ResourceHandler, false))
    }

    void "isMessageSourceMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isMessageSourceMethod(method, true)

        where:
        [result, method] << methodsOf(MessageSource, true).plus(methodsOf(ResourceHandler, false))
    }

    void "isMvcMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isMvcMethod(method, true)

        where:
        [result, method] << methodsOf(MVCHandler, true)
            .plus(methodsOf(BasiliskMvcArtifact, true))
            .plus(methodsOf(EventPublisher, false))
    }

    void "isThreadingMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isThreadingMethod(method, true)

        where:
        [result, method] << methodsOf(ThreadingHandler, true).plus(methodsOf(ResourceHandler, false))
    }

    void "isArtifactMethod() returns #result for '#method' (java.lang.reflect.Method)"() {
        expect:
        assert result == BasiliskClassUtils.isArtifactMethod(method, true)

        where:
        [result, method] << methodsOf(BasiliskArtifact, true).plus(methodsOf(EventPublisher, false))
    }

    void "isSetterMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isSetterMethod(method)

        where:
        [result, method] << methodDescriptorsOf(MySetter, true).plus(methodDescriptorsOf(MyGetter, false))
    }

    void "isGetterMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isGetterMethod(method)

        where:
        [result, method] << methodDescriptorsOf(MyGetter, true).plus(methodDescriptorsOf(MySetter, false))
    }

    void "isContributionMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isContributionMethod(method)

        where:
        [result, method] << methodDescriptorsOf(MyContributor, true).plus(methodDescriptorsOf(MyEventHandler, false))
    }

    void "isEventHandler() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isEventHandler(method)

        where:
        [result, method] << methodDescriptorsOf(MyEventHandler, true).plus(methodDescriptorsOf(MyContributor, false)).plus(methodsOf(MyAddon, false))
    }

    void "isResourceResolverMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isResourceResolverMethod(method)

        where:
        [result, method] << methodDescriptorsOf(ResourceResolver, true).plus(methodDescriptorsOf(ResourceHandler, false))
    }

    void "isResourceHandlerMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isResourceHandlerMethod(method)

        where:
        [result, method] << methodDescriptorsOf(ResourceHandler, true).plus(methodDescriptorsOf(ThreadingHandler, false))
    }

    void "isEventPublisherMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isEventPublisherMethod(method)

        where:
        [result, method] << methodDescriptorsOf(EventPublisher, true).plus(methodDescriptorsOf(ResourceHandler, false))
    }

    void "isMessageSourceMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isMessageSourceMethod(method)

        where:
        [result, method] << methodDescriptorsOf(MessageSource, true).plus(methodDescriptorsOf(ResourceHandler, false))
    }

    void "isMvcMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isMvcMethod(method)

        where:
        [result, method] << methodDescriptorsOf(MVCHandler, true)
            .plus(methodDescriptorsOf(BasiliskMvcArtifact, true))
            .plus(methodDescriptorsOf(EventPublisher, false))
    }

    void "isThreadingMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isThreadingMethod(method)

        where:
        [result, method] << methodDescriptorsOf(ThreadingHandler, true).plus(methodDescriptorsOf(ResourceHandler, false))
    }

    void "isArtifactMethod() returns #result for '#method'"() {
        expect:
        assert result == BasiliskClassUtils.isArtifactMethod(method)

        where:
        [result, method] << methodDescriptorsOf(BasiliskArtifact, true).plus(methodDescriptorsOf(EventPublisher, false))
    }

    void "InvokeInstanceMethod can resolved overloaded calls inputs=#inputs"() {
        expect:
        output == BasiliskClassUtils.invokeInstanceMethod(new Bean(), 'doSomething', *inputs)

        where:
        inputs        || output
        ['foo', 1]    || 'java.lang.String:java.lang.Integer'
        ['foo', []]   || 'java.lang.String:java.lang.Object'
        ['foo', null] || 'java.lang.String:java.lang.Object'
        ['foo']       || 'java.lang.String'
    }

    void "Verify arguments for requireNonEmpty: null byte array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((byte[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null boolean array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((boolean[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null short array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((short[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null int array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((int[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null long array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((long[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null float array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((float[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null double array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((double[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null char array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((char[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null Object array throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((Object[]) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null Collection throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((Collection) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null Map throws NPE"() {
        when:
        BasiliskClassUtils.requireNonEmpty((Map) null)

        then:
        thrown(NullPointerException)
    }

    void "Verify arguments for requireNonEmpty: null message for  #input (#input.getClass()) throws IAE"() {
        when:
        BasiliskClassUtils.requireNonEmpty(input, null)

        then:
        thrown(IllegalArgumentException)

        where:
        input << [
            new byte[0],
            new boolean[0],
            new short[0],
            new int[0],
            new long[0],
            new float[0],
            new double[0],
            new char[0],
            new Object[0],
            [],
            [:]
        ]
    }

    void "Verify arguments for requireNonEmpty: empty  #input (#input.getClass()) throws ISE"() {
        when:
        BasiliskClassUtils.requireNonEmpty(input)

        then:
        thrown(IllegalStateException)

        where:
        input << [
            new byte[0],
            new boolean[0],
            new short[0],
            new int[0],
            new long[0],
            new float[0],
            new double[0],
            new char[0],
            new Object[0],
            [],
            [:]
        ]
    }

    void "Verify arguments for requireNonEmpty: empty  #input (#input.getClass()) with message throws ISE"() {
        when:
        BasiliskClassUtils.requireNonEmpty(input, 'message')

        then:
        thrown(IllegalStateException)

        where:
        input << [
            new byte[0],
            new boolean[0],
            new short[0],
            new int[0],
            new long[0],
            new float[0],
            new double[0],
            new char[0],
            new Object[0],
            [],
            [:]
        ]
    }

    void "RequireNonEmpty is successful for #input (#input.getClass())"() {
        when:
        def output1 = BasiliskClassUtils.requireNonEmpty(input)
        def output2 = BasiliskClassUtils.requireNonEmpty(input, 'message')

        then:
        input == output1
        input == output2

        where:
        input << [
            [(byte) 1] as byte[],
            [(short) 1] as short[],
            [(int) 1] as int[],
            [(long) 1] as long[],
            [(float) 1] as float[],
            [(double) 1] as double[],
            [(char) 1] as char[],
            [true] as boolean[],
            [new Object()] as Object[],
            [1],
            [key: 'value']
        ]
    }

    private static List methodDescriptorsOf(Class<?> type, boolean result) {
        List data = []
        for (Method m : type.methods) {
            data << [result, MethodDescriptor.forMethod(m, true)]
        }
        data
    }

    private static List methodsOf(Class<?> type, boolean result) {
        List data = []
        for (Method m : type.methods) {
            data << [result, m]
        }
        data
    }

    static class Bean {
        def doSomething(String arg0, Integer arg1) {
            String.name + ':' + Integer.name
        }

        def doSomething(String arg0, Object arg1) {
            String.name + ':' + Object.name
        }

        def doSomething(String arg0) {
            String.name
        }
    }

    static interface MyEventHandler {
        void onMyEvent()
    }

    static interface MyContributor {
        void withSomething()
    }

    static interface MyAddon extends BasiliskAddon {

    }

    static interface MyGetter {
        String getSomething()
    }

    static interface MySetter {
        void setSomething(String s)
    }
}
