import Foundation

public enum DateUtilities {
    private static let hebrewDays = ["א", "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י", "יא", "יב", "יג", "יד", "טו", "טז", "יז", "יח", "יט", "כ", "כא", "כב", "כג", "כד", "כה", "כו", "כז", "כח", "כט", "ל"]
    private static let hebrewMonths = ["תשרי", "חשון", "כסלו", "טבת", "שבט", "אדר", "אדר", "ניסן", "אייר", "סיוון", "תמוז", "אב", "אלול"]

    public static func truncateHebcalTime(_ value: String) -> String {
        guard let tRange = value.range(of: "T") else { return value }
        let start = value.index(after: tRange.lowerBound)
        let end = value.index(start, offsetBy: 5, limitedBy: value.endIndex) ?? value.endIndex
        var text = String(value[start..<end])
        if text.first == "0" {
            text.removeFirst()
        }
        return text
    }

    public static func switchDate(_ date: String) -> String {
        let parts = date.split(separator: "-")
        guard parts.count == 3 else { return date }
        return [parts[2], parts[1], parts[0]]
            .joined(separator: "-")
            .replacingOccurrences(of: "-0", with: "-")
            .trimmingCharacters(in: CharacterSet(charactersIn: "0"))
    }

    public static func hebrewYear(_ year: Int) -> String {
        var input = year
        let letters = ["ה'", "ד'", "ג'", "ב'", "א'", "ת", "ש", "ר", "ק", "צ", "פ", "ע", "ס", "נ", "מ", "ל", "כ", "י", "ט", "ח", "ז", "ו", "ה", "ד", "ג", "ב", "א"]
        let values = [5000, 4000, 3000, 2000, 1000, 400, 300, 200, 100, 90, 80, 70, 60, 50, 40, 30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        var output = ""
        while input > 0 {
            if input == 16 { return output + "טז" }
            if input == 15 { return output + "טו" }
            if let index = values.firstIndex(where: { input >= $0 }) {
                input -= values[index]
                output += letters[index]
            } else {
                break
            }
        }
        return output
    }

    public static func currentHebrewDate(reference: Date = Date(), calendar: Calendar = Calendar(identifier: .hebrew)) -> String {
        var calendar = calendar
        calendar.locale = Locale(identifier: "he_IL")
        let components = calendar.dateComponents([.year, .month, .day], from: reference)
        guard let year = components.year, let month = components.month, let day = components.day else {
            return ""
        }
        let monthName = month > 0 && month <= hebrewMonths.count ? hebrewMonths[month - 1] : ""
        let dayName = day > 0 && day <= hebrewDays.count ? hebrewDays[day - 1] : "\(day)"
        return "\(dayName) \(monthName) \(hebrewYear(year))"
    }

    public static func dayName(_ date: Date = Date()) -> String {
        let names = ["ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"]
        let weekday = Calendar.current.component(.weekday, from: date)
        return names[max(0, min(weekday - 1, names.count - 1))]
    }
}
