package com.aaturenko.treeview;

import java.util.ArrayList;

public class ListItem implements Item {

    private String title;
    private ArrayList<Item> childs;

    public ListItem (String title) {
        this.title = title;
        childs = new ArrayList<>();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public ArrayList<Item> getChilds() {
        return childs;
    }

    @Override
    public int getIconResource() {
        if (childs.size() > 0)
            return R.drawable.folder_32;
        return 0;
    }

    public void addChild (Item item) {
        childs.add(item);
    }

}