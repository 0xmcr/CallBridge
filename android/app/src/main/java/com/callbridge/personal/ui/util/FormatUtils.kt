package com.callbridge.personal.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhoneValidation(
    val isValid: Boolean,
    val formatted: String,
    val e164: String,
    val message: String
)

object FormatUtils {

    /**
     * Resolves a 3-digit North American area code into its corresponding US City/State location.
     * Returns null if fewer than 3 digits or unmapped.
     */
    fun getAreaCodeLocation(digits: String): String? {
        val clean = extractNationalDigits(digits)
        if (clean.length < 3) return null
        val areaCode = clean.take(3)
        return US_AREA_CODES[areaCode]
    }

    private val US_AREA_CODES = mapOf(
        "201" to "New Jersey (Jersey City)",
        "202" to "Washington, DC (Capital District)",
        "203" to "Connecticut (Bridgeport, New Haven)",
        "205" to "Alabama (Birmingham)",
        "206" to "Washington (Seattle)",
        "207" to "Maine (Statewide)",
        "208" to "Idaho (Boise)",
        "209" to "California (Stockton, Modesto)",
        "210" to "Texas (San Antonio)",
        "212" to "New York, NY (Manhattan)",
        "213" to "California (Downtown Los Angeles)",
        "214" to "Texas (Dallas)",
        "215" to "Pennsylvania (Philadelphia)",
        "216" to "Ohio (Cleveland)",
        "217" to "Illinois (Springfield)",
        "218" to "Minnesota (Duluth)",
        "219" to "Indiana (Gary, Hammond)",
        "224" to "Illinois (Waukegan, Elgin)",
        "225" to "Louisiana (Baton Rouge)",
        "228" to "Mississippi (Gulfport, Biloxi)",
        "229" to "Georgia (Albany)",
        "231" to "Michigan (Muskegon)",
        "234" to "Ohio (Akron, Canton)",
        "239" to "Florida (Cape Coral, Fort Myers)",
        "240" to "Maryland (Bethesda, Silver Spring)",
        "248" to "Michigan (Troy, Southfield)",
        "251" to "Alabama (Mobile)",
        "252" to "North Carolina (Greenville)",
        "253" to "Washington (Tacoma)",
        "254" to "Texas (Killeen, Waco)",
        "256" to "Alabama (Huntsville)",
        "260" to "Indiana (Fort Wayne)",
        "262" to "Wisconsin (Kenosha, Racine)",
        "267" to "Pennsylvania (Philadelphia)",
        "269" to "Michigan (Kalamazoo)",
        "270" to "Kentucky (Bowling Green)",
        "272" to "Pennsylvania (Scranton)",
        "276" to "Virginia (Bristol)",
        "281" to "Texas (Houston)",
        "301" to "Maryland (Rockville, Frederick)",
        "302" to "Delaware (Statewide)",
        "303" to "Colorado (Denver)",
        "304" to "West Virginia (Statewide)",
        "305" to "Florida (Miami, Key West)",
        "307" to "Wyoming (Statewide)",
        "308" to "Nebraska (Grand Island)",
        "309" to "Illinois (Peoria)",
        "310" to "California (Santa Monica, Beverly Hills)",
        "312" to "Illinois (Downtown Chicago)",
        "313" to "Michigan (Detroit)",
        "314" to "Missouri (St. Louis)",
        "315" to "New York (Syracuse)",
        "316" to "Kansas (Wichita)",
        "317" to "Indiana (Indianapolis)",
        "318" to "Louisiana (Shreveport)",
        "319" to "Iowa (Cedar Rapids)",
        "320" to "Minnesota (St. Cloud)",
        "321" to "Florida (Orlando, Cape Canaveral)",
        "323" to "California (Los Angeles, Hollywood)",
        "325" to "Texas (Abilene, San Angelo)",
        "330" to "Ohio (Akron, Youngstown)",
        "331" to "Illinois (Aurora, Naperville)",
        "332" to "New York, NY (Manhattan)",
        "334" to "Alabama (Montgomery)",
        "336" to "North Carolina (Greensboro)",
        "337" to "Louisiana (Lafayette)",
        "339" to "Massachusetts (Boston suburbs)",
        "346" to "Texas (Houston)",
        "347" to "New York, NY (Brooklyn, Queens)",
        "351" to "Massachusetts (Lowell)",
        "352" to "Florida (Gainesville, Ocala)",
        "360" to "Washington (Olympia, Vancouver)",
        "361" to "Texas (Corpus Christi)",
        "380" to "Ohio (Columbus)",
        "385" to "Utah (Salt Lake City)",
        "386" to "Florida (Daytona Beach)",
        "401" to "Rhode Island (Providence)",
        "402" to "Nebraska (Omaha, Lincoln)",
        "404" to "Georgia (Atlanta)",
        "405" to "Oklahoma (Oklahoma City)",
        "406" to "Montana (Statewide)",
        "407" to "Florida (Orlando)",
        "408" to "California (San Jose, Silicon Valley)",
        "409" to "Texas (Beaumont, Galveston)",
        "410" to "Maryland (Baltimore, Annapolis)",
        "412" to "Pennsylvania (Pittsburgh)",
        "413" to "Massachusetts (Springfield)",
        "414" to "Wisconsin (Milwaukee)",
        "415" to "California (San Francisco)",
        "417" to "Missouri (Springfield)",
        "419" to "Ohio (Toledo)",
        "423" to "Tennessee (Chattanooga)",
        "424" to "California (Torrance, Culver City)",
        "425" to "Washington (Bellevue, Redmond)",
        "430" to "Texas (Tyler, Longview)",
        "432" to "Texas (Midland, Odessa)",
        "434" to "Virginia (Charlottesville)",
        "435" to "Utah (St. George, Logan)",
        "440" to "Ohio (Parma, Lorain)",
        "442" to "California (Oceanside, Palm Springs)",
        "443" to "Maryland (Baltimore)",
        "445" to "Pennsylvania (Philadelphia)",
        "458" to "Oregon (Eugene)",
        "463" to "Indiana (Indianapolis)",
        "469" to "Texas (Dallas, Plano)",
        "470" to "Georgia (Atlanta)",
        "475" to "Connecticut (New Haven)",
        "478" to "Georgia (Macon)",
        "479" to "Arkansas (Fort Smith, Fayetteville)",
        "480" to "Arizona (Mesa, Scottsdale)",
        "484" to "Pennsylvania (Allentown, Reading)",
        "501" to "Arkansas (Little Rock)",
        "502" to "Kentucky (Louisville)",
        "503" to "Oregon (Portland, Salem)",
        "504" to "Louisiana (New Orleans)",
        "505" to "New Mexico (Albuquerque)",
        "507" to "Minnesota (Rochester)",
        "508" to "Massachusetts (Worcester)",
        "509" to "Washington (Spokane)",
        "510" to "California (Oakland, Berkeley)",
        "512" to "Texas (Austin)",
        "513" to "Ohio (Cincinnati)",
        "515" to "Iowa (Des Moines)",
        "516" to "New York (Long Island, Nassau)",
        "517" to "Michigan (Lansing)",
        "518" to "New York (Albany)",
        "520" to "Arizona (Tucson)",
        "530" to "California (Redding, Chico)",
        "534" to "Wisconsin (Eau Claire)",
        "539" to "Oklahoma (Tulsa)",
        "540" to "Virginia (Roanoke, Harrisonburg)",
        "541" to "Oregon (Eugene, Bend)",
        "551" to "New Jersey (Jersey City)",
        "559" to "California (Fresno)",
        "561" to "Florida (West Palm Beach)",
        "562" to "California (Long Beach)",
        "563" to "Iowa (Davenport)",
        "564" to "Washington (Seattle Metro)",
        "567" to "Ohio (Toledo)",
        "570" to "Pennsylvania (Scranton)",
        "571" to "Virginia (Arlington, Alexandria)",
        "573" to "Missouri (Columbia, Jefferson City)",
        "574" to "Indiana (South Bend)",
        "575" to "New Mexico (Las Cruces)",
        "580" to "Oklahoma (Lawton)",
        "585" to "New York (Rochester)",
        "586" to "Michigan (Warren, Sterling Heights)",
        "601" to "Mississippi (Jackson)",
        "602" to "Arizona (Phoenix)",
        "603" to "New Hampshire (Statewide)",
        "605" to "South Dakota (Statewide)",
        "606" to "Kentucky (Ashland)",
        "607" to "New York (Binghamton, Ithaca)",
        "608" to "Wisconsin (Madison)",
        "609" to "New Jersey (Trenton, Princeton)",
        "610" to "Pennsylvania (Allentown)",
        "612" to "Minnesota (Minneapolis)",
        "614" to "Ohio (Columbus)",
        "615" to "Tennessee (Nashville)",
        "616" to "Michigan (Grand Rapids)",
        "617" to "Massachusetts (Boston, Cambridge)",
        "618" to "Illinois (Carbondale)",
        "619" to "California (San Diego)",
        "620" to "Kansas (Hutchinson)",
        "623" to "Arizona (Glendale, Peoria)",
        "626" to "California (Pasadena)",
        "628" to "California (San Francisco)",
        "629" to "Tennessee (Nashville)",
        "630" to "Illinois (Naperville, Wheaton)",
        "631" to "New York (Long Island, Suffolk)",
        "636" to "Missouri (St. Charles)",
        "646" to "New York, NY (Manhattan)",
        "650" to "California (San Mateo, Palo Alto)",
        "651" to "Minnesota (St. Paul)",
        "657" to "California (Anaheim, Fullerton)",
        "660" to "Missouri (Sedalia)",
        "661" to "California (Bakersfield)",
        "662" to "Mississippi (Tupelo)",
        "667" to "Maryland (Baltimore)",
        "669" to "California (San Jose)",
        "678" to "Georgia (Atlanta)",
        "681" to "West Virginia (Charleston)",
        "682" to "Texas (Fort Worth, Arlington)",
        "701" to "North Dakota (Statewide)",
        "702" to "Nevada (Las Vegas)",
        "703" to "Virginia (Arlington, Fairfax)",
        "704" to "North Carolina (Charlotte)",
        "706" to "Georgia (Augusta, Columbus)",
        "707" to "California (Santa Rosa, Napa)",
        "708" to "Illinois (Cicero, Oak Park)",
        "712" to "Iowa (Sioux City)",
        "713" to "Texas (Houston)",
        "714" to "California (Anaheim, Santa Ana)",
        "715" to "Wisconsin (Eau Claire, Wausau)",
        "716" to "New York (Buffalo, Niagara Falls)",
        "717" to "Pennsylvania (Harrisburg, Lancaster)",
        "718" to "New York, NY (Brooklyn, Queens, Bronx)",
        "719" to "Colorado (Colorado Springs)",
        "720" to "Colorado (Denver)",
        "724" to "Pennsylvania (New Castle)",
        "725" to "Nevada (Las Vegas)",
        "727" to "Florida (St. Petersburg, Clearwater)",
        "731" to "Tennessee (Jackson)",
        "732" to "New Jersey (New Brunswick, Edison)",
        "734" to "Michigan (Ann Arbor)",
        "737" to "Texas (Austin)",
        "740" to "Ohio (Newark, Zanesville)",
        "747" to "California (Burbank, San Fernando)",
        "754" to "Florida (Fort Lauderdale, Hollywood)",
        "757" to "Virginia (Virginia Beach, Norfolk)",
        "760" to "California (Palm Springs, Oceanside)",
        "762" to "Georgia (Augusta, Athens)",
        "763" to "Minnesota (Brooklyn Park)",
        "765" to "Indiana (Muncie, Lafayette)",
        "769" to "Mississippi (Jackson)",
        "770" to "Georgia (Atlanta suburbs)",
        "772" to "Florida (Port St. Lucie)",
        "773" to "Illinois (Chicago)",
        "774" to "Massachusetts (Worcester)",
        "775" to "Nevada (Reno, Carson City)",
        "779" to "Illinois (Rockford, Joliet)",
        "781" to "Massachusetts (Waltham, Lynn)",
        "785" to "Kansas (Topeka, Lawrence)",
        "786" to "Florida (Miami)",
        "801" to "Utah (Salt Lake City, Provo)",
        "802" to "Vermont (Statewide)",
        "803" to "South Carolina (Columbia)",
        "804" to "Virginia (Richmond)",
        "805" to "California (Santa Barbara, Ventura)",
        "806" to "Texas (Lubbock, Amarillo)",
        "808" to "Hawaii (Honolulu)",
        "810" to "Michigan (Flint)",
        "812" to "Indiana (Evansville)",
        "813" to "Florida (Tampa)",
        "814" to "Pennsylvania (Erie)",
        "815" to "Illinois (Rockford, Joliet)",
        "816" to "Missouri (Kansas City)",
        "817" to "Texas (Fort Worth, Arlington)",
        "818" to "California (Glendale, Burbank)",
        "828" to "North Carolina (Asheville)",
        "830" to "Texas (New Braunfels)",
        "831" to "California (Salinas, Monterey)",
        "832" to "Texas (Houston)",
        "843" to "South Carolina (Charleston)",
        "845" to "New York (Poughkeepsie)",
        "847" to "Illinois (Elgin, Waukegan)",
        "848" to "New Jersey (New Brunswick)",
        "850" to "Florida (Tallahassee, Pensacola)",
        "854" to "South Carolina (Charleston)",
        "856" to "New Jersey (Camden, Cherry Hill)",
        "857" to "Massachusetts (Boston, Cambridge)",
        "858" to "California (San Diego, La Jolla)",
        "859" to "Kentucky (Lexington)",
        "860" to "Connecticut (Hartford)",
        "862" to "New Jersey (Newark, Paterson)",
        "863" to "Florida (Lakeland)",
        "864" to "South Carolina (Greenville)",
        "865" to "Tennessee (Knoxville)",
        "870" to "Arkansas (Jonesboro)",
        "872" to "Illinois (Chicago)",
        "901" to "Tennessee (Memphis)",
        "903" to "Texas (Tyler, Longview)",
        "904" to "Florida (Jacksonville)",
        "906" to "Michigan (Marquette)",
        "907" to "Alaska (Anchorage)",
        "908" to "New Jersey (Elizabeth, Plainfield)",
        "909" to "California (San Bernardino, Ontario)",
        "910" to "North Carolina (Fayetteville)",
        "912" to "Georgia (Savannah)",
        "913" to "Kansas (Overland Park, Kansas City)",
        "914" to "New York (Yonkers, White Plains)",
        "915" to "Texas (El Paso)",
        "916" to "California (Sacramento)",
        "917" to "New York, NY (NYC All Boroughs)",
        "918" to "Oklahoma (Tulsa)",
        "919" to "North Carolina (Raleigh, Durham)",
        "920" to "Wisconsin (Green Bay, Appleton)",
        "925" to "California (Concord, Walnut Creek)",
        "928" to "Arizona (Flagstaff, Yuma)",
        "929" to "New York, NY (Brooklyn, Queens)",
        "931" to "Tennessee (Clarksville)",
        "934" to "New York (Long Island)",
        "936" to "Texas (Huntsville, Conroe)",
        "937" to "Ohio (Dayton, Springfield)",
        "938" to "Alabama (Huntsville)",
        "940" to "Texas (Denton, Wichita Falls)",
        "941" to "Florida (Sarasota, Bradenton)",
        "947" to "Michigan (Troy)",
        "949" to "California (Irvine, Newport Beach)",
        "951" to "California (Riverside, Corona)",
        "952" to "Minnesota (Bloomington)",
        "954" to "Florida (Fort Lauderdale)",
        "956" to "Texas (Laredo, McAllen)",
        "959" to "Connecticut (Hartford)",
        "970" to "Colorado (Fort Collins, Grand Junction)",
        "971" to "Oregon (Portland, Salem)",
        "972" to "Texas (Dallas, Plano, Irving)",
        "973" to "New Jersey (Newark, Paterson)",
        "978" to "Massachusetts (Lowell, Lawrence)",
        "979" to "Texas (College Station, Bryan)",
        "980" to "North Carolina (Charlotte)",
        "984" to "North Carolina (Raleigh, Durham)",
        "985" to "Louisiana (Houma, Slidell)",
        "989" to "Michigan (Saginaw, Midland)"
    )

    /**
     * Extracts national digits (up to 10 digits) from a raw input string,
     * ignoring "+1", parentheses, spaces, and dashes.
     */
    fun extractNationalDigits(input: String): String {
        var cleaned = input.trim().replace("[^0-9]".toRegex(), "")
        // If it starts with country code 1, strip it to get the national number
        if (cleaned.startsWith("1") && cleaned.length > 10) {
            cleaned = cleaned.substring(1)
        } else if (cleaned.startsWith("1") && cleaned.length == 1) {
            cleaned = ""
        }
        return cleaned.take(10)
    }

    /**
     * Formats national digits into clean North American standard phone representation:
     * e.g. "+1 (202) 555-0143"
     */
    fun formatNanpDisplay(digits: String): String {
        val clean = digits.take(10)
        return when {
            clean.isEmpty() -> "+1"
            clean.length <= 3 -> "+1 ($clean"
            clean.length <= 6 -> "+1 (${clean.substring(0, 3)}) ${clean.substring(3)}"
            else -> "+1 (${clean.substring(0, 3)}) ${clean.substring(3, 6)}-${clean.substring(6)}"
        }
    }

    /**
     * Validates whether national digits form a legitimate North American Numbering Plan number.
     * US numbers require 10 digits where:
     * - Area code (digits 0-2) cannot start with 0 or 1
     * - Central office code (digits 3-5) cannot start with 0 or 1
     */
    fun validateNanp(digits: String): PhoneValidation {
        val clean = digits.take(10)
        val formatted = formatNanpDisplay(clean)
        val e164 = if (clean.length == 10) "+1$clean" else ""

        return when {
            clean.isEmpty() -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Enter 10-digit US phone number"
            )
            clean.length < 10 -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Need ${10 - clean.length} more digits"
            )
            clean[0] in listOf('0', '1') -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Area code cannot start with 0 or 1"
            )
            clean[3] in listOf('0', '1') -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Exchange code cannot start with 0 or 1"
            )
            else -> PhoneValidation(
                isValid = true,
                formatted = formatted,
                e164 = e164,
                message = "Valid US Number"
            )
        }
    }

    /**
     * Normalizes a user-input phone number to E.164 US format (+1XXXXXXXXXX).
     * If user enters a 10-digit number without country code, prepends "+1".
     * Strips whitespace, dashes, parentheses.
     */
    fun normalizePhoneNumber(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val cleaned = trimmed.replace("[^0-9+]".toRegex(), "")
        return when {
            cleaned.startsWith("+1") -> cleaned
            cleaned.startsWith("1") && cleaned.length == 11 -> "+$cleaned"
            cleaned.startsWith("+") -> cleaned
            else -> "+1$cleaned"
        }
    }

    /**
     * Formats duration in seconds to a human-readable string (e.g. "2m 15s", "45s", "0s").
     */
    fun formatDuration(seconds: Long?): String {
        if (seconds == null || seconds <= 0) return "0s"
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}m ${remainingSeconds}s"
        } else {
            "${remainingSeconds}s"
        }
    }

    /**
     * Formats epoch timestamp in milliseconds to human-readable format.
     */
    fun formatTimestamp(millis: Long): String {
        if (millis <= 0) return "Unknown"
        val now = System.currentTimeMillis()
        val diff = now - millis

        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return when {
            diff < 24 * 60 * 60 * 1000 && isSameDay(now, millis) -> {
                "Today, ${timeFormat.format(Date(millis))}"
            }
            diff < 48 * 60 * 60 * 1000 && isYesterday(now, millis) -> {
                "Yesterday, ${timeFormat.format(Date(millis))}"
            }
            else -> {
                dateFormat.format(Date(millis))
            }
        }
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val f = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return f.format(Date(t1)) == f.format(Date(t2))
    }

    private fun isYesterday(now: Long, target: Long): Boolean {
        val dayMillis = 24 * 60 * 60 * 1000
        val f = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return f.format(Date(now - dayMillis)) == f.format(Date(target))
    }
}
