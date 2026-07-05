import Foundation

public enum LocationCatalog {
    public static let locations: [String] = [
        "IL-ירושלים", "IL-תל אביב", "IL-חיפה", "IL-באר שבע", "IL-אילת", 
        // " ",
        // "Geo/ GPS-Lat, Lon", 
        " ", 
        "IL-אשדוד", "IL-אשקלון", "IL-בית שמש", "IL-ביתר עילית",
        "IL-בני ברק", "IL-בת ים", "IL-הרצליה", "IL-חדרה", "IL-חולון", "IL-טבריה",
        "IL-יבנה", "IL-כפר סבא", "IL-לוד", "IL-מודיעין", "IL-מודיעין עילית",
        "IL-מצפה רמון", "IL-נצרת", "IL-נתניה", "IL-פתח תקוה", "IL-צפת",
        "IL-ראשון לציון", "IL-רמלה", "IL-רמת גן", "IL-רעננה", " ",
        "US-Atlanta-GA", "US-Austin-TX", "US-Baltimore-MD", "US-Boston-MA",
        "US-Chicago-IL", "US-Cleveland-OH", "US-Dallas-TX", "US-Denver-CO",
        "US-Los Angeles-CA", "US-Miami-FL", "US-New York-NY", "US-Philadelphia-PA",
        "US-San Diego-CA", "US-San Francisco-CA", "US-Seattle-WA", "US-Washington-DC",
        " ", "CA-Montreal", "CA-Toronto", "CA-Vancouver", " ", "FR-Paris",
        "GB-London", "AU-Melbourne", "AU-Sydney", "DE-Berlin", "NL-Amsterdam"
    ]

    private static let toHebrew: [String: String] = [
        "IL-Jerusalem": "IL-ירושלים", "IL-Tel Aviv": "IL-תל אביב", "IL-Haifa": "IL-חיפה",
        "IL-Eilat": "IL-אילת", "IL-Be'er Sheva": "IL-באר שבע", "IL-Ashdod": "IL-אשדוד",
        "IL-Ashkelon": "IL-אשקלון", "IL-Bat Yam": "IL-בת ים", "IL-Beit Shemesh": "IL-בית שמש",
        "IL-Bnei Brak": "IL-בני ברק", "IL-Hadera": "IL-חדרה", "IL-Herzliya": "IL-הרצליה",
        "IL-Holon": "IL-חולון", "IL-Kfar Saba": "IL-כפר סבא", "IL-Lod": "IL-לוד",
        "IL-Modiin": "IL-מודיעין", "IL-Nazareth": "IL-נצרת", "IL-Netanya": "IL-נתניה",
        "IL-Petach Tikvah": "IL-פתח תקוה", "IL-Ra'anana": "IL-רעננה", "IL-Ramat Gan": "IL-רמת גן",
        "IL-Ramla": "IL-רמלה", "IL-Rishon LeZion": "IL-ראשון לציון", "IL-Tiberias": "IL-טבריה",
        "IL-Yavne": "IL-יבנה", "IL-Mitzpe Ramon": "IL-מצפה רמון", "IL-Modiin Ilit": "IL-מודיעין עילית",
        "IL-Betar Ilit": "IL-ביתר עילית", "IL-Zefat": "IL-צפת"
    ]

    private static let toEnglish = Dictionary(uniqueKeysWithValues: toHebrew.map { ($0.value, $0.key) })

    public static func convertLocationIL(_ location: String) -> String {
        toHebrew.first { location.hasPrefix($0.key) }?.value ?? location
    }

    public static func convertFromLocationIL(_ location: String) -> String {
        toEnglish[location] ?? location
    }
}
