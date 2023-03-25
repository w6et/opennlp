/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.util.eval;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import opennlp.tools.util.Span;

/**
 * Tests for the {@link FMeasure} class.
 */
public class FMeasureTest {

  private static final double DELTA = 1.0E-9d;

  private final Span[] gold = {
      new Span(8, 9),
      new Span(9, 10),
      new Span(10, 12),
      new Span(13, 14),
      new Span(14, 15),
      new Span(15, 16)
  };

  private final Span[] predicted = {
      new Span(14, 15),
      new Span(15, 16),
      new Span(100, 120),
      new Span(210, 220),
      new Span(220, 230)
  };

  private final Span[] predictedCompletelyDistinct = {
      new Span(100, 120),
      new Span(210, 220),
      new Span(211, 220),
      new Span(212, 220),
      new Span(220, 230)
  };

  private final Span[] goldToMerge = {
      new Span(8, 9),
      new Span(9, 10),
      new Span(11, 11),
      new Span(13, 14),
      new Span(14, 15),
      new Span(15, 16),
      new Span(18, 19),
  };

  private final Span[] predictedToMerge = {
      new Span(8, 9),
      new Span(14, 15),
      new Span(15, 16),
      new Span(100, 120),
      new Span(210, 220),
      new Span(220, 230)
  };


  /**
   * Test for the {@link FMeasure#countTruePositives(Object[], Object[])} method.
   */
  @Test
  public void testCountTruePositives() {
    Assertions.assertEquals(0, FMeasure.countTruePositives(new Object[] {}, new Object[] {}));
    Assertions.assertEquals(gold.length, FMeasure.countTruePositives(gold, gold));
    Assertions.assertEquals(0, FMeasure.countTruePositives(gold, predictedCompletelyDistinct));
    Assertions.assertEquals(2, FMeasure.countTruePositives(gold, predicted));
  }

  /**
   * Test for the {@link FMeasure#precision(Object[], Object[])} method.
   */
  @Test
  public void testPrecision() {
    Assertions.assertEquals(1.0d, FMeasure.precision(gold, gold), DELTA);
    Assertions.assertEquals(0, FMeasure.precision(gold, predictedCompletelyDistinct), DELTA);
    Assertions.assertEquals(Double.NaN, FMeasure.precision(gold, new Object[] {}), DELTA);
    Assertions.assertEquals(0, FMeasure.precision(new Object[] {}, gold), DELTA);
    Assertions.assertEquals(2d / predicted.length, FMeasure.precision(gold, predicted), DELTA);
  }

  /**
   * Test for the {@link FMeasure#recall(Object[], Object[])} method.
   */
  @Test
  public void testRecall() {
    Assertions.assertEquals(1.0d, FMeasure.recall(gold, gold), DELTA);
    Assertions.assertEquals(0, FMeasure.recall(gold, predictedCompletelyDistinct), DELTA);
    Assertions.assertEquals(0, FMeasure.recall(gold, new Object[] {}), DELTA);
    Assertions.assertEquals(Double.NaN, FMeasure.recall(new Object[] {}, gold), DELTA);
    Assertions.assertEquals(2d / gold.length, FMeasure.recall(gold, predicted), DELTA);
  }

  @Test
  public void testEmpty() {
    FMeasure fm = new FMeasure();
    Assertions.assertEquals(-1, fm.getFMeasure(), DELTA);
    Assertions.assertEquals(0, fm.getRecallScore(), DELTA);
    Assertions.assertEquals(0, fm.getPrecisionScore(), DELTA);
  }

  @Test
  public void testPerfect() {
    FMeasure fm = new FMeasure();
    fm.updateScores(gold, gold);
    Assertions.assertEquals(1, fm.getFMeasure(), DELTA);
    Assertions.assertEquals(1, fm.getRecallScore(), DELTA);
    Assertions.assertEquals(1, fm.getPrecisionScore(), DELTA);
  }

  @Test
  public void testMerge() {
    FMeasure fm = new FMeasure();
    fm.updateScores(gold, predicted);
    fm.updateScores(goldToMerge, predictedToMerge);

    FMeasure fmMerge = new FMeasure();
    fmMerge.updateScores(gold, predicted);
    FMeasure toMerge = new FMeasure();
    toMerge.updateScores(goldToMerge, predictedToMerge);
    fmMerge.mergeInto(toMerge);

    double selected1 = predicted.length;
    double target1 = gold.length;
    double tp1 = FMeasure.countTruePositives(gold, predicted);

    double selected2 = predictedToMerge.length;
    double target2 = goldToMerge.length;
    double tp2 = FMeasure.countTruePositives(goldToMerge, predictedToMerge);


    Assertions.assertEquals((tp1 + tp2) / (target1 + target2), fm.getRecallScore(), DELTA);
    Assertions.assertEquals((tp1 + tp2) / (selected1 + selected2), fm.getPrecisionScore(), DELTA);

    Assertions.assertEquals(fm.getRecallScore(), fmMerge.getRecallScore(), DELTA);
    Assertions.assertEquals(fm.getPrecisionScore(), fmMerge.getPrecisionScore(), DELTA);
  }
}
