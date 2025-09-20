import SwiftUI

struct ContentView: View {
    @StateObject private var volumeManager = VolumeManager()
    
    var body: some View {
        VStack(spacing: 30) {
            // App Title
            Text("Volume Cycler")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(.primary)
            
            // Status Card
            VStack(spacing: 20) {
                HStack {
                    Image(systemName: volumeManager.isCycling ? "speaker.wave.2.fill" : "speaker.slash.fill")
                        .font(.title)
                        .foregroundColor(volumeManager.isCycling ? .green : .red)
                    
                    VStack(alignment: .leading) {
                        Text(volumeManager.isCycling ? "Cycling Active" : "Stopped")
                            .font(.headline)
                            .foregroundColor(volumeManager.isCycling ? .green : .red)
                        
                        Text(volumeManager.currentState == .muted ? "Currently Muted" : "Playing at 30%")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    
                    Spacer()
                }
                
                if volumeManager.isCycling {
                    VStack(spacing: 10) {
                        Text("Time Remaining")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        Text(volumeManager.formatTime(volumeManager.timeRemaining))
                            .font(.title2)
                            .fontWeight(.semibold)
                            .foregroundColor(.primary)
                        
                        Text(volumeManager.currentState == .muted ? "Until next play cycle" : "Until next mute cycle")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(10)
                }
            }
            .padding()
            .background(Color(.systemBackground))
            .cornerRadius(15)
            .shadow(color: .black.opacity(0.1), radius: 5, x: 0, y: 2)
            
            // Control Buttons
            VStack(spacing: 15) {
                Button(action: {
                    if volumeManager.isCycling {
                        volumeManager.stopCycling()
                    } else {
                        volumeManager.startCycling()
                    }
                }) {
                    HStack {
                        Image(systemName: volumeManager.isCycling ? "stop.circle.fill" : "play.circle.fill")
                            .font(.title2)
                        Text(volumeManager.isCycling ? "Stop Cycling" : "Start Cycling")
                            .font(.headline)
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(volumeManager.isCycling ? Color.red : Color.blue)
                    .cornerRadius(12)
                }
                
                if volumeManager.isCycling {
                    Button(action: {
                        volumeManager.stopCycling()
                    }) {
                        HStack {
                            Image(systemName: "pause.circle.fill")
                                .font(.title2)
                            Text("Pause")
                                .font(.headline)
                        }
                        .foregroundColor(.orange)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.orange.opacity(0.1))
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color.orange, lineWidth: 1)
                        )
                    }
                }
            }
            
            // Information Section
            VStack(spacing: 15) {
                Text("How it works:")
                    .font(.headline)
                    .foregroundColor(.primary)
                
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "speaker.slash.fill")
                            .foregroundColor(.red)
                        Text("Mutes media volume for 4 hours")
                            .font(.subheadline)
                    }
                    
                    HStack {
                        Image(systemName: "speaker.wave.2.fill")
                            .foregroundColor(.green)
                        Text("Plays at 30% volume for 3 minutes")
                            .font(.subheadline)
                    }
                    
                    HStack {
                        Image(systemName: "arrow.clockwise")
                            .foregroundColor(.blue)
                        Text("Repeats this cycle continuously")
                            .font(.subheadline)
                    }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(10)
            }
            
            Spacer()
        }
        .padding()
        .background(Color(.systemGroupedBackground))
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
