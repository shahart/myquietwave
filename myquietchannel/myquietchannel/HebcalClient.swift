import Foundation

public final class HebcalClient {
    private let baseURL = URL(string: "https://www.hebcal.com/")!
    private let session: URLSession

    public init(session: URLSession = .shared) {
        self.session = session
    }

    public func loadInfo(location: String, date: Date = Date()) async throws -> JewishInfo {
        async let shabbat = requestHebcal(endpoint: "shabbat", location: location)
        async let zmanim = requestZmanim(location: location)
        async let daf = requestDafYomi(date: date)

        return try await JewishInfoBuilder.build(
            shabbat: shabbat,
            zmanim: zmanim,
            dafYomi: daf
        )
    }

    private func requestHebcal(endpoint: String, location: String) async throws -> HebcalResponse {
        let url = hebcalURL(endpoint: endpoint, location: location)
        return try await decode(HebcalResponse.self, from: url)
    }

    private func requestZmanim(location: String) async throws -> ZmanimResponse {
        let url = hebcalURL(endpoint: "zmanim", location: location)
        return try await decode(ZmanimResponse.self, from: url)
    }

    private func requestDafYomi(date: Date) async throws -> HebcalResponse {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.dateFormat = "yyyy-MM-dd"
        let day = formatter.string(from: date)
        var components = URLComponents(url: baseURL.appendingPathComponent("hebcal"), resolvingAgainstBaseURL: false)!
        components.queryItems = [
            URLQueryItem(name: "v", value: "1"),
            URLQueryItem(name: "cfg", value: "json"),
            URLQueryItem(name: "F", value: "on"),
            URLQueryItem(name: "myomi", value: "on"),
            URLQueryItem(name: "nyomi", value: "on"),
            URLQueryItem(name: "dty", value: "on"),
            URLQueryItem(name: "dps", value: "on"),
            URLQueryItem(name: "o", value: "on"),
            URLQueryItem(name: "start", value: day),
            URLQueryItem(name: "end", value: day)
        ]
        return try await decode(HebcalResponse.self, from: components.url!)
    }

    private func hebcalURL(endpoint: String, location: String) -> URL {
        var components = URLComponents(url: baseURL.appendingPathComponent(endpoint), resolvingAgainstBaseURL: false)!
        var query = [URLQueryItem(name: "cfg", value: "json"), URLQueryItem(name: "ue", value: useElevation(location))]
        let clean = city(location)
        let parts = location.split(separator: ",").map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }

        if parts.count >= 2, Double(parts[0]) != nil, Double(parts[1]) != nil {
            query.append(URLQueryItem(name: "latitude", value: parts[0]))
            query.append(URLQueryItem(name: "longitude", value: parts[1]))
        } else if let geoname = specialGeoNameId(for: location) {
            query.append(URLQueryItem(name: "geonameid", value: geoname))
        } else if Double(clean) != nil {
            query.append(URLQueryItem(name: "geonameid", value: clean))
        } else {
            query.append(URLQueryItem(name: "city", value: clean))
        }
        components.queryItems = query
        return components.url!
    }

    private func decode<T: Decodable>(_ type: T.Type, from url: URL) async throws -> T {
        let (data, response) = try await session.data(from: url)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw URLError(.badServerResponse)
        }
        return try JSONDecoder().decode(type, from: data)
    }

    private func useElevation(_ location: String) -> String {
        location.lowercased().contains(", ue") || location.lowercased().contains(",ue") ? "off" : "on"
    }

    private func city(_ location: String) -> String {
        location.split(separator: ",").first.map(String.init)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? location
    }

    private func specialGeoNameId(for location: String) -> String? {
        let lower = location.lowercased()
        if lower.contains("il-yavne") { return "293222" }
        if lower.contains("il-mitzpe ramon") { return "294166" }
        if lower.contains("il-zefat") { return "293100" }
        if lower.contains("il-modiin ilit") { return "8199378" }
        if lower.contains("il-betar ilit") { return "284375" }
        return nil
    }
}

public enum JewishInfoBuilder {
    public static func build(shabbat: HebcalResponse, zmanim: ZmanimResponse, dafYomi: HebcalResponse) -> JewishInfo {
        var info = JewishInfo()
        for item in shabbat.items {
            switch item.category {
            case "candles":
                info.shabbatTimes += "\nCandle Lighting 🕯️ \(DateUtilities.truncateHebcalTime(item.date))\n"
            case "havdalah":
                info.shabbatTimes += "\nHavdalah 🌠 \(DateUtilities.truncateHebcalTime(item.date))\n"
            case "mevarchim":
                let molad = item.memo?.replacingOccurrences(of: "chalakim", with: "חלקים")
                    .replacingOccurrences(of: "and", with: "ו-") ?? ""
                info.shabbatTimes += "\n\(item.hebrew)\nהמולד: \(molad)\n"
            case "parashat":
                info.parasha = " שבת \(item.hebrew)"
                if let haftarah = item.leyning?.haftarah {
                    info.haftarah = " הפטרה \(convertBooks(haftarah.replacingOccurrences(of: "|", with: "\n")))"
                }
                if let sephardic = item.leyning?.haftarahSephardic {
                    info.haftarahSephardic = " הפטרה ספרדים \(convertBooks(sephardic.replacingOccurrences(of: "|", with: "\n")))"
                }
            case "roshchodesh":
                info.roshChodesh += "\(item.hebrew) - \(DateUtilities.switchDate(item.date))\n"
            case "holiday":
                info.holidays += "\n\(item.hebrew) - \(DateUtilities.switchDate(item.date))"
                if let memo = item.memo, !info.holidayMemo.contains(memo) {
                    info.holidayMemo += "\n\n\(item.hebrew): \(memo)"
                }
            default:
                if item.title == "Fast begins" {
                    info.fastTimes += " זמני התענית: עלות השחר \(DateUtilities.truncateHebcalTime(item.date))"
                } else if item.title == "Fast ends" {
                    info.fastTimes += " צאת הכוכבים \(DateUtilities.truncateHebcalTime(item.date))"
                }
            }
        }

        for item in dafYomi.items {
            switch item.category {
            case "dafyomi":
                info.dafYomi = item.hebrew
            case "mishnayomi":
                info.extraDailyLearning += "משנה יומית: \(item.hebrew)\n"
            case "nachyomi":
                info.extraDailyLearning += "נ'ך יומי: \(item.hebrew)\n"
            case "dailyPsalms":
                info.extraDailyLearning += "תהלים יומי: \(item.hebrew)\n"
            case "tanakhYomi":
                info.extraDailyLearning += "תנ'ך יומי: \(item.hebrew)\n"
            case "omer":
                info.omer = "ספירת העומר (בבוקר):\n\(item.hebrew.replacingOccurrences(of: "עומר", with: ""))"
            default:
                break
            }
        }

        info.locationTitle = zmanim.location.title
        info.sunTimes = "\nSunrise 🔆 \(DateUtilities.truncateHebcalTime(zmanim.times.sunrise))\nSunset 🌅 \(DateUtilities.truncateHebcalTime(zmanim.times.sunset))"
        info.zmanimDetails = zmanimDetails(zmanim.times)
        return info
    }

    private static func zmanimDetails(_ times: ZmanimTimes) -> String {
        [
            ("chatzot Night חצות הלילה", times.chatzotNight),
            ("alot HaShahar עלות השחר", times.alotHaShachar),
            ("dawn", times.dawn),
            ("sof Zman Shma מגן אברהם", times.sofZmanShmaMGA),
            ("sof Zman Shma", times.sofZmanShma),
            ("sof Zman Tfilla מגן אברהם", times.sofZmanTfillaMGA),
            ("sof Zman Tfilla", times.sofZmanTfilla),
            ("chatzot חצות היום", times.chatzot),
            ("mincha Gedola מנחה גדולה", times.minchaGedola),
            ("mincha Ketana מנחה קטנה", times.minchaKetana),
            ("plag HaMincha פלג המנחה", times.plagHaMincha),
            ("bein HaShmashos בין השמשות", times.beinHaShmashos),
            ("Dusk חשיכה", times.dusk),
            ("Tzeit צאת הכוכבים", times.tzeit7083deg),
            ("Tzeit 72' צאת הכוכבים רבינו תם", times.tzeit72min)
        ]
        .compactMap { label, value in
            guard let value else { return nil }
            return "\(label): \(DateUtilities.truncateHebcalTime(value))"
        }
        .joined(separator: "\n")
    }

    private static func convertBooks(_ text: String) -> String {
        var value = text
        [
            "Joshua": "יהושע", "Judges": "שופטים", "I Samuel": "שמואל א", "II Samuel": "שמואל ב",
            "I Kings": "מלכים א", "II Kings": "מלכים ב", "Isaiah": "ישעיהו", "Jeremiah": "ירמיהו",
            "Ezekiel": "יחזקאל", "Hosea": "הושע", "Joel": "יואל", "Amos": "עמוס",
            "Obadiah": "עובדיה", "Jonah": "יונה", "Micah": "מיכה", "Nachum": "נחום",
            "Habakkuk": "חבקוק", "Zephaniah": "צפניה", "Haggai": "חגי", "Zechariah": "זכריה",
            "Malachi": "מלאכי"
        ].forEach { value = value.replacingOccurrences(of: $0.key, with: $0.value) }
        return value
    }
}
