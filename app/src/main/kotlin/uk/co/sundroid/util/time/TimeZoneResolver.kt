package uk.co.sundroid.util.time

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.TimeZone

import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.*
import uk.co.sundroid.util.location.LatitudeLongitude


object TimeZoneResolver {
    
    /** Map of comma-separated country codes to java timezone IDs for those countries. */
    private val countryCodeMap: HashMap<String, Array<String>>
    
    /** Map of country code : state to java timezone IDs for states where zone is known. */
    private val stateMap: HashMap<String, Array<String>> = HashMap()

    /** Map of java timezone ID to cities list for display. */
    private val timeZoneMap: HashMap<String, String>
    
    fun getAllTimeZones(): ArrayList<TimeZoneDetail> {
        return resolve(timeZoneMap.keys)
    }
    
    /**
     * Attempts to work out candidate time zones for a given place. If the device time zone is specified,
     * this is always used in preference to location. If not or it can't be matched, the country code
     * and state are looked up in config maps to find a list of candidate zones. If this method returns
     * an empty list, the time zone can't be worked out and a list must be presented.
     */
    fun getPossibleTimeZones(
            location: LatitudeLongitude,
            countryCode: String?,
            state: String?): ArrayList<TimeZoneDetail> {

        // Step 2: Look up country code. Countries with state mappings aren't in this map.
        if (isNotEmpty(countryCode)) {
            for ((key, value) in countryCodeMap) {
                if (key.contains(countryCode + ",")) {
                    return resolve(value.toMutableList())
                }
            }
        }
        
        // Step 2.9: Texas exception
        if (countryCode == "US" && state == "Texas") {
            return if (location.longitude.doubleValue < -104.9172) {
                resolve(Collections.singletonList("US/Mountain"))
            } else {
                resolve(Collections.singletonList("US/Central"))
            }
        }
        
        // Step 3: Use state to resolve some ambiguous countries.
        if (isNotEmpty(countryCode) && isNotEmpty(state) && stateMap.containsKey(countryCode + ":" + state)) {
            return resolve(stateMap[countryCode + ":" + state]!!.toMutableList())
        }
        
        // Step 4: Country might be known but state wasn't - if so use the catch-all state mapping.
        if (isNotEmpty(countryCode) && stateMap.containsKey(countryCode + ":*")) {
            return resolve(stateMap[countryCode + ":*"]!!.toMutableList())
        }
        
        // Step 4: Use location to resolve remaining countries (not implemented)
        
        // Step 5: Give up and return no suggestions. Device will use UTC or ask for supported zone.
        return ArrayList()
        
    }
    
    /**
     * Attempt to get a time zone by ID, and optionally default to the device zone if the zone ID is
     * not known to Java. The cities attribute is populated from the stored map when possible.
     * @param id Time zone ID.
     * @return A {@link TimeZoneDetail}. Only the cities attribute may be null.
     */
    fun getTimeZone(id: String?): TimeZoneDetail {
        return getTimeZoneInternal(id) ?: getDefaultTimeZone()
    }

    private fun getTimeZoneInternal(id: String?): TimeZoneDetail? {
        try {
            val timeZone = TimeZone.getTimeZone(id)
            if (timeZone != null && (timeZone.id != "UTC" || id == "UTC")) {
                return TimeZoneDetail(id, timeZoneMap[id], timeZone)
            }
        } catch (e: Exception) {
            // Zone not known to this device.
        }
        return null
    }

    private fun getDefaultTimeZone(): TimeZoneDetail {
        val defaultTimeZone = TimeZone.getDefault()
        return TimeZoneDetail(defaultTimeZone.id, timeZoneMap[defaultTimeZone.id], defaultTimeZone)
    }
    
    private fun resolve(timeZoneIds: MutableCollection<String>): ArrayList<TimeZoneDetail> {
        return timeZoneIds.mapNotNullTo(ArrayList()) { getTimeZoneInternal(it) }
    }

    init {

        stateMap.apply {
            put("US:Alabama", arrayOf("US/Central"))
            put("US:Alaska", arrayOf("US/Alaska", "America/Adak"))
            put("US:Arizona", arrayOf("US/Arizona"))
            put("US:Arkansas", arrayOf("US/Central"))
            put("US:California", arrayOf("US/Pacific"))
            put("US:Colorado", arrayOf("US/Mountain"))
            put("US:Connecticut", arrayOf("US/Eastern"))
            put("US:Delaware", arrayOf("US/Eastern"))
            put("US:Florida", arrayOf("US/Eastern", "US/Central"))
            put("US:Georgia", arrayOf("US/Eastern"))
            put("US:Hawaii", arrayOf("US/Hawaii"))
            put("US:Idaho", arrayOf("US/Mountain"))
            put("US:Illinois", arrayOf("US/Central"))
            put("US:Indiana", arrayOf("US/Eastern", "US/Central"))
            put("US:Iowa", arrayOf("US/Central"))
            put("US:Kansas", arrayOf("US/Central", "US/Mountain"))
            put("US:Kentucky", arrayOf("US/Eastern", "US/Central"))
            put("US:Louisiana", arrayOf("US/Central"))
            put("US:Maine", arrayOf("US/Eastern"))
            put("US:Maryland", arrayOf("US/Eastern"))
            put("US:Massachusetts", arrayOf("US/Eastern"))
            put("US:Michigan", arrayOf("US/Eastern"))
            put("US:Minnesota", arrayOf("US/Central"))
            put("US:Mississippi", arrayOf("US/Central"))
            put("US:Missouri", arrayOf("US/Central"))
            put("US:Montana", arrayOf("US/Mountain", "US/Pacific"))
            put("US:Nebraska", arrayOf("US/Central", "US/Mountain"))
            put("US:Nevada", arrayOf("US/Pacific"))
            put("US:New Hampshire", arrayOf("US/Eastern"))
            put("US:New Jersey", arrayOf("US/Eastern"))
            put("US:New Mexico", arrayOf("US/Mountain"))
            put("US:New York", arrayOf("US/Eastern"))
            put("US:North Carolina", arrayOf("US/Eastern"))
            put("US:North Dakota", arrayOf("US/Central", "US/Mountain"))
            put("US:Ohio", arrayOf("US/Eastern"))
            put("US:Oklahoma", arrayOf("US/Central"))
            put("US:Oregon", arrayOf("US/Pacific", "US/Mountain"))
            put("US:Pennsylvania", arrayOf("US/Eastern"))
            put("US:Rhode Island", arrayOf("US/Eastern"))
            put("US:South Carolina", arrayOf("US/Eastern"))
            put("US:South Dakota", arrayOf("US/Central", "US/Mountain"))
            put("US:Tennessee", arrayOf("US/Eastern", "US/Central"))
            put("US:Texas", arrayOf("US/Central"))
            put("US:Utah", arrayOf("US/Mountain"))
            put("US:Vermont", arrayOf("US/Eastern"))
            put("US:Virginia", arrayOf("US/Eastern"))
            put("US:Washington", arrayOf("US/Pacific"))
            put("US:West Virginia", arrayOf("US/Eastern"))
            put("US:Wisconsin", arrayOf("US/Central"))
            put("US:Wyoming", arrayOf("US/Mountain"))
            put("US:*", arrayOf("US/Hawaii", "America/Adak", "US/Alaska", "US/Pacific", "US/Arizona", "US/Mountain", "US/Central", "US/Eastern", "US/East-Indiana"))
            put("AU:Western Australia", arrayOf("Australia/Perth"))
            put("AU:South Australia", arrayOf("Australia/Adelaide"))
            put("AU:Northern Territory", arrayOf("Australia/Darwin"))
            put("AU:Queensland", arrayOf("Australia/Brisbane"))
            put("AU:New South Wales", arrayOf("Australia/Sydney"))
            put("AU:Victoria", arrayOf("Australia/Sydney"))
            put("AU:Tasmania", arrayOf("Australia/Hobart"))
            put("AU:*", arrayOf("Australia/Perth", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Australia/Hobart"))
            put("ES:Canary Islands", arrayOf("Europe/London"))
            put("ES:*", arrayOf("Europe/Paris"))
        }

        countryCodeMap = HashMap()
        countryCodeMap.apply {
            put("AS,WS,", arrayOf("US/Samoa")) // American Samoa, Western Samoa
            put("CA,", arrayOf("US/Pacific", "US/Mountain", "US/Central", "Canada/Saskatchewan", "US/Eastern", "Canada/Atlantic", "Canada/Newfoundland")) // Canada
            // Handled by state map. put("US,", arrayOf("US/Hawaii","US/Alaska","US/Pacific","US/Arizona","US/Mountain","US/Central","US/Eastern","US/East-Indiana")) // USA
            put("MX,", arrayOf("America/Tijuana", "America/Chihuahua", "America/Mexico_City")) // Mexico
            put("CO,PE,EC,", arrayOf("America/Bogota")) // Columbia, Peru, Ecuador
            put("BR,", arrayOf("America/Bogota", "America/Manaus", "Brazil/East")) // Brazil
            put("VE,", arrayOf("America/Caracas")) // Venezuela
            put("BO,", arrayOf("America/La_Paz")) // Bolivia
            put("CL,", arrayOf("America/Santiago")) // Chile
            put("AR,GY,", arrayOf("America/Buenos_Aires")) // Argentina, Guyana
            put("GL,", arrayOf("America/Godthab")) // Greenland
            put("UY,", arrayOf("America/Montevideo")) // Uruguay
            put("GS,", arrayOf("Atlantic/South_Georgia")) // South Georgia
            put("CV,", arrayOf("Atlantic/Cape_Verde")) // Cape Verde Is
            put("IS,MA,LR,", arrayOf("Atlantic/Reykjavik")) // Iceland, Morocco, Liberia
            put("GB,PT,IE,", arrayOf("Europe/London"))  // Britain, Portugal, Ireland
            put("FR,BE,DK,", arrayOf("Europe/Paris")) //France, Belgium, Denmark
            put("DE,NL,IT,AT,CH,SE,NO,", arrayOf("Europe/Amsterdam")) // Germany, Netherlans, Italy, Austria, Switzerland, Sweden, Norway (assumed)
            put("GR,TR,RO,", arrayOf("Europe/Bucharest")) // Greece, Turkey, Romania
            put("FI,UA,LV,BG,EE,LT,", arrayOf("Europe/Helsinki")) // Finland, Ukraine, Latvia, Bulgaria, Estonia, Lithuania
            put("PL,HR,BA,MK,", arrayOf("Europe/Sarajevo")) // Poland, Croatia, Bosnia, Macedonia
            put("CZ,SK,HU,SI,RS,", arrayOf("Europe/Belgrade")) // Czech R, Slovakia, Hungary, Slovenia, Serbia
            put("JO,", arrayOf("Asia/Amman")) // Jordan
            put("LB,", arrayOf("Asia/Beirut")) // Lebanon
            put("EG,", arrayOf("Africa/Cairo")) // Egypt
            put("ZW,ZA,", arrayOf("Africa/Harare")) // Zimbabwe, South Africa
            put("IL,", arrayOf("Asia/Jerusalem")) // Israel
            put("BY,", arrayOf("Etc/GMT-3")) // Belarus
            put("IQ,", arrayOf("Asia/Baghdad")) // Iraq
            put("KW,SA,", arrayOf("Asia/Kuwait")) // Kuwait, Saudi Arabia
            put("RU,", arrayOf("Etc/GMT-4", "Etc/GMT-6", "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12")) // Russia
            put("KE,", arrayOf("Africa/Nairobi")) // Kenya
            put("IR,", arrayOf("Asia/Tehran")) // Iran
            put("AE,OM,", arrayOf("Asia/Muscat")) // UAE, Oman
            put("AZ,", arrayOf("Asia/Baku")) // Azerbaijan
            put("AM,", arrayOf("Asia/Yerevan")) // Armenia
            put("AF,", arrayOf("Asia/Kabul")) // Afghanistan
            put("PK,UZ,", arrayOf("Asia/Tashkent")) // Pakistan, Uzbekistan
            put("IN,", arrayOf("Asia/Calcutta")) // India
            put("NP,", arrayOf("Asia/Katmandu")) // Nepal
            put("KZ,BD,", arrayOf("Asia/Dhaka")) // Kazakhstan, Bangladesh
            put("MM,", arrayOf("Asia/Rangoon")) // Burma
            put("TH,VN,", arrayOf("Asia/Jakarta")) // Thailand, Vietnam, Indonesia
            put("MN,", arrayOf("Asia/Ulaanbaatar")) // Mongolia
            put("MY,", arrayOf("Asia/Kuala_Lumpur")) // Malaysia
            put("JP,", arrayOf("Asia/Tokyo")) // Japan
            put("KR,KP,", arrayOf("Asia/Seoul")) // North Korea, South Korea
            put("PG,", arrayOf("Pacific/Port_Moresby")) // Papua New Guinea
            put("SB,NC,", arrayOf("Asia/Magadan")) // Solomon Is, New Caledonia
            put("NZ,", arrayOf("Pacific/Auckland")) // New Zealand
            put("CN,", arrayOf("Asia/Hong_Kong")) // China
            // Handled by state map. put("AU,", arrayOf("Australia/Perth", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Australia/Hobart")) // Australia
            put("FJ,MH,", arrayOf("Pacific/Fiji")) // Fiji, Marshall Is
            put("TO,", arrayOf("Pacific/Tongatapu")) // Tonga
        }

        timeZoneMap = LinkedHashMap()
        timeZoneMap.apply {
            put("UTC", "Coordinated Universal Time")
            put("US/Samoa", "Midway Island, Samoa")
            put("America/Adak", "Aleutian Islands")
            put("US/Hawaii", "Hawaii")
            put("US/Alaska", "Alaska")
            put("US/Pacific", "Pacific Time (US & Canada)")
            put("America/Tijuana", "Tijuana, Baja California")
            put("US/Arizona", "Arizona")
            put("America/Chihuahua", "Chihuahua, La Paz, Mazatlan")
            put("US/Mountain", "Mountain Time (US & Canada)")
            put("US/Central", "Central Time (US & Canada)")
            put("America/Mexico_City", "Guadalajara, Mexico City, Monterray")
            put("Canada/Saskatchewan", "Saskatchewan")
            put("America/Bogota", "Bogota, Lima, Quito, Rio Branco")
            put("US/Eastern", "Eastern Time (US & Canada)")
            put("US/East-Indiana", "Indiana (East)")
            put("Canada/Atlantic", "Atlantic Time (Canada)")
            put("America/Caracas", "Caracas")
            put("America/La_Paz", "La Paz")
            put("America/Manaus", "Manaus")
            put("America/Santiago", "Santiago")
            put("Canada/Newfoundland", "Newfoundland")
            put("Brazil/East", "Brasilia")
            put("America/Buenos_Aires", "Buenos Aires, Georgetown")
            put("America/Godthab", "Greenland")
            put("America/Montevideo", "Montevideo")
            put("Atlantic/South_Georgia", "Mid-Atlantic")
            put("Atlantic/Azores", "Azores")
            put("Atlantic/Cape_Verde", "Cape Verde Is.")
            put("Atlantic/Reykjavik", "Casablanca, Monrovia, Reykjavik")
            put("Europe/London", "Dublin, Edinburgh, Lisbon, London")
            put("Europe/Amsterdam", "Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna")
            put("Europe/Belgrade", "Belgrade, Bratislava, Budapest, Ljubliana, Prague")
            put("Europe/Paris", "Brussels, Copenhagen, Madrid, Paris")
            put("Europe/Sarajevo", "Sarajevo, Skopje, Warsaw, Zagreb")
            put("Asia/Amman", "Amman")
            put("Europe/Bucharest", "Athens, Bucharest, Istanbul")
            put("Asia/Beirut", "Beirut")
            put("Africa/Cairo", "Cairo")
            put("Africa/Harare", "Harare, Pretoria")
            put("Europe/Helsinki", "Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius")
            put("Asia/Jerusalem", "Jerusalem")
            put("Etc/GMT-3", "Minsk")
            put("Asia/Baghdad", "Baghdad")
            put("Asia/Kuwait", "Kuwait, Riyadh")
            put("Etc/GMT-4", "Moscow, St. Petersburg, Volgograd")
            put("Africa/Nairobi", "Nairobi")
            put("Asia/Tehran", "Tehran")
            put("Asia/Muscat", "Abu Dhabi, Muscat")
            put("Asia/Baku", "Baku")
            put("Asia/Yerevan", "Yerevan")
            put("Asia/Kabul", "Kabul")
            put("Etc/GMT-6", "Ekaterinburg")
            put("Asia/Tashkent", "Islamabad, Karachi, Tashkent")
            put("Asia/Calcutta", "Chennai, Kolkata, Mumbai, New Delhi")
            put("Asia/Katmandu", "Katmandu")
            put("Etc/GMT-7", "Novosibirsk")
            put("Asia/Dhaka", "Astana, Dhaka")
            put("Asia/Rangoon", "Yangon (Rangoon)")
            put("Asia/Jakarta", "Bangkok, Hanoi, Jakarta")
            put("Etc/GMT-8", "Krasnoyarsk")
            put("Asia/Hong_Kong", "Beijing, Chongqing, Hong Kong, Urumqi")
            put("Etc/GMT-9", "Irkutsk")
            put("Asia/Ulaanbaatar", "Ulaan Bataar")
            put("Asia/Kuala_Lumpur", "Kuala Lumpur, Singapore")
            put("Australia/Perth", "Perth")
            put("Asia/Taipei", "Taipei")
            put("Asia/Tokyo", "Osaka, Sapporo, Tokyo")
            put("Asia/Seoul", "Seoul")
            put("Etc/GMT-10", "Yakutsk")
            put("Australia/Adelaide", "Adelaide")
            put("Australia/Darwin", "Darwin")
            put("Australia/Brisbane", "Brisbane")
            put("Pacific/Port_Moresby", "Guam, Port Moresby")
            put("Australia/Sydney", "Canberra, Melbourne, Sydney")
            put("Australia/Hobart", "Hobart")
            put("Etc/GMT-11", "Vladivostok")
            put("Etc/GMT-12", "Magadan")
            put("Pacific/Noumea", "Solomon Is., New Caledonia")
            put("Pacific/Auckland", "Auckland, Wellington")
            put("Pacific/Fiji", "Fiji, Marshall Is.")
            put("Pacific/Tongatapu", "Nuku'alofa")
        }

    }

}
