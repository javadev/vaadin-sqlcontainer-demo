package com.vaadin.data.util.sqlcontainer.demo.addressbook.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SearchFilter implements Serializable {

    private final String term;
    private final Object propertyId;
    private String propertyIdDisplayName;
    private String termDisplayName;
    private String searchName;

    public SearchFilter(Object propertyId, String searchTerm, String name) {
        this.propertyId = propertyId;
        term = searchTerm;
        searchName = name;
    }

    public SearchFilter(Object propertyId, String searchTerm, String name,
            String propertyIdDisplayName, String termDisplayName) {
        this(propertyId, searchTerm, name);
        setPropertyIdDisplayName(propertyIdDisplayName);
        setTermDisplayName(termDisplayName);
    }

    public String getTerm() {
        return term;
    }

    public Object getPropertyId() {
        return propertyId;
    }

    public String getSearchName() {
        return searchName;
    }

    @Override
    public String toString() {
        return getSearchName();
    }

    public String getPropertyIdDisplayName() {
        return propertyIdDisplayName;
    }

    public void setPropertyIdDisplayName(String propertyIdDisplayName) {
        this.propertyIdDisplayName = propertyIdDisplayName;
    }

    public String getTermDisplayName() {
        return termDisplayName;
    }

    public void setTermDisplayName(String termDisplayName) {
        this.termDisplayName = termDisplayName;
    }

}
