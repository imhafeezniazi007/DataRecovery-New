package com.example.datarecovery.views.adapters;

public class GridItem extends AbstractItem {

    public GridItem(String label) {
        super(label);
    }

    @Override
    public int getType() {
        return GRID_TYPE;
    }

}
