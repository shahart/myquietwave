import Foundation

public struct AppSettings: Equatable {
    public static let defaultNextHours = "17, 21, 7, 12, 15, 18"
    public static let defaultTodo = "פלטה, מיחם, שעון שבת, מנורה קטנה במסדרון, מזגן"

    public var stationName: String
    public var location: String
    public var newsDurationMinutes: Int
    public var nextHours: String
    public var continuousRadio: Bool
    public var todoList: String

    public init(
        stationName: String = RadioStation.all[0].name,
        location: String = "IL-Jerusalem",
        newsDurationMinutes: Int = 4,
        nextHours: String = Self.defaultNextHours,
        continuousRadio: Bool = false,
        todoList: String = Self.defaultTodo
    ) {
        self.stationName = stationName
        self.location = location
        self.newsDurationMinutes = max(1, min(59, newsDurationMinutes))
        self.nextHours = nextHours.isEmpty ? Self.defaultNextHours : nextHours
        self.continuousRadio = continuousRadio
        self.todoList = todoList
    }
}

public enum SettingsStore {
    private enum Key {
        static let stationName = "stationName"
        static let location = "location"
        static let newsDurationMinutes = "newsDurationMinutes"
        static let nextHours = "nextHours"
        static let continuousRadio = "continuousRadio"
        static let todoList = "todoList"
    }

    public static func load(from defaults: UserDefaults = .standard) -> AppSettings {
        AppSettings(
            stationName: defaults.string(forKey: Key.stationName) ?? RadioStation.all[0].name,
            location: defaults.string(forKey: Key.location) ?? "IL-Jerusalem",
            newsDurationMinutes: defaults.object(forKey: Key.newsDurationMinutes) as? Int ?? 4,
            nextHours: defaults.string(forKey: Key.nextHours) ?? AppSettings.defaultNextHours,
            continuousRadio: defaults.bool(forKey: Key.continuousRadio),
            todoList: defaults.string(forKey: Key.todoList) ?? AppSettings.defaultTodo
        )
    }

    public static func save(_ settings: AppSettings, to defaults: UserDefaults = .standard) {
        defaults.set(settings.stationName, forKey: Key.stationName)
        defaults.set(settings.location, forKey: Key.location)
        defaults.set(settings.newsDurationMinutes, forKey: Key.newsDurationMinutes)
        defaults.set(settings.nextHours, forKey: Key.nextHours)
        defaults.set(settings.continuousRadio, forKey: Key.continuousRadio)
        defaults.set(settings.todoList, forKey: Key.todoList)
    }
}
