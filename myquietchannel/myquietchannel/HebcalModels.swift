import Foundation

public struct HebcalResponse: Decodable {
    public let items: [HebcalItem]
}

public struct HebcalItem: Decodable, Identifiable {
    public var id: String { "\(category)-\(date)-\(hebrew)" }
    public let category: String
    public let hebrew: String
    public let leyning: Leyning?
    public let memo: String?
    public let date: String
    public let title: String?
    public let link: String?
}

public struct Leyning: Decodable {
    public let haftarah: String?
    public let haftarahSephardic: String?

    enum CodingKeys: String, CodingKey {
        case haftarah
        case haftarahSephardic = "haftarah_sephardic"
    }
}

public struct ZmanimResponse: Decodable {
    public let location: HebcalLocation
    public let times: ZmanimTimes
}

public struct HebcalLocation: Decodable {
    public let title: String
}

public struct ZmanimTimes: Decodable {
    public let sunrise: String
    public let sunset: String
    public let beinHaShmashos: String?
    public let dusk: String?
    public let tzeit7083deg: String?
    public let tzeit72min: String?
    public let dawn: String?
    public let chatzot: String?
    public let chatzotNight: String?
    public let alotHaShachar: String?
    public let minchaGedola: String?
    public let plagHaMincha: String?
    public let minchaKetana: String?
    public let sofZmanShma: String?
    public let sofZmanTfilla: String?
    public let sofZmanShmaMGA: String?
    public let sofZmanTfillaMGA: String?
}

public struct JewishInfo: Equatable {
    public var parasha = ""
    public var haftarah = ""
    public var haftarahSephardic = ""
    public var shabbatTimes = ""
    public var dafYomi = ""
    public var extraDailyLearning = ""
    public var omer = ""
    public var locationTitle = ""
    public var sunTimes = ""
    public var zmanimDetails = ""
    public var roshChodesh = ""
    public var holidays = ""
    public var holidayMemo = ""
    public var fastTimes = ""
}
