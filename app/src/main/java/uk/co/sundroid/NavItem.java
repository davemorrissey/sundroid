package uk.co.sundroid;

public class NavItem {

    public enum NavItemLocation {
        MENU,
        HEADER,
        HEADER_IF_ROOM
    }

    private String title;
    private int action;
    private int icon;
    private NavItemLocation location;

    public NavItem(String title, int icon, NavItemLocation location, int action) {
        this.title = title;
        this.icon = icon;
        this.location = location;
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public int getIcon() {
        return icon;
    }

    public NavItemLocation getLocation() {
        return location;
    }

    public String getTitle() {
        return title;
    }

}
