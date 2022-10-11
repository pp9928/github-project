// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.thoughtworks.selenium.condition;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for Condition class.
 */
public class ConditionTest {

  private static final ConditionRunner conditionRunner =
    new JUnitConditionRunner(null, 1, 100);

  @Test
  void testAppendsInfoToFailureMessage() {
    try {
      conditionRunner.waitFor("this condition should always fail", new AlwaysFalseCondition());
      fail("the condition should have failed");
    } catch (AssertionError expected) {
      assertEquals("Condition \"Sky should be blue\" failed to become true within 100 msec; " +
        "this condition should always fail; [sky is in fact pink]", expected.getMessage());
    }
  }

  @Test
  void testNotCanInvertFailingSituationQuickly() {
    Condition alwaysFalse = new AlwaysFalseCondition();
    long start = System.currentTimeMillis();
    final StringBuilder sb = new StringBuilder();
    alwaysFalse.isTrue(new ConditionRunner.Context() {
      @Override
      public ConditionRunner getConditionRunner() {
        return null;
      }

      @Override
      public Selenium getSelenium() {
        return null;
      }

      @Override
      public void info(String string) {
        sb.append(string);
      }

      @Override
      public long elapsed() {
        return 0;
      }
    });
    new JUnitConditionRunner(null, 0, 1000, 100000).waitFor(new Not(alwaysFalse));
    assertTrue(System.currentTimeMillis() - start < 100);
    assertEquals("sky is in fact pink", sb.toString());
  }

  @Test
  void testNotCanNegatePassingSituationAfterTimeout() {
    Condition alwaysTrue = new AlwaysTrueCondition();
    long start = System.currentTimeMillis();
    try {
      new JUnitConditionRunner(null, 0, 1000, 1000).waitFor(new Not(alwaysTrue));
      fail("the condition should have failed");
    } catch (AssertionError expected) {
      long l = System.currentTimeMillis() - start;
      assertTrue(l >= 1000);
      assertEquals(
        "Condition \"NOT of (Condition \"Sky should be blue\")\" failed to become true within 1000 msec; [yes it is really is blue]",
        expected.getMessage());
    }
  }

  @Test
  void testCanTurnTrueBeforeTimeout() {
    long start = System.currentTimeMillis();
    final int[] time = new int[1];
    JUnitConditionRunner conditionRunner1 = new JUnitConditionRunner(null, 0, 100, 2000);
    conditionRunner1.waitFor(new Condition() {
      @Override
      public boolean isTrue(ConditionRunner.Context runner) {
        return time[0]++ >= 12;
      }
    });
    long l = System.currentTimeMillis() - start;
    assertTrue(l >= 1200); // is waiting at least 12 * 100 milliseconds
    assertTrue(l < 2000); // but timing out before 2000 milliseconds
  }

  @Test
  void testCannotTurnTrueAfterTimeout() {
    long start = System.currentTimeMillis();
    final int[] time = new int[1];
    JUnitConditionRunner conditionRunner1 = new JUnitConditionRunner(null, 0, 100, 500);
    try {
      conditionRunner1.waitFor(new Condition() {
        @Override
        public boolean isTrue(ConditionRunner.Context runner) {
          return time[0]++ == 52;
        }
      });
      fail("the condition should have failed");
    } catch (AssertionError expected) {
      long l = System.currentTimeMillis() - start;
      assertTrue(l >= 500); // timed out after 5000 milliseconds
    }

  }

  /**
   * Why? Well because for some technologies/setups, any Selenium operation may result in a 'body
   * not loaded' for the first few loops See <a href="http://jira.openqa.org/browse/SRC-302"></a>
   */
  @Test
  void testCanLateNotifyOfSeleniumExceptionAfterTimeout() {
    long start = System.currentTimeMillis();
    JUnitConditionRunner conditionRunner1 = new JUnitConditionRunner(null, 0, 100, 500);
    try {
      conditionRunner1.waitFor(new Condition() {
        @Override
        public boolean isTrue(ConditionRunner.Context runner) {
          throw new SeleniumException("Yeehaa!");
        }
      });
      fail("the condition should have failed");
    } catch (AssertionError expected) {
      assertEquals(
        "SeleniumException while waiting for 'Condition \"null\"' (otherwise timed out); cause: Yeehaa!",
        expected.getMessage());
      long l = System.currentTimeMillis() - start;
      assertTrue(l >= 500); // timed out after 500 milliseconds
    }

  }

  @Test
  void testRuntimeExceptionInsideConditionIsWrapped() {
    final RuntimeException thrownException = new RuntimeException("ooops");
    Condition condition = new Condition("foo") {
      @Override
      public boolean isTrue(ConditionRunner.Context runner) {
        throw thrownException;
      }
    };
    try {
      conditionRunner.waitFor(condition);
      fail("should have thrown a exception");
    } catch (AssertionError expected) {
      assertEquals("Exception while waiting for 'Condition \"foo\"'; cause: ooops",
        expected.getMessage());
    }
  }

  @Test
  void testAssertionFailureInsideConditionIsNotWrapped() {
    Condition condition = new Condition() {
      @Override
      public boolean isTrue(ConditionRunner.Context runner) {
        fail("OMG");
        return false;
      }
    };
    try {
      conditionRunner.waitFor(condition);
      fail("should have thrown an assertion failed error");
    } catch (AssertionError expected) {
      assertTrue(expected.getMessage().contains("OMG"));
    }
  }

  @Test
  void testMessageWithArgs() {
    final RuntimeException thrownException = new RuntimeException();
    Condition condition = new Condition("foo %s baz", "bar") {
      @Override
      public boolean isTrue(ConditionRunner.Context runner) {
        throw thrownException;
      }
    };
    try {
      conditionRunner.waitFor(condition);
      fail("should have thrown a runtime exception");
    } catch (AssertionError expected) {
      assertEquals("Exception while waiting for 'Condition \"foo bar baz\"'",
        expected.getMessage());
    }
  }

  private static class AlwaysFalseCondition extends Condition {
    public AlwaysFalseCondition() {
      super("Sky should be blue");
    }

    @Override
    public boolean isTrue(ConditionRunner.Context context) {
      context.info("sky is in fact pink");
      return false;
    }
  }

  private static class AlwaysTrueCondition extends Condition {
    public AlwaysTrueCondition() {
      super("Sky should be blue");
    }

    @Override
    public boolean isTrue(ConditionRunner.Context context) {
      context.info("yes it is really is blue");
      return true;
    }
  }
}
