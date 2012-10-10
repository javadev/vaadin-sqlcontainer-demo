package com.vaadin.data.util.sqlcontainer.demo.addressbook.ui;

import com.vaadin.data.util.sqlcontainer.demo.addressbook.AddressBookApplication;
import com.vaadin.data.util.sqlcontainer.demo.addressbook.data.DatabaseHelper;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class PersonList extends Table {
    public PersonList(final AddressBookApplication app) {
        setSizeFull();

        /*
         * Get the SQLContainer containing the persons in the address book and
         * set it as the data source of this table.
         */
        setContainerDataSource(app.getDbHelp().getPersonContainer());

        /*
         * Remove container filters and set container filtering mode to
         * inclusive. (These are default values but set here just in case)
         */
        app.getDbHelp().getPersonContainer().removeAllContainerFilters();

        setColumnCollapsingAllowed(true);
        setColumnReorderingAllowed(true);

        /*
         * Make table selectable, react immediately to user events, and pass
         * events to the controller (our main application).
         */
        setSelectable(true);
        setImmediate(true);
        addListener((ValueChangeListener) app);
        /* We don't want to allow users to de-select a row */
        setNullSelectionAllowed(false);

        /* Customize email column to have mailto: links using column generator */
        addGeneratedColumn("EMAIL", new ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
                if (getItem(itemId).getItemProperty("EMAIL").getValue() != null) {
                    Link l = new Link();
                    l.setResource(new ExternalResource("mailto:"
                            + getItem(itemId).getItemProperty("EMAIL")
                                    .getValue()));
                    l.setCaption(getItem(itemId).getItemProperty("EMAIL")
                            .getValue().toString());
                    return l;
                }
                return null;
            }
        });

        /*
         * Create a cityName column that fetches the city name from another
         * SQLContainer through the DatabaseHelper.
         */
        addGeneratedColumn("CITYID", new ColumnGenerator() {
            public Component generateCell(Table source, Object itemId,
                    Object columnId) {
                if (getItem(itemId).getItemProperty("CITYID").getValue() != null) {
                    Label l = new Label();
                    int cityId = (Integer) getItem(itemId).getItemProperty(
                            "CITYID").getValue();
                    l.setValue(app.getDbHelp().getCityName(cityId));
                    l.setSizeUndefined();
                    return l;
                }
                return null;
            }
        });

        /* Set visible columns, their ordering and their headers. */
        setVisibleColumns(DatabaseHelper.NATURAL_COL_ORDER);
        setColumnHeaders(DatabaseHelper.COL_HEADERS_ENGLISH);
    }

    /**
     * Checks that selection is not null and that the selection actually exists
     * in the container. If no valid selection is made, the first item will be
     * selected. Finally, the selection will be scrolled into view.
     */
    public void fixVisibleAndSelectedItem() {
        if ((getValue() == null || !containsId(getValue())) && size() > 0) {
            Object itemId = getItemIds().iterator().next();
            select(itemId);
            setCurrentPageFirstItemId(itemId);
        } else {
            setCurrentPageFirstItemId(getValue());
        }
    }
}