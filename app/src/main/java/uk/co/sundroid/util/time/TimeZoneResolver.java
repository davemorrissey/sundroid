package uk.co.sundroid.util.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import uk.co.sundroid.domain.TimeZoneDetail;
import uk.co.sundroid.util.StringUtils;
import uk.co.sundroid.util.location.LatitudeLongitude;


public class TimeZoneResolver {
	
	/** Map of comma-separated country codes to java timezone IDs for those countries. */
	private static Map<String, String[]> countryCodeMap;
	
	/** Map of country code : state to java timezone IDs for states where zone is known. */
	private static Map<String, String[]> stateMap;
	
	/** Map of java timezone ID to cities list for display. */
	private static Map<String, String> timeZoneMap;
	
	public static ArrayList<TimeZoneDetail> getAllTimeZones() {
		return resolve(timeZoneMap.keySet());
	}
	
	/**
	 * Attempts to work out candidate time zones for a given place. If the device time zone is specified,
	 * this is always used in preference to location. If not or it can't be matched, the country code
	 * and state are looked up in config maps to find a list of candidate zones. If this method returns
	 * an empty list, the time zone can't be worked out and a list must be presented.
	 */
	public static ArrayList<TimeZoneDetail> getPossibleTimeZones(
			LatitudeLongitude location,
			String countryCode,
			String state) {

		// Step 2: Look up country code. Countries with state mappings aren't in this map.
		if (StringUtils.isNotEmpty(countryCode)) {
			for (Map.Entry<String, String[]> entry : countryCodeMap.entrySet()) {
				if (entry.getKey().contains(countryCode + ",")) {
					String[] possibleTimeZoneIds = entry.getValue();
					return resolve(Arrays.asList(possibleTimeZoneIds));
				}
			}
		}
		
		// Step 2.9: Texas exception
		if ("US".equals(countryCode) && "Texas".equals(state)) {
			if (location.getLongitude().getDoubleValue() < -104.9172) {
				return resolve(Collections.singletonList("US/Mountain"));
			} else {
				return resolve(Collections.singletonList("US/Central"));
			}
		}
		
		// Step 3: Use state to resolve some ambiguous countries.
		if (StringUtils.isNotEmpty(countryCode) && StringUtils.isNotEmpty(state) && stateMap.containsKey(countryCode + ":" + state)) {
			String[] possibleTimeZoneIds = stateMap.get(countryCode + ":" + state);
			return resolve(Arrays.asList(possibleTimeZoneIds));
		}
		
		// Step 4: Country might be known but state wasn't - if so use the catch-all state mapping.
		if (StringUtils.isNotEmpty(countryCode) && stateMap.containsKey(countryCode + ":*")) {
			String[] possibleTimeZoneIds = stateMap.get(countryCode + ":*");
			return resolve(Arrays.asList(possibleTimeZoneIds));
		}
		
		// Step 4: Use location to resolve remaining countries (not implemented)
		
		// Step 5: Give up and return no suggestions. Device will use UTC or ask for supported zone.
		return new ArrayList<>();
		
	}
	
	/**
	 * Attempt to get a time zone by ID, and optionally default to the device zone if the zone ID is
	 * not known to Java. The cities attribute is populated from the stored map when possible.
	 * @param id Time zone ID.
	 * @param fallback Whether to fall back to using default time zone if the device doesn't recognise the ID.
	 * @return A {@link TimeZoneDetail}. Only the cities attribute may be null.
	 */
	public static TimeZoneDetail getTimeZone(String id, boolean fallback) {
		if (id != null) {
			try {
				TimeZone timeZone = TimeZone.getTimeZone(id);
				if (timeZone != null && (!timeZone.getID().equals("UTC") || id.equals("UTC"))) {
					return new TimeZoneDetail(id, timeZoneMap.get(id), timeZone);
				}
			} catch (Exception e) {
				// Zone not known to this device.
			}
		}
		if (fallback || id == null) {
			return getTimeZone(TimeZone.getDefault().getID(), false);
		}
		return null;
	}
	
	private static ArrayList<TimeZoneDetail> resolve(Collection<String> timeZoneIds) {
		ArrayList<TimeZoneDetail> result = new ArrayList<>();
		for (String timeZoneId : timeZoneIds) {
			TimeZoneDetail timeZoneDetail = getTimeZone(timeZoneId, false);
			if (timeZoneDetail != null) {
				result.add(timeZoneDetail);
			}
		}
		return result;
	}
	
	static {
		
		stateMap = new HashMap<>();
		stateMap.put("US:Alabama", new String[] { "US/Central" });
		stateMap.put("US:Alaska", new String[] { "US/Alaska", "America/Adak" });
		stateMap.put("US:Arizona", new String[] { "US/Arizona" });
		stateMap.put("US:Arkansas", new String[] { "US/Central" });
		stateMap.put("US:California", new String[] { "US/Pacific" });
		stateMap.put("US:Colorado", new String[] { "US/Mountain" });
		stateMap.put("US:Connecticut", new String[] { "US/Eastern" });
		stateMap.put("US:Delaware", new String[] { "US/Eastern" });
		stateMap.put("US:Florida", new String[] { "US/Eastern", "US/Central" });
		stateMap.put("US:Georgia", new String[] { "US/Eastern" });
		stateMap.put("US:Hawaii", new String[] { "US/Hawaii" });
		stateMap.put("US:Idaho", new String[] { "US/Mountain" });
		stateMap.put("US:Illinois", new String[] { "US/Central" });
		stateMap.put("US:Indiana", new String[] { "US/Eastern", "US/Central" });
		stateMap.put("US:Iowa", new String[] { "US/Central" });
		stateMap.put("US:Kansas", new String[] { "US/Central", "US/Mountain" });
		stateMap.put("US:Kentucky", new String[] { "US/Eastern", "US/Central" });
		stateMap.put("US:Louisiana", new String[] { "US/Central" });
		stateMap.put("US:Maine", new String[] { "US/Eastern" });
		stateMap.put("US:Maryland", new String[] { "US/Eastern" });
		stateMap.put("US:Massachusetts", new String[] { "US/Eastern" });
		stateMap.put("US:Michigan", new String[] { "US/Eastern" });
		stateMap.put("US:Minnesota", new String[] { "US/Central" });
		stateMap.put("US:Mississippi", new String[] { "US/Central" });
		stateMap.put("US:Missouri", new String[] { "US/Central" });
		stateMap.put("US:Montana", new String[] { "US/Mountain", "US/Pacific" });
		stateMap.put("US:Nebraska", new String[] { "US/Central", "US/Mountain" });
		stateMap.put("US:Nevada", new String[] { "US/Pacific" });
		stateMap.put("US:New Hampshire", new String[] { "US/Eastern" });
		stateMap.put("US:New Jersey", new String[] { "US/Eastern" });
		stateMap.put("US:New Mexico", new String[] { "US/Mountain" });
		stateMap.put("US:New York", new String[] { "US/Eastern" });
		stateMap.put("US:North Carolina", new String[] { "US/Eastern" });
		stateMap.put("US:North Dakota", new String[] { "US/Central", "US/Mountain" });
		stateMap.put("US:Ohio", new String[] { "US/Eastern" });
		stateMap.put("US:Oklahoma", new String[] { "US/Central" });
		stateMap.put("US:Oregon", new String[] { "US/Pacific", "US/Mountain" });
		stateMap.put("US:Pennsylvania", new String[] { "US/Eastern" });
		stateMap.put("US:Rhode Island", new String[] { "US/Eastern" });
		stateMap.put("US:South Carolina", new String[] { "US/Eastern" });
		stateMap.put("US:South Dakota", new String[] { "US/Central", "US/Mountain" });
		stateMap.put("US:Tennessee", new String[] { "US/Eastern", "US/Central" });
		stateMap.put("US:Texas", new String[] { "US/Central" });
		stateMap.put("US:Utah", new String[] { "US/Mountain" });
		stateMap.put("US:Vermont", new String[] { "US/Eastern" });
		stateMap.put("US:Virginia", new String[] { "US/Eastern" });
		stateMap.put("US:Washington", new String[] { "US/Pacific" });
		stateMap.put("US:West Virginia", new String[] { "US/Eastern" });
		stateMap.put("US:Wisconsin", new String[] { "US/Central" });
		stateMap.put("US:Wyoming", new String[] { "US/Mountain" });
		stateMap.put("US:*", new String[] {"US/Hawaii","America/Adak","US/Alaska","US/Pacific","US/Arizona","US/Mountain","US/Central","US/Eastern","US/East-Indiana"});
		stateMap.put("AU:Western Australia", new String[] { "Australia/Perth" });
		stateMap.put("AU:South Australia", new String[] { "Australia/Adelaide" });
		stateMap.put("AU:Northern Territory", new String[] { "Australia/Darwin" });
		stateMap.put("AU:Queensland", new String[] { "Australia/Brisbane" });
		stateMap.put("AU:New South Wales", new String[] { "Australia/Sydney" });
		stateMap.put("AU:Victoria", new String[] { "Australia/Sydney" });
		stateMap.put("AU:Tasmania", new String[] { "Australia/Hobart" });
		stateMap.put("AU:*", new String[] { "Australia/Perth", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Australia/Hobart" });
		stateMap.put("ES:Canary Islands", new String[] { "Europe/London" });
		stateMap.put("ES:*", new String[] { "Europe/Paris" });
		
		
		countryCodeMap = new HashMap<>();
		countryCodeMap.put("AS,WS,", new String[] {"US/Samoa"}); // American Samoa, Western Samoa
		countryCodeMap.put("CA,", new String[] {"US/Pacific", "US/Mountain", "US/Central", "Canada/Saskatchewan", "US/Eastern", "Canada/Atlantic", "Canada/Newfoundland"}); // Canada
		// Handled by state map. countryCodeMap.put("US,", new String[] {"US/Hawaii","US/Alaska","US/Pacific","US/Arizona","US/Mountain","US/Central","US/Eastern","US/East-Indiana"}); // USA
		countryCodeMap.put("MX,", new String[] {"America/Tijuana", "America/Chihuahua", "America/Mexico_City"}); // Mexico
		countryCodeMap.put("CO,PE,EC,", new String[] {"America/Bogota"}); // Columbia, Peru, Ecuador
		countryCodeMap.put("BR,", new String[] {"America/Bogota", "America/Manaus", "Brazil/East"}); // Brazil
		countryCodeMap.put("VE,", new String[] {"America/Caracas"}); // Venezuela
		countryCodeMap.put("BO,", new String[] {"America/La_Paz"}); // Bolivia
		countryCodeMap.put("CL,", new String[] {"America/Santiago"}); // Chile
		countryCodeMap.put("AR,GY,", new String[] {"America/Buenos_Aires"}); // Argentina, Guyana
		countryCodeMap.put("GL,", new String[] {"America/Godthab"}); // Greenland
		countryCodeMap.put("UY,", new String[] {"America/Montevideo"}); // Uruguay
		countryCodeMap.put("GS,", new String[] {"Atlantic/South_Georgia"}); // South Georgia
		countryCodeMap.put("CV,", new String[] {"Atlantic/Cape_Verde"}); // Cape Verde Is
		countryCodeMap.put("IS,MA,LR,", new String[] {"Atlantic/Reykjavik"}); // Iceland, Morocco, Liberia
		countryCodeMap.put("GB,PT,IE,", new String[] {"Europe/London"});  // Britain, Portugal, Ireland
		countryCodeMap.put("FR,BE,DK,", new String[] {"Europe/Paris"}); //France, Belgium, Denmark
		countryCodeMap.put("DE,NL,IT,AT,CH,SE,NO,", new String[] {"Europe/Amsterdam"}); // Germany, Netherlans, Italy, Austria, Switzerland, Sweden, Norway (assumed)
		countryCodeMap.put("GR,TR,RO,", new String[] {"Europe/Bucharest"}); // Greece, Turkey, Romania
		countryCodeMap.put("FI,UA,LV,BG,EE,LT,", new String[] {"Europe/Helsinki"}); // Finland, Ukraine, Latvia, Bulgaria, Estonia, Lithuania
		countryCodeMap.put("PL,HR,BA,MK,", new String[] {"Europe/Sarajevo"}); // Poland, Croatia, Bosnia, Macedonia
		countryCodeMap.put("CZ,SK,HU,SI,RS,", new String[] {"Europe/Belgrade"}); // Czech R, Slovakia, Hungary, Slovenia, Serbia
		countryCodeMap.put("JO,", new String[] {"Asia/Amman"}); // Jordan
		countryCodeMap.put("LB,", new String[] {"Asia/Beirut"}); // Lebanon
		countryCodeMap.put("EG,", new String[] {"Africa/Cairo"}); // Egypt
		countryCodeMap.put("ZW,ZA,", new String[] {"Africa/Harare"}); // Zimbabwe, South Africa
		countryCodeMap.put("IL,", new String[] {"Asia/Jerusalem"}); // Israel
		countryCodeMap.put("BY,", new String[] {"Etc/GMT-3"}); // Belarus
		countryCodeMap.put("IQ,", new String[] {"Asia/Baghdad"}); // Iraq
		countryCodeMap.put("KW,SA,", new String[] {"Asia/Kuwait"}); // Kuwait, Saudi Arabia
		countryCodeMap.put("RU,", new String[] {"Etc/GMT-4", "Etc/GMT-6", "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12"}); // Russia
		countryCodeMap.put("KE,", new String[] {"Africa/Nairobi"}); // Kenya
		countryCodeMap.put("IR,", new String[] {"Asia/Tehran"}); // Iran
		countryCodeMap.put("AE,OM,", new String[] {"Asia/Muscat"}); // UAE, Oman
		countryCodeMap.put("AZ,", new String[] {"Asia/Baku"}); // Azerbaijan
		countryCodeMap.put("AM,", new String[] {"Asia/Yerevan"}); // Armenia
		countryCodeMap.put("AF,", new String[] {"Asia/Kabul"}); // Afghanistan
		countryCodeMap.put("PK,UZ,", new String[] {"Asia/Tashkent"}); // Pakistan, Uzbekistan
		countryCodeMap.put("IN,", new String[] {"Asia/Calcutta"}); // India
		countryCodeMap.put("NP,", new String[] {"Asia/Katmandu"}); // Nepal
		countryCodeMap.put("KZ,BD,", new String[] {"Asia/Dhaka"}); // Kazakhstan, Bangladesh
		countryCodeMap.put("MM,", new String[] {"Asia/Rangoon"}); // Burma
		countryCodeMap.put("TH,VN,", new String[] {"Asia/Jakarta"}); // Thailand, Vietnam, Indonesia
		countryCodeMap.put("MN,", new String[] {"Asia/Ulaanbaatar"}); // Mongolia
		countryCodeMap.put("MY,", new String[] {"Asia/Kuala_Lumpur"}); // Malaysia
		countryCodeMap.put("JP,", new String[] {"Asia/Tokyo"}); // Japan
		countryCodeMap.put("KR,KP,", new String[] {"Asia/Seoul"}); // North Korea, South Korea
		countryCodeMap.put("PG,", new String[] {"Pacific/Port_Moresby"}); // Papua New Guinea
		countryCodeMap.put("SB,NC,", new String[] {"Asia/Magadan"}); // Solomon Is, New Caledonia
		countryCodeMap.put("NZ,", new String[] {"Pacific/Auckland"}); // New Zealand
		countryCodeMap.put("CN,", new String[] {"Asia/Hong_Kong"}); // China
		// Handled by state map. countryCodeMap.put("AU,", new String[] {"Australia/Perth", "Australia/Adelaide", "Australia/Darwin", "Australia/Brisbane", "Australia/Sydney", "Australia/Hobart"}); // Australia
		countryCodeMap.put("FJ,MH,", new String[] {"Pacific/Fiji"}); // Fiji, Marshall Is
		countryCodeMap.put("TO,", new String[] {"Pacific/Tongatapu"}); // Tonga
		
		timeZoneMap = new LinkedHashMap<>();
		timeZoneMap.put("UTC", "Coordinated Universal Time");
		timeZoneMap.put("US/Samoa", "Midway Island, Samoa");
		timeZoneMap.put("America/Adak", "Aleutian Islands");
		timeZoneMap.put("US/Hawaii", "Hawaii");
		timeZoneMap.put("US/Alaska", "Alaska");
		timeZoneMap.put("US/Pacific", "Pacific Time (US & Canada)");
		timeZoneMap.put("America/Tijuana", "Tijuana, Baja California");
		timeZoneMap.put("US/Arizona", "Arizona");
		timeZoneMap.put("America/Chihuahua", "Chihuahua, La Paz, Mazatlan");
		timeZoneMap.put("US/Mountain", "Mountain Time (US & Canada)");
		timeZoneMap.put("US/Central", "Central Time (US & Canada)");
		timeZoneMap.put("America/Mexico_City", "Guadalajara, Mexico City, Monterray");
		timeZoneMap.put("Canada/Saskatchewan", "Saskatchewan");
		timeZoneMap.put("America/Bogota", "Bogota, Lima, Quito, Rio Branco");
		timeZoneMap.put("US/Eastern", "Eastern Time (US & Canada)");
		timeZoneMap.put("US/East-Indiana", "Indiana (East)");
		timeZoneMap.put("Canada/Atlantic", "Atlantic Time (Canada)");
		timeZoneMap.put("America/Caracas", "Caracas");
		timeZoneMap.put("America/La_Paz", "La Paz");
		timeZoneMap.put("America/Manaus", "Manaus");
		timeZoneMap.put("America/Santiago", "Santiago");
		timeZoneMap.put("Canada/Newfoundland", "Newfoundland");
		timeZoneMap.put("Brazil/East", "Brasilia");
		timeZoneMap.put("America/Buenos_Aires", "Buenos Aires, Georgetown");
		timeZoneMap.put("America/Godthab", "Greenland");
		timeZoneMap.put("America/Montevideo", "Montevideo");
		timeZoneMap.put("Atlantic/South_Georgia", "Mid-Atlantic");
		timeZoneMap.put("Atlantic/Azores", "Azores");
		timeZoneMap.put("Atlantic/Cape_Verde", "Cape Verde Is.");
		timeZoneMap.put("Atlantic/Reykjavik", "Casablanca, Monrovia, Reykjavik");
		timeZoneMap.put("Europe/London", "Dublin, Edinburgh, Lisbon, London");
		timeZoneMap.put("Europe/Amsterdam", "Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna");
		timeZoneMap.put("Europe/Belgrade", "Belgrade, Bratislava, Budapest, Ljubliana, Prague");
		timeZoneMap.put("Europe/Paris", "Brussels, Copenhagen, Madrid, Paris");
		timeZoneMap.put("Europe/Sarajevo", "Sarajevo, Skopje, Warsaw, Zagreb");
		timeZoneMap.put("Asia/Amman", "Amman");
		timeZoneMap.put("Europe/Bucharest", "Athens, Bucharest, Istanbul");
		timeZoneMap.put("Asia/Beirut", "Beirut");
		timeZoneMap.put("Africa/Cairo", "Cairo");
		timeZoneMap.put("Africa/Harare", "Harare, Pretoria");
		timeZoneMap.put("Europe/Helsinki", "Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius");
		timeZoneMap.put("Asia/Jerusalem", "Jerusalem");
		timeZoneMap.put("Etc/GMT-3", "Minsk");
		timeZoneMap.put("Asia/Baghdad", "Baghdad");
		timeZoneMap.put("Asia/Kuwait", "Kuwait, Riyadh");
		timeZoneMap.put("Etc/GMT-4", "Moscow, St. Petersburg, Volgograd");
		timeZoneMap.put("Africa/Nairobi", "Nairobi");
		timeZoneMap.put("Asia/Tehran", "Tehran");
		timeZoneMap.put("Asia/Muscat", "Abu Dhabi, Muscat");
		timeZoneMap.put("Asia/Baku", "Baku");
		timeZoneMap.put("Asia/Yerevan", "Yerevan");
		timeZoneMap.put("Asia/Kabul", "Kabul");
		timeZoneMap.put("Etc/GMT-6", "Ekaterinburg");
		timeZoneMap.put("Asia/Tashkent", "Islamabad, Karachi, Tashkent");
		timeZoneMap.put("Asia/Calcutta", "Chennai, Kolkata, Mumbai, New Delhi");
		timeZoneMap.put("Asia/Katmandu", "Katmandu");
		timeZoneMap.put("Etc/GMT-7", "Novosibirsk");
		timeZoneMap.put("Asia/Dhaka", "Astana, Dhaka");
		timeZoneMap.put("Asia/Rangoon", "Yangon (Rangoon)");
		timeZoneMap.put("Asia/Jakarta", "Bangkok, Hanoi, Jakarta");
		timeZoneMap.put("Etc/GMT-8", "Krasnoyarsk");
		timeZoneMap.put("Asia/Hong_Kong", "Beijing, Chongqing, Hong Kong, Urumqi");
		timeZoneMap.put("Etc/GMT-9", "Irkutsk");
		timeZoneMap.put("Asia/Ulaanbaatar", "Ulaan Bataar");
		timeZoneMap.put("Asia/Kuala_Lumpur", "Kuala Lumpur, Singapore");
		timeZoneMap.put("Australia/Perth", "Perth");
		timeZoneMap.put("Asia/Taipei", "Taipei");
		timeZoneMap.put("Asia/Tokyo", "Osaka, Sapporo, Tokyo");
		timeZoneMap.put("Asia/Seoul", "Seoul");
		timeZoneMap.put("Etc/GMT-10", "Yakutsk");
		timeZoneMap.put("Australia/Adelaide", "Adelaide");
		timeZoneMap.put("Australia/Darwin", "Darwin");
		timeZoneMap.put("Australia/Brisbane", "Brisbane");
		timeZoneMap.put("Pacific/Port_Moresby", "Guam, Port Moresby");
		timeZoneMap.put("Australia/Sydney", "Canberra, Melbourne, Sydney");
		timeZoneMap.put("Australia/Hobart", "Hobart");
		timeZoneMap.put("Etc/GMT-11", "Vladivostok");
		timeZoneMap.put("Etc/GMT-12", "Magadan");
		timeZoneMap.put("Pacific/Noumea", "Solomon Is., New Caledonia");
		timeZoneMap.put("Pacific/Auckland", "Auckland, Wellington");
		timeZoneMap.put("Pacific/Fiji", "Fiji, Marshall Is.");
		timeZoneMap.put("Pacific/Tongatapu", "Nuku'alofa");	

	}

}
