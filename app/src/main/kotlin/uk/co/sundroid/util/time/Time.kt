package uk.co.sundroid.util.time

class Time(val time: String, val marker: String) {

    override fun toString(): String {
        return time + marker
    }

}