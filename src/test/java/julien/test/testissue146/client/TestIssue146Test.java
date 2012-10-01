package julien.test.testissue146.client;

import static com.google.gwt.query.client.GQuery.*;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.plugins.Effects.Speed;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
/**
 * Test class for TestIssue146 entry-point
 */
public class TestIssue146Test extends GWTTestCase {

  public String getModuleName() {
    return "julien.test.testissue146.TestIssue146";
  }

  private double fontSize(GQuery g) {
    return $(g).cur("fontSize", true);
  }

  public void testOnModuleLoad() {
    
    // Create a container in the document
    final GQuery g =  $("<div></div>").appendTo(document);

    // run onModuleLoad
    TestIssue146 a = new TestIssue146();
    a.onModuleLoad();

    // delay the test
    delayTestFinish(Speed.DEFAULT * 5);

    // trigger mouse over event
    final double size1 = fontSize(g);
    g.trigger(Event.ONMOUSEOVER);
    new Timer() {
      public void run() {
        // assert that the font size increases
        assertTrue(fontSize(g) > size1);
        
        // trigger mouse out event
        final double size2 = fontSize(g);
        g.trigger(Event.ONMOUSEOUT);
        new Timer() {
          public void run() {
            // assert that the font size decreases
            assertTrue(fontSize(g) < size2);
            g.remove();
            
            // finish the test
            finishTest();
          }
        }.schedule(Speed.DEFAULT);
      }
    }.schedule(Speed.DEFAULT);
  }

}
