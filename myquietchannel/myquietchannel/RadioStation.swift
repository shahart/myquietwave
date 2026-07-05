import Foundation

public struct RadioStation: Identifiable, Hashable, Codable {
    public let name: String
    public let streamURL: URL

    public var id: String { name }

    public static let all: [RadioStation] = [
        .init(name: "גלי צהל", streamURL: URL(string: "https://glzwizzlv.bynetcdn.com/glz_mp3")!),
        .init(name: "גלגלצ", streamURL: URL(string: "https://glzwizzlv.bynetcdn.com/glglz_mp3")!),
        .init(name: "רשת ב", streamURL: URL(string: "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_BET.mp3")!),
        .init(name: "רשת ג", streamURL: URL(string: "https://playerservices.streamtheworld.com/api/livestream-redirect/KAN_GIMMEL.mp3")!),
        .init(name: "FM102", streamURL: URL(string: "https://cdn88.mediacast.co.il/102fm-tlv/102fm_mp3/icecast.audio")!),
        .init(name: "גלי ישראל", streamURL: URL(string: "https://cdn.cybercdn.live/Galei_Israel/Live/icecast.audio")!),
        .init(name: "כאן 88", streamURL: URL(string: "https://27863.live.streamtheworld.com/KAN_88.mp3")!),
        .init(name: "קול חי", streamURL: URL(string: "https://live.kcm.fm/live-new")!),
        .init(name: "קול חי מיוזיק", streamURL: URL(string: "https://live.kcm.fm/livemusic")!),
        .init(name: "קול ברמה", streamURL: URL(string: "https://cdn.cybercdn.live/Kol_Barama/Live_Audio/icecast.audio")!)
    ]

    public static func named(_ name: String) -> RadioStation {
        all.first { $0.name == name } ?? all[0]
    }
}
