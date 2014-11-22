package julien.test.testissue146.client;
import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Predicate;
import com.google.gwt.query.client.impl.SelectorEngineSizzle;
import com.google.gwt.query.client.js.JsNodeArray;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;

/**
 * Example code for a GwtQuery application
 */
public class TestIssue146 implements EntryPoint {

  public void onModuleLoad() {
    MGQuery m = new MGQuery($("select").children());

    long startTime = System.currentTimeMillis();
    m.filterOld("option");
    long elapsedTime = System.currentTimeMillis() - startTime;
    $("#gquerytimeold").text(elapsedTime + "ms");

    startTime = System.currentTimeMillis();
    m.filterNew("option");
    elapsedTime = System.currentTimeMillis() - startTime;
    $("#gquerytimenew").text(elapsedTime + "ms");

    startTime = System.currentTimeMillis();
    m.filterPredicate("[value]");
    elapsedTime = System.currentTimeMillis() - startTime;
    $("#gquerytimepredicate").text(elapsedTime + "ms");

    startTime = System.currentTimeMillis();
    m.filterSizzle("option");
    elapsedTime = System.currentTimeMillis() - startTime;
    $("#gquerytimesizzle").text(elapsedTime + "ms");
  }


  private class MGQuery extends GQuery {
    protected MGQuery(GQuery gq) {
      super(gq);
    }

    public GQuery filterNew(String selector) {
      boolean considerDetached = false;
      if (selector.isEmpty()) {
        return this;
      }
      GQuery all = $(selector);
      Element ghostParent = null;
      if (considerDetached) {
        ghostParent = Document.get().createDivElement();
        for (Element e : elements()) {
          if (JsUtils.isDetached(e)) {
            ghostParent.appendChild(e);
          }
        }
        all = all.add($(selector, ghostParent));
      }
      JsNodeArray array = JsNodeArray.create();
      for (Element e : elements()) {
        for (Element l : all.elements()) {
          if (e == l) {
            array.addNode(e);
            break;
          }
        }
      }
      if (ghostParent != null) {
        $(ghostParent).html(null);
      }
      return pushStack(array, "filter", selector);
    }

    public GQuery filterOld(String... filters) {
      if (filters.length == 0 || filters[0] == null) {
        return this;
      }

      JsNodeArray array = JsNodeArray.create();

      for (String f : filters) {
        for (Element e : elements()) {
          boolean ghostParent = false;
          if (e == window || e.getNodeName() == null) {
            continue;
          }
          if (e.getParentNode() == null) {
            DOM.createDiv().appendChild(e);
            ghostParent = true;
          }

          for (Element c : $(f, e.getParentNode()).elements()) {
            if (c == e) {
              array.addNode(c);
              break;
            }
          }

          if (ghostParent) {
            e.removeFromParent();
          }
        }
      }
      return pushStack(unique(array), "filter", filters[0]);
    }

    public GQuery filterPredicate(String filter) {
      return filter(asPredicateFilter(filter));
    }

    private RegExp simpleAttrFilter = RegExp.compile("\\[([\\w-]+)(\\^|\\$|\\*|\\||~|!)?=?[\"']?([\\w\\u00C0-\\uFFFF\\s\\-_\\.]+)?[\"']?\\]");

    public Predicate asPredicateFilter(String selector) {
      final MatchResult simpleAttrMatch = simpleAttrFilter.exec(selector);
      if (simpleAttrMatch == null) return null; // non simple attr filter
      final String attrName = simpleAttrMatch.getGroup(1);
      final String matchOp = simpleAttrMatch.getGroup(2);
      final String matchVal = simpleAttrMatch.getGroup(3);
      final char op = matchOp == null || matchOp.length() == 0 ? '0' : matchOp.charAt(0);
      if ("0=^$*|~!".indexOf(op) == -1) return null; // unsupported or illegal operator
      return new Predicate() {
        @Override
        public boolean f(Element e, int index) {
          switch (op) {
            case '0': return e.hasAttribute(attrName);
            case '=': return e.getAttribute(attrName).equals(matchVal);
            case '^': return e.getAttribute(attrName).startsWith(matchVal);
            case '$': return e.getAttribute(attrName).endsWith(matchVal);
            case '*': return e.getAttribute(attrName).contains(matchVal);
            case '|': return (e.getAttribute(attrName) + "-").startsWith(matchVal + "-");
            case '~': return (" " + e.getAttribute(attrName) + " ").contains(" " + matchVal + " ");
            case '!': return !e.getAttribute(attrName).equals(matchVal);
            default: return false;
          }
        }
      };
    }

    public GQuery filterSizzle(String selector) {
      JsArray<Element> seed = JsArray.createArray(elements().length).cast();
      for (Element element : elements()) seed.push(element);
      JsNodeArray array = matches(selector, seed).cast();
      return pushStack(array, "filter", selector);
    }

  }

  static {
    SelectorEngineSizzle.initialize();
  }

  public static native JsArray<Element> matches(String selector, JsArray<Element> seed) /*-{
      return $wnd.GQS(selector, null, null, seed);
  }-*/;

}
