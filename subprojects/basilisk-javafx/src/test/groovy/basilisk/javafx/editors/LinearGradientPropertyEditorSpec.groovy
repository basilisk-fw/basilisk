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
package basilisk.javafx.editors

import com.googlecode.openbeans.PropertyEditor
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static javafx.scene.paint.Color.BLACK
import static javafx.scene.paint.Color.WHITE

@Unroll
class LinearGradientPropertyEditorSpec extends Specification {
    @Shared
    private LinearGradient sharedPaint = new LinearGradient(1, 2, 3, 4, false, CycleMethod.NO_CYCLE, [new Stop(0d, BLACK), new Stop(1d, WHITE)])

    @Shared
    private LinearGradient sharedPaintC = new LinearGradient(1, 2, 3, 4, false, CycleMethod.REPEAT, [new Stop(0d, BLACK), new Stop(1d, WHITE)])

    void "LinearGradient format '#format' is supported"() {
        setup:
        PropertyEditor editor = new LinearGradientPropertyEditor()

        when:
        editor.value = format

        then:

        paintsAreEqual value, editor.value

        where:
        value        | format
        null         | null
        null         | ''
        null         | ' '
        null         | []
        null         | [:]
        sharedPaint  | [1, 2, 3, 4, false, [new Stop(0d, BLACK), new Stop(1d, WHITE)]]
        sharedPaintC | [1, 2, 3, 4, false, [new Stop(0d, BLACK), new Stop(1d, WHITE)], 'REPEAT']
        sharedPaintC | [1, 2, 3, 4, false, [new Stop(0d, BLACK), new Stop(1d, WHITE)], CycleMethod.REPEAT]
        sharedPaint  | [sx: 1, sy: 2, ex: 3, ey: 4, stops: [new Stop(0d, BLACK), new Stop(1d, WHITE)]]
        sharedPaintC | [sx: 1, sy: 2, ex: 3, ey: 4, stops: [new Stop(0d, BLACK), new Stop(1d, WHITE)], cycle: 'REPEAT']
        sharedPaintC | [sx: 1, sy: 2, ex: 3, ey: 4, stops: [new Stop(0d, BLACK), new Stop(1d, WHITE)], cycle: CycleMethod.REPEAT]
        sharedPaint  | sharedPaint
    }

    private static void paintsAreEqual(LinearGradient p1, LinearGradient p2) {
        if (p1 == null) {
            assert p2 == null
        } else {
            assert p1.startX == p2.startX &&
                p1.startY == p2.startY &&
                p1.endX == p2.endX &&
                p1.endY == p2.endY &&
                p1.proportional == p2.proportional &&
                p1.stops*.toString() == p2.stops*.toString() &&
                p1.cycleMethod == p2.cycleMethod
        }
    }

    void "Invalid gradient format '#format'"() {
        setup:

        PropertyEditor editor = new LinearGradientPropertyEditor()

        when:
        editor.value = format

        then:

        thrown(IllegalArgumentException)

        where:
        format << [
            'garbage',
            '1, 2, 3',
            '1, 2, 3, 4, 5',
            [1, 2, 3],
            [1, 2, 3, 4, 5],
            [sx: 'a'],
            [new Object()],
            [sx: new Object()],
            new Object()
        ]
    }
}
