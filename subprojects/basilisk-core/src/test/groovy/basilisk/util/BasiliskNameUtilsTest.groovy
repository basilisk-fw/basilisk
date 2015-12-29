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
package basilisk.util

class BasiliskNameUtilsTest extends GroovyTestCase {
    void testCapitalize() {
        assert '' == BasiliskNameUtils.capitalize('')
        assert ' ' == BasiliskNameUtils.capitalize(' ')
        assert null == BasiliskNameUtils.capitalize(null)
        assert 'A' == BasiliskNameUtils.capitalize('a')
        assert 'Basilisk' == BasiliskNameUtils.capitalize('basilisk')
    }

    void testUncapitalize() {
        assert '' == BasiliskNameUtils.uncapitalize('')
        assert ' ' == BasiliskNameUtils.uncapitalize(' ')
        assert null == BasiliskNameUtils.uncapitalize(null)
        assert 'a' == BasiliskNameUtils.uncapitalize('A')
        assert 'basilisk' == BasiliskNameUtils.uncapitalize('Basilisk')
    }

    void testGetSetterName() {
        assert 'setA' == BasiliskNameUtils.getSetterName('a')
        assert 'setBasilisk' == BasiliskNameUtils.getSetterName('basilisk')
    }

    void testGetGetterName() {
        assert 'getA' == BasiliskNameUtils.getGetterName('a')
        assert 'getBasilisk' == BasiliskNameUtils.getGetterName('basilisk')
    }

    void testGetClassName() {
        shouldFail(IllegalArgumentException) {
            BasiliskNameUtils.getClassName(null, 'Controller')
        }
        shouldFail(IllegalArgumentException) {
            BasiliskNameUtils.getClassName('', 'Controller')
        }
        shouldFail(IllegalArgumentException) {
            BasiliskNameUtils.getClassName(' ', 'Controller')
        }

        assert 'PersonController' == BasiliskNameUtils.getClassName('person', 'Controller')
        assert 'Person' == BasiliskNameUtils.getClassName('person', '')
    }

    void testGetPropertyName() {
        assert 'foo' == BasiliskNameUtils.getPropertyName(Foo)
    }

    void testGetPropertyNameRepresentation() {
        assert 'foo' == BasiliskNameUtils.getPropertyName('foo')
        assert 'foo' == BasiliskNameUtils.getPropertyName('Foo')
        assert 'foo' == BasiliskNameUtils.getPropertyName('basilisk.util.Foo')
        assert 'fooBar' == BasiliskNameUtils.getPropertyName('Foo Bar')
    }

    void testGetShortName() {
        assert 'Foo' == BasiliskNameUtils.getShortName('basilisk.util.Foo')
        assert 'Foo' == BasiliskNameUtils.getShortName(Foo)
    }

    void testGetClassNameRepresentation() {
        assert "MyClass" == BasiliskNameUtils.getClassNameRepresentation("my-class")
        assert "MyClass" == BasiliskNameUtils.getClassNameRepresentation("MyClass")
        assert "F" == BasiliskNameUtils.getClassNameRepresentation(".f")
        assert "AB" == BasiliskNameUtils.getClassNameRepresentation(".a.b")
        assert "AlphaBakerCharlie" == BasiliskNameUtils.getClassNameRepresentation(".alpha.baker.charlie")
    }

    void testGetNaturalName() {
        assert "First Name" == BasiliskNameUtils.getNaturalName("firstName")
        assert "URL" == BasiliskNameUtils.getNaturalName("URL")
        assert "Local URL" == BasiliskNameUtils.getNaturalName("localURL")
        assert "URL local" == BasiliskNameUtils.getNaturalName("URLlocal")
        assert "My Domain Class" == BasiliskNameUtils.getNaturalName("MyDomainClass")
        assert "My Domain Class" == BasiliskNameUtils.getNaturalName("com.myco.myapp.MyDomainClass")
    }

    void testGetLogicalName() {
        assert "Test" == BasiliskNameUtils.getLogicalName("TestController", "Controller")
        assert "Test" == BasiliskNameUtils.getLogicalName("org.music.TestController", "Controller")
    }

    void testGetLogicalPropertyName() {
        assert "myFunky" == BasiliskNameUtils.getLogicalPropertyName("MyFunkyController", "Controller")
        assert "HTML" == BasiliskNameUtils.getLogicalPropertyName("HTMLCodec", "Codec")
        assert "payRoll" == BasiliskNameUtils.getLogicalPropertyName("org.something.PayRollController", "Controller")
    }

    void testGetLogicalPropertyNameForArtefactWithSingleCharacterName() {
        assert "a" == BasiliskNameUtils.getLogicalPropertyName("AController", "Controller")
        assert "b" == BasiliskNameUtils.getLogicalPropertyName("BService", "Service")
    }

    void testGetLogicalPropertyNameForArtefactWithAllUpperCaseName() {
        assert "ABC" == BasiliskNameUtils.getLogicalPropertyName("ABCController", "Controller")
        assert "BCD" == BasiliskNameUtils.getLogicalPropertyName("BCDService", "Service")
    }

    void testIsBlank() {
        assert BasiliskNameUtils.isBlank(null), "'null' value should count as blank."
        assert BasiliskNameUtils.isBlank(""), "Empty string should count as blank."
        assert BasiliskNameUtils.isBlank("  "), "Spaces should count as blank."
        assert BasiliskNameUtils.isBlank("\t"), "A tab should count as blank."
        assert !BasiliskNameUtils.isBlank("\t  h"), "String with whitespace and non-whitespace should not count as blank."
        assert !BasiliskNameUtils.isBlank("test"), "String should not count as blank."
    }

    void testQuote() {
        assert " " == BasiliskNameUtils.quote(" ")
        assert "\" a\"" == BasiliskNameUtils.quote(" a")
        assert "\" a \"" == BasiliskNameUtils.quote(" a ")
        assert "\"a \"" == BasiliskNameUtils.quote("a ")
    }

    void testUnquote() {
        assert "" == BasiliskNameUtils.unquote("")
        assert " " == BasiliskNameUtils.unquote(" ")
        assert "" == BasiliskNameUtils.unquote("\"\"")
        assert " " == BasiliskNameUtils.unquote("\" \"")
        assert "foo" == BasiliskNameUtils.unquote("\"foo\"")
        assert "" == BasiliskNameUtils.unquote("''")
        assert " " == BasiliskNameUtils.unquote("' '")
        assert "foo" == BasiliskNameUtils.unquote("'foo'")
        assert "\"foo" == BasiliskNameUtils.unquote("\"foo")
        assert "foo\"" == BasiliskNameUtils.unquote("foo\"")
        assert "'foo" == BasiliskNameUtils.unquote("'foo")
        assert "foo'" == BasiliskNameUtils.unquote("foo'")
    }

    void testGetHyphenatedName() {
        assert "basilisk-name-utils" == BasiliskNameUtils.getHyphenatedName(BasiliskNameUtils.class)
        assert "basilisk-name-utils" == BasiliskNameUtils.getHyphenatedName(BasiliskNameUtils.class.getName())
    }

    void testGetClassNameForLowerCaseHyphenSeparatedName() {
        assert "BasiliskNameUtils" == BasiliskNameUtils.getClassNameForLowerCaseHyphenSeparatedName("basilisk-name-utils")
    }
}

@SuppressWarnings('EmptyClass')
class Foo {}
