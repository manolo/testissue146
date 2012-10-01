package julien.test.testissue146.client;
import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.core.client.EntryPoint;

/**
 * Example code for a GwtQuery application
 */
public class TestIssue146 implements EntryPoint {

  public void onModuleLoad() {
    
    long startTime = System.currentTimeMillis();
    
    $("select").children().filter("option");
    
    long elapsedTime = System.currentTimeMillis() - startTime;
    $("#gquerytime").text(elapsedTime +"ms");
  }

}
