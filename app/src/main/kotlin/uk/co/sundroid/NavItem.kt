package uk.co.sundroid

class NavItem(val title: String, val icon: Int, val location: NavItemLocation, val action: Int) {

    enum class NavItemLocation {
        MENU,
        HEADER,
        HEADER_IF_ROOM
    }

}
