import CoreLocation
import Combine
import SwiftUI

#if os(iOS)
public struct ContentView: View {
    @StateObject private var scheduler = RadioScheduler()
    @StateObject private var viewModel = ContentViewModel()
    @State private var settings = SettingsStore.load()
    @State private var showDailyLearning = false
    @State private var showZmanim = false
    @State private var showHolidayMemo = false

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    Text(scheduler.statusText)
                        .font(.system(size: 28, weight: .semibold))
                        .foregroundStyle(Color.headingText)
                        .multilineTextAlignment(.center)

                    controls
                    linksAndCalendar
                    details
                    todoAndShare
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 24)
            }
            .background(Color.screenBackground.ignoresSafeArea())
            .navigationBarTitleDisplayMode(.inline)
            .task { await viewModel.refresh(location: settings.location) }
            .onChange(of: settings) { newValue in
                SettingsStore.save(newValue)
            }
        }
    }

    private var controls: some View {
        VStack(spacing: 14) {
            Button {
                if case .stopped = scheduler.state {
                    normalizeSettings()
                    SettingsStore.save(settings)
                    scheduler.start(settings: settings)
                } else {
                    scheduler.stop()
                }
            } label: {
                Text(isRunning ? "Stop" : "Start")
                    .font(.headline)
                    .frame(maxWidth: .infinity, minHeight: 52)
            }
            .buttonStyle(.borderedProminent)
            .tint(isRunning ? .accentGreen : .buttonIdle)

            Text("Manually start your radio app, or listen to one of the radio stations from this app.")
                .font(.body)
                .foregroundStyle(Color.bodyText)
                .multilineTextAlignment(.center)

            Picker("Station", selection: $settings.stationName) {
                ForEach(RadioStation.all) { station in
                    Text(station.name).tag(station.name)
                }
            }
            .pickerStyle(.menu)
            .disabled(isRunning)

            Toggle("Ignore upcoming news and just play continuously", isOn: $settings.continuousRadio)
                .disabled(isRunning)

            TextField("Upcoming news", text: $settings.nextHours)
                .textFieldStyle(.roundedBorder)
                .multilineTextAlignment(.center)
                .disabled(isRunning || settings.continuousRadio)

            Stepper("News Length in Minutes: \(settings.newsDurationMinutes)", value: $settings.newsDurationMinutes, in: 1...59)
                .disabled(isRunning || settings.continuousRadio)

            if !scheduler.countdownText.isEmpty {
                Text(scheduler.countdownText)
                    .font(.system(.title2, design: .monospaced))
                    .foregroundStyle(Color.headingText)
            }
        }
        .padding(20)
        .background(Color.panelPrimary)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }

    private var linksAndCalendar: some View {
        VStack(spacing: 10) {
            Link("Useful links", destination: URL(string: "https://shahart.github.io/myquietwave/links.html")!)
                .foregroundStyle(Color.linkText)

            Text(Calendar.current.component(.weekday, from: Date()) == 6 ? "🕯🕯 Shabbat Shalom 🕯🕯" : "Enjoy")
                .font(.title2.bold())
                .foregroundStyle(Color.accentGreen)

            Text(DateUtilities.currentHebrewDate())
                .font(.title3)
                .foregroundStyle(Color.headingText)

            Text(Date(), style: .time)
                .font(.system(size: 28, design: .monospaced))
                .foregroundStyle(Color.headingText)

            Text(TimeZone.current.identifier)
                .foregroundStyle(Color.bodyText)

            Text("\(DateUtilities.dayName()) \(Date().formatted(date: .numeric, time: .omitted))")
                .foregroundStyle(Color.headingText)
        }
    }

    private var details: some View {
        VStack(spacing: 12) {
            if viewModel.isLoading {
                ProgressView()
            }

            linkedText(viewModel.info.parasha, url: wikiURL(for: viewModel.info.parasha))
            linkedText(viewModel.info.haftarah, url: URL(string: "https://shahart.github.io/heb-bible/index.html"))
            linkedText(viewModel.info.haftarahSephardic, url: URL(string: "https://shahart.github.io/heb-bible/index.html"))

            Text(viewModel.info.shabbatTimes)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.headingText)

            Button("הדף היומי") { showDailyLearning = true }
                .buttonStyle(.plain)
                .foregroundStyle(Color.headingText)

            Link(viewModel.info.dafYomi, destination: URL(string: "https://daf-yomi.com/Dafyomi_Page.aspx")!)
                .foregroundStyle(Color.linkText)

            Text(viewModel.info.locationTitle)
                .foregroundStyle(Color.headingText)

            Button(viewModel.info.sunTimes) { showZmanim = true }
                .buttonStyle(.plain)
                .foregroundStyle(Color.bodyText)
                .multilineTextAlignment(.center)

            Text(viewModel.info.roshChodesh + viewModel.info.holidays + "\n" + viewModel.info.fastTimes + "\n" + viewModel.info.omer)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.bodyText)

            if !viewModel.info.holidayMemo.isEmpty {
                Button("Holiday details") { showHolidayMemo = true }
            }

            TextField("Location", text: $settings.location)
                .textFieldStyle(.roundedBorder)
                .multilineTextAlignment(.center)
                .onSubmit { Task { await viewModel.refresh(location: settings.location) } }

            Picker("Location", selection: Binding(
                get: { LocationCatalog.convertLocationIL(settings.location) },
                set: {
                    if !$0.trimmingCharacters(in: .whitespaces).isEmpty, $0 != "Geo/ GPS-Lat, Lon" {
                        settings.location = LocationCatalog.convertFromLocationIL($0)
                        Task { await viewModel.refresh(location: settings.location) }
                    }
                }
            )) {
                ForEach(LocationCatalog.locations, id: \.self) { location in
                    Text(location).tag(location)
                }
            }
            .pickerStyle(.menu)
        }
        .padding(20)
        .background(Color.panelSurface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .alert("עוד לימודים יומיים", isPresented: $showDailyLearning) {
            Button("Close", role: .cancel) {}
        } message: {
            Text(viewModel.info.extraDailyLearning)
        }
        .alert("Zmanim", isPresented: $showZmanim) {
            Button("Close", role: .cancel) {}
        } message: {
            Text(viewModel.info.zmanimDetails)
        }
        .alert("Holiday details", isPresented: $showHolidayMemo) {
            Button("Close", role: .cancel) {}
        } message: {
            Text(viewModel.info.holidayMemo)
        }
    }

    private var todoAndShare: some View {
        VStack(spacing: 12) {
            Text("To do list 📝")
                .font(.headline)
                .foregroundStyle(Color.headingText)

            TextEditor(text: $settings.todoList)
                .frame(minHeight: 90)
                .scrollContentBackground(.hidden)
                .padding(8)
                .background(Color.inputBackground)
                .clipShape(RoundedRectangle(cornerRadius: 8))

            ShareLink(item: "Check out this nice app: https://apps.apple.com/") {
                Text("Share")
                    .frame(maxWidth: .infinity, minHeight: 52)
            }
            .buttonStyle(.bordered)
            .tint(.buttonIdle)
        }
    }

    private var isRunning: Bool {
        if case .stopped = scheduler.state { return false }
        return true
    }

    private func normalizeSettings() {
        settings.newsDurationMinutes = max(1, min(59, settings.newsDurationMinutes))
        if settings.nextHours.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            settings.nextHours = AppSettings.defaultNextHours
        }
    }

    @ViewBuilder
    private func linkedText(_ text: String, url: URL?) -> some View {
        if !text.isEmpty, let url {
            Link(text, destination: url)
                .foregroundStyle(Color.linkText)
                .multilineTextAlignment(.center)
        } else if !text.isEmpty {
            Text(text)
                .multilineTextAlignment(.center)
                .foregroundStyle(Color.headingText)
        }
    }

    private func wikiURL(for text: String) -> URL? {
        let clean = text.replacingOccurrences(of: " שבת ", with: "").trimmingCharacters(in: .whitespacesAndNewlines)
        guard !clean.isEmpty else { return nil }
        return URL(string: "https://he.wikipedia.org/wiki/\(clean.replacingOccurrences(of: " ", with: "_"))")
    }
}

@MainActor
final class ContentViewModel: ObservableObject {
    @Published var info = JewishInfo()
    @Published var isLoading = false

    private let client = HebcalClient()

    func refresh(location: String) async {
        isLoading = true
        defer { isLoading = false }
        do {
            info = try await client.loadInfo(location: location)
        } catch {
            info.locationTitle = "Unable to load Hebcal data"
        }
    }
}

private extension Color {
    static let screenBackground = Color(red: 0.96, green: 0.95, blue: 0.91)
    static let panelPrimary = Color(red: 0.99, green: 0.98, blue: 0.95)
    static let panelSurface = Color(red: 1.00, green: 0.99, blue: 0.99)
    static let headingText = Color(red: 0.17, green: 0.13, blue: 0.10)
    static let bodyText = Color(red: 0.42, green: 0.35, blue: 0.29)
    static let linkText = Color(red: 0.04, green: 0.43, blue: 0.45)
    static let accentGreen = Color(red: 0.36, green: 0.55, blue: 0.35)
    static let buttonIdle = Color(red: 0.17, green: 0.13, blue: 0.10)
    static let inputBackground = Color(red: 0.96, green: 0.93, blue: 0.88)
}
#endif
