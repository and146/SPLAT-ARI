/*
 * $Id: TestSuite.java,v 1.7 2001/07/22 22:02:09 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.jester;


/**
 * The abstract superclass of test suites. A test suite
 * runs tests on one or a set of objects. Test suites
 * generally do not follow the class hierarchy in any way,
 * but inherit directly from this class. Here is a brief
 * description -- for more details and tutorial examples,
 * see the package documentation.
 *
 * In order to make it possible for test suites to be run
 * on objects of different classes -- such as subclasses
 * or objects that implement interfaces -- all object creation
 * should be parameterized by providing <i>factory</i> objects.
 * Factory object can be a <i>unit factory</i>, in which case they
 * provide (by convention) one version of the method <b>create</b>
 * for each constructor, or a <i>collaboration factory</i>, in
 * which case each role played in the collaboration has
 * one or more version of a <b>createRole</b> method.
 *
 * In the case of unit factories, the factory inheritance
 * hierarchy mimics the class hierarchy. In the case of
 * collaboration factories, factories that create more specific
 * classes tend should inherit from the more general factories,
 * but the inheritance is less structured because there may
 * be multiple subclasses.
 * 
 * In general, a test suite contains:
 *
 * <ul>
 * <li>A single constructor that takes a test harness and one
 * or factories as arguments
 * <li>Implementations of the runSuite() and run() methods.
 * <li>Subclasses of TestCase for use in the tests (optional)
 * <li>One or more Factory classes (*)
 * <li>A main() method for running just this test suite (*)
 * <li>Public methods that implement tests, with names starting
 * with "test."
 * </ul>
 *
 * The steps marked (*) are omitted if the test suite is written
 * for an abstract class or an interface.
 * 
 * The classes diva.jester.demo.test.UnitFoo and
 * diva.jester.demo.test.UnitBar
 * are examples of how to write unit tests, and serve as examples of
 * the expected formatting of the source.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.7 $
 */
public abstract class TestSuite {

  /**
   * The test harness.
   */
  private TestHarness _testHarness;

  /**
   * The factory.
   */
  private Object _factory;

  /**
   * Initialize the test harness and run all the tests that can be
   * run by this class. This final method calls runAll(), in between
   * calling the test harness to tell it that the test suite is
   * starting and stopping.
   */
  public final void run () {
    _testHarness.readyTestSuite(this);
    runAll();
    _testHarness.doneTestSuite();
  }

  /**
   * Run all the tests that can be run by this class. By default, this
   * method calls runSuite(), and needs to be overridden if additional
   * test suites are to be created -- for example, to exercise methods
   * defined in superclasses or implemented interfaces. In the case
   * of unit tests of classes that have superclasses or implemented
   * interfaces, the overriding method should:
   * <ul>
   * <li>Create an instance of the unit test suite of each superclass 
   * and implemented interface, and call its runSuite() method.
   * <li>Call its own runSuite() method.
   * </ul>
   *
   * (Note that for this to work properly, runAll() must not call
   * runAll() of its superclass, but must enumerate each of the test
   * suites to be run on this object. This allows subclasses to
   * exclude certain tests.)
   */
  public void runAll () {
    runSuite();
  }

  /**
   * Run the tests defined by this test suite. This is an abstract
   * method, and must be overridden to execute the test methods.
   */
  public abstract void runSuite ();

  /**
   * Run a single test case by passing it to the harness used by this
   * test suite. This is a convenient access method, mainly intended
   * for use by subclasses, and it equivalent to
   * <pre>
   *   getTestHarness().runTestCase(testCase)
   * </pre>
   */
  public void runTestCase (TestCase testCase) {
    _testHarness.runTestCase(testCase);
  }

  /**
   * Set the test harness used by this test suite.
   */
  public void setTestHarness (TestHarness h) {
      _testHarness = h;
  }

  /**
   * Get the test harness used by this test suite.
   */
  public TestHarness getTestHarness () {
      return _testHarness;
  }

  /**
   * Set the factory used by this test suite. This can be
   * any object, but the test output will make most sense if
   * it is an object used to produce the object or objects
   * being tested, and that has a descriptive toString()
   * method.
   */
  public void setFactory (Object f) {
      _factory = f;
  }

  /**
   * Get the factory used by this test suite.
   */
  public Object getFactory () {
      return _factory;
  }
}


