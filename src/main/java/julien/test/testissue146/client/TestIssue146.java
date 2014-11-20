package julien.test.testissue146.client;
import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Predicate;
import com.google.gwt.query.client.impl.SelectorEngineSizzle;
import com.google.gwt.query.client.js.JsNodeArray;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;

import java.util.ArrayList;
import java.util.List;

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
    /**
     * Removes all elements from the set of matched elements that do not pass the specified css
     * expression. This method is used to narrow down the results of a search.
     */
    public GQuery filterNew(String selector) {
      if (selector.isEmpty()) {
        return this;
      }
      Element ghostParent = null;
      ArrayList<Element> parents = new ArrayList<Element>();
      List<Element> elmList = new ArrayList<Element>();
      for (Element e : elements()) {
        if (e == window || e.getNodeName() == null || "html".equalsIgnoreCase(e.getNodeName())) {
          continue;
        }
        elmList.add(e);
        Element p = e.getParentElement();
        if (p == null) {
          if (ghostParent == null) {
            ghostParent = Document.get().createDivElement();
            parents.add(ghostParent);
          }
          p = ghostParent;
          p.appendChild(e);
        } else if (!parents.contains(p)) {
          parents.add(p);
        }
      }
      JsNodeArray array = JsNodeArray.create();
      for (Element e : parents) {
        NodeList<Element> n  = engine.select(selector, e);
        for (int i = 0, l = n.getLength(); i < l; i++) {
          Element el = n.getItem(i);
          if (elmList.contains(el)) {
            elmList.remove(el);
            array.addNode(el);
          }
        }
      }
      if (ghostParent != null) {
        $(ghostParent).empty();
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
