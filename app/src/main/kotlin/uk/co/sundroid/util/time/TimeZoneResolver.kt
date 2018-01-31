package uk.co.sundroid.util.time;

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.TimeZone

import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.*
import uk.co.sundroid.util.location.LatitudeLongitude


class TimeZoneResolver {
    
    /** Map of comma-separated country codes to java timezone IDs for those countries. */
    private val countryCodeMap: HashMap<String, Array<String>>
    
    /** Map of country code : state to java timezone IDs for states where zone is known. */
    private val stateMap: HashMap<String, Array<String>>
    
    /** Map of java timezone ID to cities list for display. */
    private val timeZoneMap: HashMap<String, String>

    companion object {

        val instance = TimeZoneResolver()

    }
    
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
        if ("US".equals(countryCode) && "Texas".equals(state)) {
            if (location.longitude.doubleValue < -104.9172) {
                return resolve(Collections.singletonList("US/Mountain"));
            } else {
                return resolve(Collections.singletonList("US/Central"));
            }
        }
        
        // Step 3: Use state to resolve some ambiguous countries.
        if (isNotEmpty(countryCode) && isNotEmpty(state) && stateMap.containsKey(countryCode + ":" + state)) {
            val possibleTimeZoneIds = stateMap.get(countryCode + ":" + state)!!
            return resolve(possibleTimeZoneIds.toMutableList())
        }
        
        // Step 4: Country might be known but state wasn't - if so use the catch-all state mapping.
        if (isNotEmpty(countryCode) && stateMap.containsKey(countryCode + ":*")) {
            val possibleTimeZoneIds = stateMap.get(countryCode + ":*")!!
            return resolve(possibleTimeZoneIds.toMutableList())
        }
        
        // Step 4: Use location to resolve remaining countries (not implemented)
        
        // Step 5: Give up and return no suggestions. Device will use UTC or ask for supported zone.
        return ArrayList()
        
    }
    
    /**
     * Attempt to get a time zone by ID, and optionally default to the device zone if the zone ID is
     * not known to Java. The cities attribute is populated from the stored map when possible.
     * @param id Time zone ID.
     * @param fallback Whether to fall back to using default time zone if the device doesn't recognise the ID.
     * @return A {@link TimeZoneDetail}. Only the cities attribute may be null.
     */
    fun getTimeZone(id: String, fallback: Boolean): TimeZoneDetail? {
        try {
            val timeZone = TimeZone.getTimeZone(id)
            if (timeZone != null && (timeZone.id != "UTC" || id == "UTC")) {
                return TimeZoneDetail(id, timeZoneMap.get(id), timeZone)
            }
        } catch (e: Exception) {
            // Zone not known to this device.
        }
        if (fallback) {
            return getTimeZone(TimeZone.getDefault().id, false)
        }
        return null
    }
    
    private fun resolve(timeZoneIds: MutableCollection<String>): ArrayList<TimeZoneDetail> {
        val result = ArrayList<TimeZoneDetail>()
        for (timeZoneId in timeZoneIds) {
            val timeZoneDetail = getTimeZone(timeZoneId, false)
            if (timeZoneDetail != null) {
                result.add(timeZoneDetail)
            }
        }
        return result
    }
    
    private constructor() {
        
        stateMap = HashMap()
        stateMap.put("US:Alabama", arrayOf("US/Central"))
        stateMap.put("US:Alaska", arrayOf("US/Alaska", "America/Adak"))
        stateMap.put("US:Arizona", arrayOf("US/Arizona"))
        stateMap.put("US:Arkansas", arrayOf("US/Central"))
        stateMap.put("US:California", arrayOf("US/Pacific"))
        stateMap.put("US:Colorado", arrayOf("US/Mountain"))
        stateMap.put("US:Connecticut", arrayOf("US/Eastern"))
        stateMap.put("US:Delaware", arrayOf("US/Eastern"))
        stateMap.put("US:Florida", arrayOf("US/Eastern", "US/Central"))
        stateMap.put("US:Georgia", arrayOf("US/Eastern"))
        stateMap.put("US:Hawaii", arrayOf("US/Hawaii"))
        stateMap.put("US:Idaho", arrayOf("US/Mountain"))
        stateMap.put("US:Illinois", arrayOf("US/Central"))
        stateMap.put("US:Indiana", arrayOf("US/Eastern", "US/Central"))
        stateMap.put("US:Iowa", arrayOf("US/Central"))
        stateMap.put("US:Kansas", arrayOf("US/Central", "US/Mountain"))
        stateMap.put("US:Kentucky", arrayOf("US/Eastern", "US/Central"))
        stateMap.put("US:Louisiana", arrayOf("US/Central"))
        stateMap.put("US:Maine", arrayOf("US/Eastern"))
        stateMap.put("US:Maryland", arrayOf("US/Eastern"))
        stateMap.put("US:Massachusetts", arrayOf("US/Eastern"))
        stateMap.put("US:Michigan", arrayOf("US/Eastern"))
        stateMap.put("US:Minnesota", arrayOf("US/Central"))
        stateMap.put("US:Mississippi", arrayOf("US/Central"))
        stateMap.put("US:Missouri", arrayOf("US/Central"))
        stateMap.put("US:Montana", arrayOf("US/Mountain", "US/Pacific"))
        stateMap.put("US:Nebraska", arrayOf("US/Central", "US/Mountain"))
        stateMap.put("US:Nevada", arrayOf("US/Pacific"))
        stateMap.put("US:New Hampshire", arrayOf("US/Eastern"))
        stateMap.put("US:New Jersey", arrayOf("US/Eastern"))
        stateMap.put("US:New Mexico", arrayOf("US/Mountain"))
        stateMap.put("US:New York", arrayOf("US/Eastern"))
        stateMap.put("US:North Carolina", arrayOf("US/Eastern"))
        stateMap.put("US:North Dakota", arrayOf("US/Central", "US/Mountain"))
        stateMap.put("US:Ohio", arrayOf("US/Eastern"))
        stateMap.put("US:Oklahoma", arrayOf("US/Central"))
        stateMap.put("US:Oregon", arrayOf("US/Pacific", "US/Mountain"))
        stateMap.put("US:Pennsylvania", arrayOf("US/Eastern"))
        stateMap.put("US:Rhode Island", arrayOf("US/Eastern"))
        stateMap.put("US:South Carolina", arrayOf("US/Eastern"))
        stateMap.put("US:South Dakota", arrayOf("US/Central", "US/Mountain"))
        stateMap.put("US:Tennessee", arrayOf("US/Eastern", "US/Central"))
        stateMap.put("US:Texas", arrayOf("US/Central"))
        stateMap.put("US:Utah", arrayOf("US/Mountain"))
        stateMap.put("US:Vermont", arrayOf("US/Eastern"))
        stateMap.put("US:Virginia", arrayOf("US/Eastern"))
        stateMap.put("US:Washington", arrayOf("US/Pacific"))
        stateMap.put("US:West Virginia", arrayOf("US/Eastern"))
        stateMap.put("US:Wisconsin", arrayOf("US/Central"))
        stateMap.put("US:Wyoming", arrayOf("US/Mountain"))
        stateMap.put("US:*", arrayOf("US/Hawaii","America/Adak","US/Alaska","US/Pacific","US/Arizona","US/Mountain","US/Central","US/Eastern","US/East-Indiana"))
        stateMap.put("AU:Western Australia", arrayOf("Australia/Perth"))
        stateMap.put("AU:South Australia", arrayOf("Australia/Adelaide"))
        stateMap.put("AU:Northern Territory", arrayOf("Australia/Darwin"))
        stateMap.put("AU:Queensland", arrayOf("Australia/Brisbane"))
        stateMap.put("AU:New South Wales", arrayOf("Australia/Sydney"))
        stateMap.put("AU:Victoria", arrayOf("Australia/Sydney"))
        stateMap.put("AU:Tasmania", arrayOf("Australia/Hobart"))
        stateMap.put("AU:*", arrayOf("Australia/Perth", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Australia/Hobart"))
        stateMap.put("ES:Canary Islands", arrayOf("Europe/London"))
        stateMap.put("ES:*", arrayOf("Europe/Paris"))
        
        
        countryCodeMap = HashMap()
        countryCodeMap.put("AS,WS,", arrayOf("US/Samoa")) // American Samoa, Western Samoa
        countryCodeMap.put("CA,", arrayOf("US/Pacific", "US/Mountain", "US/Central", "Canada/Saskatchewan", "US/Eastern", "Canada/Atlantic", "Canada/Newfoundland")) // Canada
        // Handled by state map. countryCodeMap.put("US,", arrayOf("US/Hawaii","US/Alaska","US/Pacific","US/Arizona","US/Mountain","US/Central","US/Eastern","US/East-Indiana")) // USA
        countryCodeMap.put("MX,", arrayOf("America/Tijuana", "America/Chihuahua", "America/Mexico_City")) // Mexico
        countryCodeMap.put("CO,PE,EC,", arrayOf("America/Bogota")) // Columbia, Peru, Ecuador
        countryCodeMap.put("BR,", arrayOf("America/Bogota", "America/Manaus", "Brazil/East")) // Brazil
        countryCodeMap.put("VE,", arrayOf("America/Caracas")) // Venezuela
        countryCodeMap.put("BO,", arrayOf("America/La_Paz")) // Bolivia
        countryCodeMap.put("CL,", arrayOf("America/Santiago")) // Chile
        countryCodeMap.put("AR,GY,", arrayOf("America/Buenos_Aires")) // Argentina, Guyana
        countryCodeMap.put("GL,", arrayOf("America/Godthab")) // Greenland
        countryCodeMap.put("UY,", arrayOf("America/Montevideo")) // Uruguay
        countryCodeMap.put("GS,", arrayOf("Atlantic/South_Georgia")) // South Georgia
        countryCodeMap.put("CV,", arrayOf("Atlantic/Cape_Verde")) // Cape Verde Is
        countryCodeMap.put("IS,MA,LR,", arrayOf("Atlantic/Reykjavik")) // Iceland, Morocco, Liberia
        countryCodeMap.put("GB,PT,IE,", arrayOf("Europe/London"))  // Britain, Portugal, Ireland
        countryCodeMap.put("FR,BE,DK,", arrayOf("Europe/Paris")) //France, Belgium, Denmark
        countryCodeMap.put("DE,NL,IT,AT,CH,SE,NO,", arrayOf("Europe/Amsterdam")) // Germany, Netherlans, Italy, Austria, Switzerland, Sweden, Norway (assumed)
        countryCodeMap.put("GR,TR,RO,", arrayOf("Europe/Bucharest")) // Greece, Turkey, Romania
        countryCodeMap.put("FI,UA,LV,BG,EE,LT,", arrayOf("Europe/Helsinki")) // Finland, Ukraine, Latvia, Bulgaria, Estonia, Lithuania
        countryCodeMap.put("PL,HR,BA,MK,", arrayOf("Europe/Sarajevo")) // Poland, Croatia, Bosnia, Macedonia
        countryCodeMap.put("CZ,SK,HU,SI,RS,", arrayOf("Europe/Belgrade")) // Czech R, Slovakia, Hungary, Slovenia, Serbia
        countryCodeMap.put("JO,", arrayOf("Asia/Amman")) // Jordan
        countryCodeMap.put("LB,", arrayOf("Asia/Beirut")) // Lebanon
        countryCodeMap.put("EG,", arrayOf("Africa/Cairo")) // Egypt
        countryCodeMap.put("ZW,ZA,", arrayOf("Africa/Harare")) // Zimbabwe, South Africa
        countryCodeMap.put("IL,", arrayOf("Asia/Jerusalem")) // Israel
        countryCodeMap.put("BY,", arrayOf("Etc/GMT-3")) // Belarus
        countryCodeMap.put("IQ,", arrayOf("Asia/Baghdad")) // Iraq
        countryCodeMap.put("KW,SA,", arrayOf("Asia/Kuwait")) // Kuwait, Saudi Arabia
        countryCodeMap.put("RU,", arrayOf("Etc/GMT-4", "Etc/GMT-6", "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12")) // Russia
        countryCodeMap.put("KE,", arrayOf("Africa/Nairobi")) // Kenya
        countryCodeMap.put("IR,", arrayOf("Asia/Tehran")) // Iran
        countryCodeMap.put("AE,OM,", arrayOf("Asia/Muscat")) // UAE, Oman
        countryCodeMap.put("AZ,", arrayOf("Asia/Baku")) // Azerbaijan
        countryCodeMap.put("AM,", arrayOf("Asia/Yerevan")) // Armenia
        countryCodeMap.put("AF,", arrayOf("Asia/Kabul")) // Afghanistan
        countryCodeMap.put("PK,UZ,", arrayOf("Asia/Tashkent")) // Pakistan, Uzbekistan
        countryCodeMap.put("IN,", arrayOf("Asia/Calcutta")) // India
        countryCodeMap.put("NP,", arrayOf("Asia/Katmandu")) // Nepal
        countryCodeMap.put("KZ,BD,", arrayOf("Asia/Dhaka")) // Kazakhstan, Bangladesh
        countryCodeMap.put("MM,", arrayOf("Asia/Rangoon")) // Burma
        countryCodeMap.put("TH,VN,", arrayOf("Asia/Jakarta")) // Thailand, Vietnam, Indonesia
        countryCodeMap.put("MN,", arrayOf("Asia/Ulaanbaatar")) // Mongolia
        countryCodeMap.put("MY,", arrayOf("Asia/Kuala_Lumpur")) // Malaysia
        countryCodeMap.put("JP,", arrayOf("Asia/Tokyo")) // Japan
        countryCodeMap.put("KR,KP,", arrayOf("Asia/Seoul")) // North Korea, South Korea
        countryCodeMap.put("PG,", arrayOf("Pacific/Port_Moresby")) // Papua New Guinea
        countryCodeMap.put("SB,NC,", arrayOf("Asia/Magadan")) // Solomon Is, New Caledonia
        countryCodeMap.put("NZ,", arrayOf("Pacific/Auckland")) // New Zealand
        countryCodeMap.put("CN,", arrayOf("Asia/Hong_Kong")) // China
        // Handled by state map. countryCodeMap.put("AU,", arrayOf("Australia/Perth", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Australia/Hobart")) // Australia
        countryCodeMap.put("FJ,MH,", arrayOf("Pacific/Fiji")) // Fiji, Marshall Is
        countryCodeMap.put("TO,", arrayOf("Pacific/Tongatapu")) // Tonga
        
        timeZoneMap = LinkedHashMap()
        timeZoneMap.put("UTC", "Coordinated Universal Time")
        timeZoneMap.put("US/Samoa", "Midway Island, Samoa")
        timeZoneMap.put("America/Adak", "Aleutian Islands")
        timeZoneMap.put("US/Hawaii", "Hawaii")
        timeZoneMap.put("US/Alaska", "Alaska")
        timeZoneMap.put("US/Pacific", "Pacific Time (US & Canada)")
        timeZoneMap.put("America/Tijuana", "Tijuana, Baja California")
        timeZoneMap.put("US/Arizona", "Arizona")
        timeZoneMap.put("America/Chihuahua", "Chihuahua, La Paz, Mazatlan")
        timeZoneMap.put("US/Mountain", "Mountain Time (US & Canada)")
        timeZoneMap.put("US/Central", "Central Time (US & Canada)")
        timeZoneMap.put("America/Mexico_City", "Guadalajara, Mexico City, Monterray")
        timeZoneMap.put("Canada/Saskatchewan", "Saskatchewan")
        timeZoneMap.put("America/Bogota", "Bogota, Lima, Quito, Rio Branco")
        timeZoneMap.put("US/Eastern", "Eastern Time (US & Canada)")
        timeZoneMap.put("US/East-Indiana", "Indiana (East)")
        timeZoneMap.put("Canada/Atlantic", "Atlantic Time (Canada)")
        timeZoneMap.put("America/Caracas", "Caracas")
        timeZoneMap.put("America/La_Paz", "La Paz")
        timeZoneMap.put("America/Manaus", "Manaus")
        timeZoneMap.put("America/Santiago", "Santiago")
        timeZoneMap.put("Canada/Newfoundland", "Newfoundland")
        timeZoneMap.put("Brazil/East", "Brasilia")
        timeZoneMap.put("America/Buenos_Aires", "Buenos Aires, Georgetown")
        timeZoneMap.put("America/Godthab", "Greenland")
        timeZoneMap.put("America/Montevideo", "Montevideo")
        timeZoneMap.put("Atlantic/South_Georgia", "Mid-Atlantic")
        timeZoneMap.put("Atlantic/Azores", "Azores")
        timeZoneMap.put("Atlantic/Cape_Verde", "Cape Verde Is.")
        timeZoneMap.put("Atlantic/Reykjavik", "Casablanca, Monrovia, Reykjavik")
        timeZoneMap.put("Europe/London", "Dublin, Edinburgh, Lisbon, London")
        timeZoneMap.put("Europe/Amsterdam", "Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna")
        timeZoneMap.put("Europe/Belgrade", "Belgrade, Bratislava, Budapest, Ljubliana, Prague")
        timeZoneMap.put("Europe/Paris", "Brussels, Copenhagen, Madrid, Paris")
        timeZoneMap.put("Europe/Sarajevo", "Sarajevo, Skopje, Warsaw, Zagreb")
        timeZoneMap.put("Asia/Amman", "Amman")
        timeZoneMap.put("Europe/Bucharest", "Athens, Bucharest, Istanbul")
        timeZoneMap.put("Asia/Beirut", "Beirut")
        timeZoneMap.put("Africa/Cairo", "Cairo")
        timeZoneMap.put("Africa/Harare", "Harare, Pretoria")
        timeZoneMap.put("Europe/Helsinki", "Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius")
        timeZoneMap.put("Asia/Jerusalem", "Jerusalem")
        timeZoneMap.put("Etc/GMT-3", "Minsk")
        timeZoneMap.put("Asia/Baghdad", "Baghdad")
        timeZoneMap.put("Asia/Kuwait", "Kuwait, Riyadh")
        timeZoneMap.put("Etc/GMT-4", "Moscow, St. Petersburg, Volgograd")
        timeZoneMap.put("Africa/Nairobi", "Nairobi")
        timeZoneMap.put("Asia/Tehran", "Tehran")
        timeZoneMap.put("Asia/Muscat", "Abu Dhabi, Muscat")
        timeZoneMap.put("Asia/Baku", "Baku")
        timeZoneMap.put("Asia/Yerevan", "Yerevan")
        timeZoneMap.put("Asia/Kabul", "Kabul")
        timeZoneMap.put("Etc/GMT-6", "Ekaterinburg")
        timeZoneMap.put("Asia/Tashkent", "Islamabad, Karachi, Tashkent")
        timeZoneMap.put("Asia/Calcutta", "Chennai, Kolkata, Mumbai, New Delhi")
        timeZoneMap.put("Asia/Katmandu", "Katmandu")
        timeZoneMap.put("Etc/GMT-7", "Novosibirsk")
        timeZoneMap.put("Asia/Dhaka", "Astana, Dhaka")
        timeZoneMap.put("Asia/Rangoon", "Yangon (Rangoon)")
        timeZoneMap.put("Asia/Jakarta", "Bangkok, Hanoi, Jakarta")
        timeZoneMap.put("Etc/GMT-8", "Krasnoyarsk")
        timeZoneMap.put("Asia/Hong_Kong", "Beijing, Chongqing, Hong Kong, Urumqi")
        timeZoneMap.put("Etc/GMT-9", "Irkutsk")
        timeZoneMap.put("Asia/Ulaanbaatar", "Ulaan Bataar")
        timeZoneMap.put("Asia/Kuala_Lumpur", "Kuala Lumpur, Singapore")
        timeZoneMap.put("Australia/Perth", "Perth")
        timeZoneMap.put("Asia/Taipei", "Taipei")
        timeZoneMap.put("Asia/Tokyo", "Osaka, Sapporo, Tokyo")
        timeZoneMap.put("Asia/Seoul", "Seoul")
        timeZoneMap.put("Etc/GMT-10", "Yakutsk")
        timeZoneMap.put("Australia/Adelaide", "Adelaide")
        timeZoneMap.put("Australia/Darwin", "Darwin")
        timeZoneMap.put("Australia/Brisbane", "Brisbane")
        timeZoneMap.put("Pacific/Port_Moresby", "Guam, Port Moresby")
        timeZoneMap.put("Australia/Sydney", "Canberra, Melbourne, Sydney")
        timeZoneMap.put("Australia/Hobart", "Hobart")
        timeZoneMap.put("Etc/GMT-11", "Vladivostok")
        timeZoneMap.put("Etc/GMT-12", "Magadan")
        timeZoneMap.put("Pacific/Noumea", "Solomon Is., New Caledonia")
        timeZoneMap.put("Pacific/Auckland", "Auckland, Wellington")
        timeZoneMap.put("Pacific/Fiji", "Fiji, Marshall Is.")
        timeZoneMap.put("Pacific/Tongatapu", "Nuku'alofa")    

    }

}
