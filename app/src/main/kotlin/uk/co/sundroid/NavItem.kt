package uk.co.sundroid

class NavItem(val title: String, val icon: Int, val location: NavItemLocation, val action: Int) {

    enum class NavItemLocation {
        HEADER,
        HEADER_IF_ROOM
    }

}
