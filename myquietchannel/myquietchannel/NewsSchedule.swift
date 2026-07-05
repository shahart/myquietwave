import Foundation

public struct NewsSchedule: Equatable {
    public let entries: [DateComponents]

    public init(text: String) {
        entries = text
            .split(separator: ",")
            .compactMap { raw -> DateComponents? in
                let trimmed = raw.trimmingCharacters(in: .whitespacesAndNewlines)
                guard !trimmed.isEmpty else { return nil }
                let parts = trimmed.split(separator: ":")
                guard let hour = Int(parts[0]), (0...23).contains(hour) else { return nil }
                let minute = parts.count > 1 ? Int(parts[1]) ?? 0 : 0
                guard (0...59).contains(minute) else { return nil }
                return DateComponents(hour: hour, minute: minute)
            }
    }

    public func nextDate(after date: Date, calendar: Calendar = .current) -> Date? {
        guard !entries.isEmpty else { return nil }
        let candidates = entries.compactMap { entry in
            calendar.nextDate(
                after: date,
                matching: DateComponents(hour: entry.hour, minute: entry.minute, second: 0),
                matchingPolicy: .nextTime
            )
        }
        return candidates.min()
    }
}
