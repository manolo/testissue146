package julien.test.testissue146.client;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import java.util.ArrayList;
import java.util.List;

/**
 * Example code for a GwtQuery application
 */
public class TestIssue146 implements EntryPoint {

    public static final String CELL_SELECTOR = "[__gwt_cell]";

    public void onModuleLoad() {
        long startTime, elapsedTime;
        GQuery m = $("select").children();

        startTime = System.currentTimeMillis();
        m.filterDefault("option");
        elapsedTime = System.currentTimeMillis() - startTime;
        $("#gquerytimedefault").text(elapsedTime + "ms");

        startTime = System.currentTimeMillis();
        m.filterOld("option");
        elapsedTime = System.currentTimeMillis() - startTime;
        $("#gquerytimeold").text(elapsedTime + "ms");

        startTime = System.currentTimeMillis();
        m.filterNew(false, "option");
        elapsedTime = System.currentTimeMillis() - startTime;
        $("#gquerytimenewfalse").text(elapsedTime + "ms");

        startTime = System.currentTimeMillis();
        m.filterNew(true, "option");
        elapsedTime = System.currentTimeMillis() - startTime;
        $("#gquerytimenewtrue").text(elapsedTime + "ms");

        startTime = System.currentTimeMillis();
        m.filterPredicate("[value]");
        elapsedTime = System.currentTimeMillis() - startTime;
        $("#gquerytimepredicate").text(elapsedTime + "ms");

        startTime = System.currentTimeMillis();
        m.filterSizzle("option");
        elapsedTime = System.currentTimeMillis() - startTime;
        $("#gquerytimesizzle").text(elapsedTime + "ms");

        // live events performance
        final Label breadcrumb = new Label();
        final ListBox algorithm = new ListBox();
        for (GQuery.FilterStrategy filterStrategy : GQuery.FilterStrategy.values()) {
            algorithm.addItem(filterStrategy.name());
        }
        algorithm.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                String value = algorithm.getSelectedValue();
                GQuery.filterStrategy = value == null || value.isEmpty()
                        ? GQuery.FilterStrategy.DEFAULT
                        : GQuery.FilterStrategy.valueOf(value);
                GQuery node = $(CELL_SELECTOR).first();
                StringBuilder message = new StringBuilder();
                long total = 0;
                while (!node.is("body")) {
                    long startTime = System.currentTimeMillis();
                    node.is(CELL_SELECTOR);
                    long elapsed = System.currentTimeMillis() - startTime;
                    total += elapsed;
                    message.append(node.get(0).getTagName()).append(" ").append(elapsed).append("ms > ");
                    node = node.parent();
                }
                breadcrumb.setText(message.append(" total ").append(total).append("ms").toString());
            }
        });

        $(CELL_SELECTOR).live("mouseenter", new Function() {
            @Override
            public void f(final Element e) {
                $(e).css("color", "red");
            }
        });
        $(CELL_SELECTOR).live("mouseleave", new Function() {
            @Override
            public void f(final Element e) {
                $(e).css("color", null);
            }
        });

        final AbstractCellTable<String> bigTable = new CellTable<String>(Integer.MAX_VALUE);
        final TextColumn<String> col = new TextColumn<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        final IntegerBox cols = new IntegerBox();
        cols.setTitle("cols");
        cols.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                while (bigTable.getColumnCount() > 0) bigTable.removeColumn(0);
                for (int i = 0; i < event.getValue(); i++) {
                    bigTable.addColumn(col);
                }
            }
        });
        cols.setValue(10, true);

        IntegerBox rows = new IntegerBox();
        rows.setTitle("rows");
        rows.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                List<String> listData = new ArrayList<String>(event.getValue());
                for (int i = 0; i < event.getValue(); i++) {
                    listData.add("#" + (int) (Math.random() * 1000));
                }
                bigTable.setRowData(listData);
            }
        });
        rows.setValue(1000, true);

        ScrollPanel vScroll = new ScrollPanel(bigTable);
        vScroll.setWidth("400px");
        vScroll.setHeight("300px");

        FlowPanel widgets = new FlowPanel();
        widgets.add(new InlineLabel("algorithm: "));
        widgets.add(algorithm);
        widgets.add(new InlineLabel(" cols: "));
        widgets.add(cols);
        widgets.add(new InlineLabel(" rows: "));
        widgets.add(rows);
        widgets.add(breadcrumb);
        widgets.add(vScroll);

        RootPanel.get("hovertable").add(widgets);
    }

}
