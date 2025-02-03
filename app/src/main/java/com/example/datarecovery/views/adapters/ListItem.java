package com.example.datarecovery.views.adapters;

public class ListItem extends AbstractItem {

    public ListItem(String label) {
        super(label);
    }

    @Override
    public int getType() {
        return LIST_TYPE;
    }

}
