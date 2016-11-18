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
package basilisk.javafx.support

import groovy.transform.Canonical
import javafx.beans.binding.Binding
import javafx.beans.binding.ObjectBinding
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.BiFunction
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier

import static java.lang.Boolean.parseBoolean
import static java.lang.Double.parseDouble
import static java.lang.Float.parseFloat
import static java.lang.Integer.parseInt
import static java.lang.Long.parseLong

@Unroll
class BindingUtilsSpec extends Specification {
    def "Map #type to object literal"() {
        given:
        ObjectBinding binding = BindingUtils.mapToObject(source)

        expect:
        !binding.get()

        when:
        source.set(value)

        then:
        value == binding.get()

        where:
        type      | source                      | value
        'String'  | new SimpleStringProperty()  | '1'
        'Boolean' | new SimpleBooleanProperty() | true
        'Integer' | new SimpleIntegerProperty() | 1
        'Long'    | new SimpleLongProperty()    | 1L
        'Float'   | new SimpleFloatProperty()   | 1f
        'Double'  | new SimpleDoubleProperty()  | 1d
    }

    def "Map object observable to #type binding"() {
        given:
        Binding binding = BindingUtils."mapTo${type}"(source)

        expect:
        !binding.get()

        when:
        source.set(value)

        then:
        value == binding.get()

        where:
        type      | source                                   | value
        'String'  | new SimpleObjectProperty<String>('')     | '1'
        'Boolean' | new SimpleObjectProperty<Boolean>(false) | true
        'Integer' | new SimpleObjectProperty<Integer>(0)     | 1
        'Long'    | new SimpleObjectProperty<Long>(0L)       | 1L
        'Float'   | new SimpleObjectProperty<Float>(0f)      | 1f
        'Double'  | new SimpleObjectProperty<Double>(0d)     | 1d
    }

    def "Map#type with function"() {
        given:
        Binding binding = BindingUtils."map${type}"(source, function as Function)

        when:
        source.set(value)

        then:
        result == binding.get()

        where:
        type      | source                          | value      | result     | function
        'Integer' | new SimpleIntegerProperty()     | 1          | 2          | { i -> i * 2 }
        'Long'    | new SimpleLongProperty()        | 1L         | 2L         | { i -> i * 2 }
        'Float'   | new SimpleFloatProperty()       | 1f         | 2f         | { i -> (i * 2f) as float }
        'Double'  | new SimpleDoubleProperty()      | 1d         | 2d         | { i -> i * 2 }
        'String'  | new SimpleStringProperty()      | '1'        | '11'       | { i -> i * 2 }
        'Boolean' | new SimpleBooleanProperty()     | false      | true       | { i -> true }
        'Object'  | new SimpleObjectProperty<Box>() | new Box(1) | new Box(2) | { i -> new Box(2) }
    }

    def "Map#type with observable function"() {
        given:
        ObjectProperty mapper = new SimpleObjectProperty(function1 as Function)
        Binding binding = BindingUtils."map${type}"(source, mapper)

        when:
        source.set(value)

        then:
        result1 == binding.get()

        when:
        mapper.set(function2 as Function)

        then:
        result2 == binding.get()

        where:
        type      | source                          | value      | result1    | result2    | function1                  | function2
        'Integer' | new SimpleIntegerProperty()     | 1          | 2          | 3          | { i -> i * 2 }             | { i -> i + 2 }
        'Long'    | new SimpleLongProperty()        | 1L         | 2L         | 3L         | { i -> i * 2 }             | { i -> i + 2 }
        'Float'   | new SimpleFloatProperty()       | 1f         | 2f         | 3f         | { i -> (i * 2f) as float } | { i -> (i + 2f) as float }
        'Double'  | new SimpleDoubleProperty()      | 1d         | 2d         | 3d         | { i -> i * 2 }             | { i -> i + 2 }
        'String'  | new SimpleStringProperty()      | '1'        | '11'       | '12'       | { i -> i * 2 }             | { i -> i + 2 }
        'Boolean' | new SimpleBooleanProperty()     | false      | true       | false      | { i -> true }              | { i -> false }
        'Object'  | new SimpleObjectProperty<Box>() | new Box(1) | new Box(2) | new Box(3) | { i -> new Box(2) }        | { i -> new Box(3) }
    }

    def "Map#type with function (observables) value1=#value1 value2=#value2"() {
        given:
        Binding binding = BindingUtils."map${type}"(ob1, ob2, defaultValue, function as BiFunction)

        when:
        ob1.set(value1)
        ob2.set(value2)

        then:
        result == binding.get()

        where:
        type      | ob1                             | ob2                             | defaultValue | value1     | value2     | result     | function
        'Integer' | new SimpleIntegerProperty()     | new SimpleIntegerProperty()     | 0            | 1          | 2          | 3          | { a, b -> a + b }
        'Long'    | new SimpleLongProperty()        | new SimpleLongProperty()        | 0L           | 1L         | 2L         | 3L         | { a, b -> a + b }
        'Float'   | new SimpleFloatProperty()       | new SimpleFloatProperty()       | 0f           | 1f         | 2f         | 3f         | { a, b -> (a + b) as float }
        'Double'  | new SimpleDoubleProperty()      | new SimpleDoubleProperty()      | 0d           | 1d         | 2d         | 3d         | { a, b -> a + b }
        'Boolean' | new SimpleBooleanProperty()     | new SimpleBooleanProperty()     | false        | false      | true       | true       | { a, b -> a || b }
        'String'  | new SimpleStringProperty()      | new SimpleStringProperty()      | '0'          | '1'        | '2'        | '12'       | { a, b -> a + b }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | null       | null       | new Box(3) | { a, b -> new Box(4) }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | new Box(1) | null       | new Box(3) | { a, b -> new Box(4) }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | null       | new Box(2) | new Box(3) | { a, b -> new Box(4) }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | new Box(1) | new Box(2) | new Box(4) | { a, b -> new Box(4) }
    }

    def "Map#type with observable function (observables)"() {
        given:
        ObjectProperty mapper = new SimpleObjectProperty(function1 as BiFunction)
        Binding binding = BindingUtils."map${type}"(ob1, ob2, defaultValue, mapper)

        when:
        ob1.set(value1)
        ob2.set(value2)

        then:
        result1 == binding.get()

        when:
        mapper.set(function2 as BiFunction)

        then:
        result2 == binding.get()

        where:
        type      | ob1                             | ob2                             | defaultValue | value1     | value2     | result1    | result2    | function1                    | function2
        'Integer' | new SimpleIntegerProperty()     | new SimpleIntegerProperty()     | 0            | 1          | 2          | 3          | -1         | { a, b -> a + b }            | { a, b -> a - b }
        'Long'    | new SimpleLongProperty()        | new SimpleLongProperty()        | 0L           | 1L         | 2L         | 3L         | -1L        | { a, b -> a + b }            | { a, b -> a - b }
        'Float'   | new SimpleFloatProperty()       | new SimpleFloatProperty()       | 0f           | 1f         | 2f         | 3f         | -1f        | { a, b -> (a + b) as float } | { a, b -> (a - b) as float }
        'Double'  | new SimpleDoubleProperty()      | new SimpleDoubleProperty()      | 0d           | 1d         | 2d         | 3d         | -1d        | { a, b -> a + b }            | { a, b -> a - b }
        'Boolean' | new SimpleBooleanProperty()     | new SimpleBooleanProperty()     | false        | false      | true       | true       | false      | { a, b -> a || b }           | { a, b -> a && b }
        'String'  | new SimpleStringProperty()      | new SimpleStringProperty()      | '0'          | '1'        | '2'        | '12'       | '3'        | { a, b -> a + b }            | { a, b -> '3' }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | null       | null       | new Box(3) | new Box(3) | { a, b -> new Box(4) }       | { a, b -> new Box(5) }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | new Box(1) | null       | new Box(3) | new Box(3) | { a, b -> new Box(4) }       | { a, b -> new Box(5) }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | null       | new Box(2) | new Box(3) | new Box(3) | { a, b -> new Box(4) }       | { a, b -> new Box(5) }
        'Object'  | new SimpleObjectProperty<Box>() | new SimpleObjectProperty<Box>() | new Box(3)   | new Box(1) | new Box(2) | new Box(4) | new Box(5) | { a, b -> new Box(4) }       | { a, b -> new Box(5) }
    }

    def "ReduceThenMapTo#type list with functions and default value"() {
        given:
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, defaultValue, reducer as BinaryOperator, mapper as Function)

        expect:
        mapper(defaultValue) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | defaultValue | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | 'true'       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | '4'          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | '4'          | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | '4'          | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | '4'          | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "ReduceThenMapTo#type list with functions and supplier"() {
        given:
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, supplier as Supplier, reducer as BinaryOperator, mapper as Function)

        expect:
        mapper(supplier()) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | supplier   | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | { 'true' } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | { '4' }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | { '4' }    | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | { '4' }    | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | { '4' }    | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | { 'A' }    | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "ReduceThenMapTo#type list with observable functions and default value"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, defaultValue, observableReducer, observableMapper)

        expect:
        mapper1(defaultValue) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | defaultValue | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2           | mapper2                               | result3 | result4
        'Boolean' | 'true'       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> 'true' } | { i -> false }                        | true    | false
        'Integer' | '4'          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> '5' }    | { i -> 2 * parseInt(i) }              | 5       | 10
        'Long'    | '4'          | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> '5' }    | { i -> 2 * parseLong(i) }             | 5L      | 10L
        'Float'   | '4'          | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> '5' }    | { i -> (2 * parseFloat(i)) as float } | 5f      | 10f
        'Double'  | '4'          | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> '5' }    | { i -> 2 * parseDouble(i) }           | 5d      | 10d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> 'D' }    | { i -> i * 3 }                        | 'DD'    | 'DDD'
    }

    def "ReduceThenMapTo#type list with observable functions and supplier"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, supplier as Supplier, observableReducer, observableMapper)

        expect:
        mapper1(supplier()) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | supplier  | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2           | mapper2                               | result3 | result4
        'Boolean' | {'true' } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> 'true' } | { i -> false }                        | true    | false
        'Integer' | {'4' }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> '5' }    | { i -> 2 * parseInt(i) }              | 5       | 10
        'Long'    | {'4' }    | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> '5' }    | { i -> 2 * parseLong(i) }             | 5L      | 10L
        'Float'   | {'4' }    | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> '5' }    | { i -> (2 * parseFloat(i)) as float } | 5f      | 10f
        'Double'  | {'4' }    | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> '5' }    | { i -> 2 * parseDouble(i) }           | 5d      | 10d
        'String'  | {'A' }    | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> 'D' }    | { i -> i * 3 }                        | 'DD'    | 'DDD'
    }

    def "ReduceThenMapTo#type set with functions and default value"() {
        given:
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, defaultValue, reducer as BinaryOperator, mapper as Function)

        expect:
        mapper(defaultValue) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | defaultValue | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | 'true'       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | '4'          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | '4'          | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | '4'          | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | '4'          | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "ReduceThenMapTo#type set with functions and supplier"() {
        given:
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, supplier as Supplier, reducer as BinaryOperator, mapper as Function)

        expect:
        mapper(supplier()) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | supplier   | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | { 'true' } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | { '4' }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | { '4' }    | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | { '4' }    | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | { '4' }    | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | { 'A' }    | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "ReduceThenMapTo#type set with observable functions and default value"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, defaultValue, observableReducer, observableMapper)

        expect:
        mapper1(defaultValue) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | defaultValue | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2           | mapper2                               | result3 | result4
        'Boolean' | 'true'       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> 'true' } | { i -> false }                        | true    | false
        'Integer' | '4'          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> '5' }    | { i -> 2 * parseInt(i) }              | 5       | 10
        'Long'    | '4'          | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> '5' }    | { i -> 2 * parseLong(i) }             | 5L      | 10L
        'Float'   | '4'          | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> '5' }    | { i -> (2 * parseFloat(i)) as float } | 5f      | 10f
        'Double'  | '4'          | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> '5' }    | { i -> 2 * parseDouble(i) }           | 5d      | 10d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> 'D' }    | { i -> i * 3 }                        | 'DD'    | 'DDD'
    }

    def "ReduceThenMapTo#type set with observable functions and supplier"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, supplier as Supplier, observableReducer, observableMapper)

        expect:
        mapper1(supplier()) == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | supplier  | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2           | mapper2                               | result3 | result4
        'Boolean' | {'true' } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> 'true' } | { i -> false }                        | true    | false
        'Integer' | {'4' }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> '5' }    | { i -> 2 * parseInt(i) }              | 5       | 10
        'Long'    | {'4' }    | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> '5' }    | { i -> 2 * parseLong(i) }             | 5L      | 10L
        'Float'   | {'4' }    | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> '5' }    | { i -> (2 * parseFloat(i)) as float } | 5f      | 10f
        'Double'  | {'4' }    | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> '5' }    | { i -> 2 * parseDouble(i) }           | 5d      | 10d
        'String'  | {'A' }    | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> 'D' }    | { i -> i * 3 }                        | 'DD'    | 'DDD'
    }

    def "ReduceThenMapTo#type map with functions and default value"() {
        given:
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, defaultValue, reducer as BinaryOperator, mapper as Function)

        expect:
        mapper(defaultValue) == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        where:
        type      | defaultValue | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | 'true'       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | '4'          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | '4'          | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | '4'          | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | '4'          | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "ReduceThenMapTo#type map with functions and supplier"() {
        given:
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, supplier as Supplier, reducer as BinaryOperator, mapper as Function)

        expect:
        mapper(supplier()) == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        where:
        type      | supplier   | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | { 'true' } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | { '4' }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | { '4' }    | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | { '4' }    | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | { '4' }    | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | { 'A' }    | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "ReduceThenMapTo#type map with observable functions and default value"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, defaultValue, observableReducer, observableMapper)

        expect:
        mapper1(defaultValue) == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | defaultValue | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2           | mapper2                               | result3 | result4
        'Boolean' | 'true'       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> 'true' } | { i -> false }                        | true    | false
        'Integer' | '4'          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> '5' }    | { i -> 2 * parseInt(i) }              | 5       | 10
        'Long'    | '4'          | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> '5' }    | { i -> 2 * parseLong(i) }             | 5L      | 10L
        'Float'   | '4'          | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> '5' }    | { i -> (2 * parseFloat(i)) as float } | 5f      | 10f
        'Double'  | '4'          | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> '5' }    | { i -> 2 * parseDouble(i) }           | 5d      | 10d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> 'D' }    | { i -> i * 3 }                        | 'DD'    | 'DDD'
    }

    def "ReduceThenMapTo#type map with observable functions and supplier"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."reduceThenMapTo${type}"(items, supplier as Supplier, observableReducer, observableMapper)

        expect:
        mapper1(supplier()) == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | supplier  | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2           | mapper2                               | result3 | result4
        'Boolean' | {'true' } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> 'true' } | { i -> false }                        | true    | false
        'Integer' | {'4' }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> '5' }    | { i -> 2 * parseInt(i) }              | 5       | 10
        'Long'    | {'4' }    | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> '5' }    | { i -> 2 * parseLong(i) }             | 5L      | 10L
        'Float'   | {'4' }    | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> '5' }    | { i -> (2 * parseFloat(i)) as float } | 5f      | 10f
        'Double'  | {'4' }    | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> '5' }    | { i -> 2 * parseDouble(i) }           | 5d      | 10d
        'String'  | {'A' }    | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> 'D' }    | { i -> i * 3 }                        | 'DD'    | 'DDD'
    }

    def "MapTo#type then reduce list with functions and default value"() {
        given:
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, defaultValue, mapper as Function, reducer as BinaryOperator)

        expect:
        defaultValue == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | defaultValue | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | true         | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | 4            | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | 4L           | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | 4f           | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | 4d           | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "MapTo#type then reduce list with functions and supplier"() {
        given:
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, supplier as Supplier, mapper as Function, reducer as BinaryOperator)

        expect:
        supplier() == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | supplier | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | { true } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | { 4 }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | { 4L }   | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | { 4f }   | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | { 4d }   | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | { 'A' }  | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "MapTo#type then reduce list with observable functions and default value"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, defaultValue, observableMapper, observableReducer)

        expect:
        defaultValue == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | defaultValue | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2                     | mapper2        | result3  | result4
        'Boolean' | true         | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> true }             | { i -> false } | true     | true
        'Integer' | 3            | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> b * 2 }            | { i -> 5 }     | 4        | 10
        'Long'    | 3L           | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> b * 2 }            | { i -> 5L }    | 4L       | 10L
        'Float'   | 3f           | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> (b * 2) as float } | { i -> 5f }    | 4f       | 10f
        'Double'  | 3d           | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> b * 2 }            | { i -> 5d }    | 4d       | 10d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> b * 3 }            | { i -> 'D' }   | 'CCCCCC' | 'DDD'
    }

    def "MapTo#type then reduce list with observable functions and supplier"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableList items = FXCollections.observableArrayList()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, supplier as Supplier, observableMapper, observableReducer)

        expect:
        supplier() == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | supplier | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2                     | mapper2        | result3  | result4
        'Boolean' | { true } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> true }             | { i -> false } | true     | true
        'Integer' | { 3 }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> b * 2 }            | { i -> 5 }     | 4        | 10
        'Long'    | { 3L }   | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> b * 2 }            | { i -> 5L }    | 4L       | 10L
        'Float'   | { 3f }   | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> (b * 2) as float } | { i -> 5f }    | 4f       | 10f
        'Double'  | { 3d }   | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> b * 2 }            | { i -> 5d }    | 4d       | 10d
        'String'  | { 'A' }  | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> b * 3 }            | { i -> 'D' }   | 'CCCCCC' | 'DDD'
    }

    def "MapTo#type then reduce set with functions and default value"() {
        given:
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, defaultValue, mapper as Function, reducer as BinaryOperator)

        expect:
        defaultValue == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | defaultValue | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | true         | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | 3            | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | 3L           | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | 3f           | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | 3d           | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "MapTo#type then reduce set with functions and supplier"() {
        given:
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, supplier as Supplier, mapper as Function, reducer as BinaryOperator)

        expect:
        supplier() == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        where:
        type      | supplier | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | { true } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | { 3 }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | { 3L }   | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | { 3f }   | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | { 3d }   | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | { 'A' }  | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "MapTo#type then reduce set with observable functions and default value"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, defaultValue, observableMapper, observableReducer)

        expect:
        defaultValue == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | defaultValue | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2                     | mapper2        | result3  | result4
        'Boolean' | true         | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> true }             | { i -> false } | true     | true
        'Integer' | 3            | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> b * 2 }            | { i -> 5 }     | 4        | 10
        'Long'    | 3L           | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> b * 2 }            | { i -> 5L }    | 4L       | 10L
        'Float'   | 3f           | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> (b * 2) as float } | { i -> 5f }    | 4f       | 10f
        'Double'  | 3d           | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> b * 2 }            | { i -> 5d }    | 4d       | 10d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> b * 3 }            | { i -> 'D' }   | 'CCCCCC' | 'DDD'
    }

    def "MapTo#type then reduce set with observable functions and supplier"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableSet items = FXCollections.observableSet()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, supplier as Supplier, observableMapper, observableReducer)

        expect:
        supplier() == binding.get()

        when:
        items << value1

        then:
        result1 == binding.get()

        when:
        items << value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | supplier | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2                     | mapper2        | result3  | result4
        'Boolean' | { true } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> true }             | { i -> false } | true     | true
        'Integer' | { 3 }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> b * 2 }            | { i -> 5 }     | 4        | 10
        'Long'    | { 3L }   | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> b * 2 }            | { i -> 5L }    | 4L       | 10L
        'Float'   | { 3f }   | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> (b * 2) as float } | { i -> 5f }    | 4f       | 10f
        'Double'  | { 3d }   | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> b * 2 }            | { i -> 5d }    | 4d       | 10d
        'String'  | { 'A' }  | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> b * 3 }            | { i -> 'D' }   | 'CCCCCC' | 'DDD'
    }

    def "MapTo#type then reduce map with functions and default value"() {
        given:
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, defaultValue, mapper as Function, reducer as BinaryOperator)

        expect:
        defaultValue == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        where:
        type      | defaultValue | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | true       | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | 3          | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | 3L         | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | 3f         | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | 3d         | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | 'A'        | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "MapTo#type then reduce map with functions and supplier"() {
        given:
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, supplier as Supplier, mapper as Function, reducer as BinaryOperator)

        expect:
        supplier() == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        where:
        type      | supplier | reducer       | mapper                   | value1 | value2  | result1 | result2
        'Boolean' | { true } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false
        'Integer' | { 3 }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2
        'Long'    | { 3L }   | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L
        'Float'   | { 3f }   | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f
        'Double'  | { 3d }   | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d
        'String'  | { 'A' }  | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'
    }

    def "MapTo#type then reduce map with observable functions and default value"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, defaultValue, observableMapper, observableReducer)

        expect:
        defaultValue == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | defaultValue | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2                     | mapper2        | result3  | result4
        'Boolean' | true         | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> true }             | { i -> false } | true     | true
        'Integer' | 3            | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> b * 2 }            | { i -> 5 }     | 4        | 10
        'Long'    | 3L           | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> b * 2 }            | { i -> 5L }    | 4L       | 10L
        'Float'   | 3f           | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> (b * 2) as float } | { i -> 5f }    | 4f       | 10f
        'Double'  | 3d           | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> b * 2 }            | { i -> 5d }    | 4d       | 10d
        'String'  | 'A'          | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> b * 3 }            | { i -> 'D' }   | 'CCCCCC' | 'DDD'
    }

    def "MapTo#type then reduce map with observable functions and supplier"() {
        given:
        ObjectProperty observableReducer = new SimpleObjectProperty(reducer1 as BinaryOperator)
        ObjectProperty observableMapper = new SimpleObjectProperty(mapper1 as Function)
        ObservableMap items = FXCollections.observableHashMap()
        Binding binding = BindingUtils."mapTo${type}ThenReduce"(items, supplier as Supplier, observableMapper, observableReducer)

        expect:
        supplier() == binding.get()

        when:
        items.key1 = value1

        then:
        result1 == binding.get()

        when:
        items.key2 = value2

        then:
        result2 == binding.get()

        when:
        observableReducer.set(reducer2 as BinaryOperator)

        then:
        result3 == binding.get()

        when:
        observableMapper.set(mapper2 as Function)

        then:
        result4 == binding.get()

        where:
        type      | supplier | reducer1      | mapper1                  | value1 | value2  | result1 | result2 | reducer2                     | mapper2        | result3  | result4
        'Boolean' | { true } | { a, b -> b } | { i -> parseBoolean(i) } | 'true' | 'false' | true    | false   | { a, b -> true }             | { i -> false } | true     | true
        'Integer' | { 3 }    | { a, b -> b } | { i -> parseInt(i) }     | '1'    | '2'     | 1       | 2       | { a, b -> b * 2 }            | { i -> 5 }     | 4        | 10
        'Long'    | { 3L }   | { a, b -> b } | { i -> parseLong(i) }    | '1'    | '2'     | 1L      | 2L      | { a, b -> b * 2 }            | { i -> 5L }    | 4L       | 10L
        'Float'   | { 3f }   | { a, b -> b } | { i -> parseFloat(i) }   | '1'    | '2'     | 1f      | 2f      | { a, b -> (b * 2) as float } | { i -> 5f }    | 4f       | 10f
        'Double'  | { 3d }   | { a, b -> b } | { i -> parseDouble(i) }  | '1'    | '2'     | 1d      | 2d      | { a, b -> b * 2 }            | { i -> 5d }    | 4d       | 10d
        'String'  | { 'A' }  | { a, b -> b } | { i -> i + i }           | 'B'    | 'C'     | 'BB'    | 'CC'    | { a, b -> b * 3 }            | { i -> 'D' }   | 'CCCCCC' | 'DDD'
    }

    @Canonical
    private static class Box {
        int id
    }
}
