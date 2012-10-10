package com.vaadin.data.util.sqlcontainer.demo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import com.vaadin.Application;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.connection.SimpleJDBCConnectionPool;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class SQLContainerDemo extends Application implements Serializable {

    private static final String[] VISIBLE_COLS = { "FIRSTNAME", "LASTNAME",
            "COMPANY" };

    private SimpleJDBCConnectionPool connectionPool = null;
    private SQLContainer container = null;
    private Window mainWindow;

    private Table contactList = new Table();
    private VerticalLayout editorLayout = new VerticalLayout();
    private Form contactEditor = new Form();
    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();
    private Button contactRemovalButton;

    @Override
    public void init() {
        mainWindow = new Window("SQLContainer test");
        setMainWindow(mainWindow);
        initConnectionPool();
        initDatabase();
        initContainer();
        fillContainer(container);

        initLayout();
        initContactAddRemoveButtons();
        initAddressList();
        initFilteringControls();
        initFieldFactory();
    }

    private void initFieldFactory() {
        contactEditor.setFormFieldFactory(new DefaultFieldFactory() {
            @Override
            public Field createField(Item item, Object propertyId,
                    Component uiContext) {
                Field f = super.createField(item, propertyId, uiContext);
                if (f instanceof TextField) {
                    ((TextField) f).setNullRepresentation("");
                }
                if (propertyId.equals("FIRSTNAME")
                        || propertyId.equals("LASTNAME")) {
                    f.setRequired(true);
                }
                return f;
            }
        });
    }

    private void initLayout() {
        SplitPanel splitPanel = new SplitPanel(
                SplitPanel.ORIENTATION_HORIZONTAL);
        setMainWindow(new Window("Address Book", splitPanel));
        VerticalLayout left = new VerticalLayout();
        left.setSizeFull();
        left.addComponent(contactList);
        contactList.setSizeFull();
        left.setExpandRatio(contactList, 1);
        splitPanel.addComponent(left);
        splitPanel.addComponent(editorLayout);
        contactEditor.setSizeFull();
        contactEditor.getLayout().setMargin(true);
        contactEditor.setImmediate(false);
        contactEditor.setValidationVisible(false);
        contactEditor.setValidationVisibleOnCommit(false);
        editorLayout.addComponent(contactEditor);
        editorLayout.addComponent(new Button("Save",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        contactEditor.commit();
                        try {
                            container.commit();
                            editorLayout.setVisible(false);
                        } catch (SQLException e) {
                            showError("Error when saving record!");
                            e.printStackTrace();
                        }
                    }
                }));
        editorLayout.setVisible(false);
        bottomLeftCorner.setWidth("100%");
        left.addComponent(bottomLeftCorner);
    }

    private void initContactAddRemoveButtons() {
        // New item button
        bottomLeftCorner.addComponent(new Button("+",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        Object id = contactList.addItem();
                        contactList.setValue(id);
                    }
                }));

        // Remove item button
        contactRemovalButton = new Button("-", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                contactList.removeItem(contactList.getValue());
                try {
                    container.commit();
                } catch (SQLException e) {
                    showError("Error when removing record!");
                    e.printStackTrace();
                }
                contactList.select(null);
            }
        });
        contactRemovalButton.setVisible(false);
        bottomLeftCorner.addComponent(contactRemovalButton);
    }

    private void initAddressList() {
        contactList.setContainerDataSource(container);
        contactList.setVisibleColumns(VISIBLE_COLS);
        contactList.setSelectable(true);
        contactList.setImmediate(true);
        contactList.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                Object id = contactList.getValue();
                if (id instanceof Integer) {
                    try {
                        container.rollback();
                    } catch (UnsupportedOperationException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                contactEditor.setItemDataSource(id == null ? null : contactList
                        .getItem(id));
                editorLayout.setVisible(id != null);
                contactRemovalButton.setVisible(id != null);
            }
        });
    }

    private void initFilteringControls() {
        for (final String pn : VISIBLE_COLS) {
            final TextField sf = new TextField();
            bottomLeftCorner.addComponent(sf);
            sf.setWidth("100%");
            sf.setInputPrompt(pn);
            sf.setImmediate(true);
            bottomLeftCorner.setExpandRatio(sf, 1);
            sf.addListener(new Property.ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    container.removeContainerFilters(pn);
                    if (sf.toString().length() > 0 && !pn.equals(sf.toString())) {
                        container.addContainerFilter(pn, sf.toString(), true,
                                false);
                    }
                    getMainWindow().showNotification(
                            "" + container.size() + " matches found");
                }
            });
        }
    }

    private void initConnectionPool() {
        try {
            connectionPool = new SimpleJDBCConnectionPool(
                    "org.hsqldb.jdbc.JDBCDriver",
                    "jdbc:hsqldb:mem:sqlcontainer", "SA", "", 2, 5);
        } catch (SQLException e) {
            showError("Couldn't create the connection pool!");
            e.printStackTrace();
        }
    }

    public void showError(String errorString) {
        mainWindow.showNotification(errorString,
                Notification.TYPE_ERROR_MESSAGE);
    }

    private void initDatabase() {
        try {
            Connection conn = connectionPool.reserveConnection();
            Statement statement = conn.createStatement();
            try {
                statement.executeQuery("SELECT * FROM PEOPLE");
            } catch (SQLException e) {
                // Failed, which means that we should init the database
                statement
                        .execute("CREATE TABLE PEOPLE "
                                + "(ID INTEGER GENERATED ALWAYS AS IDENTITY, "
                                + "FIRSTNAME VARCHAR(32), LASTNAME VARCHAR(32), "
                                + "COMPANY VARCHAR(32), MOBILE VARCHAR(20), WORKPHONE VARCHAR(20), "
                                + "HOMEPHONE VARCHAR(20), WORKEMAIL VARCHAR(128), HOMEEMAIL VARCHAR(128), "
                                + "STREET VARCHAR(32), ZIP VARCHAR(16), CITY VARCHAR(32), STATE VARCHAR(2), "
                                + "COUNTRY VARCHAR(32), PRIMARY KEY(ID))");
            }
            statement.close();
            conn.commit();
            connectionPool.releaseConnection(conn);
        } catch (SQLException e) {
            showError("Could not create people table!");
            e.printStackTrace();
        }
    }

    private void initContainer() {
        try {
            FreeformQuery query = new FreeformQuery("SELECT * FROM PEOPLE",
                    Arrays.asList("ID"), connectionPool);
            query.setDelegate(new DemoFreeformQueryDelegate());
            container = new SQLContainer(query);
        } catch (SQLException e) {
            showError("Could not create an instance of SQLContainer!");
            e.printStackTrace();
        }
    }

    private void fillContainer(SQLContainer container) {
        if (container.size() == 0) {
            String[] fnames = { "Peter", "Alice", "Joshua", "Mike", "Olivia",
                    "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
                    "Lisa", "Marge" };
            String[] lnames = { "Smith", "Gordon", "Simpson", "Brown",
                    "Clavel", "Simons", "Verne", "Scott", "Allison", "Gates",
                    "Rowling", "Barks", "Ross", "Schneider", "Tate" };

            for (int i = 0; i < 1000; i++) {
                Object id = container.addItem();
                container.getContainerProperty(id, "FIRSTNAME").setValue(
                        fnames[(int) (fnames.length * Math.random())]);
                container.getContainerProperty(id, "LASTNAME").setValue(
                        lnames[(int) (lnames.length * Math.random())]);
            }
            try {
                container.commit();
            } catch (SQLException e) {
                showError("Could not store items!");
                e.printStackTrace();
            }
        }
    }

}
