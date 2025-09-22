import SwiftUI
import BackgroundTasks

@main
struct VolumeCyclerApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
        // Register background app refresh task
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.volumecycler.app.refresh", using: nil) { task in
            self.handleAppRefresh(task: task as! BGAppRefreshTask)
        }
        
        // Schedule the first refresh
        scheduleAppRefresh()
        
        return true
    }
    
    func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        // Handle background fetch if needed
        completionHandler(.noData)
    }

    private func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: "com.volumecycler.app.refresh")
        // Earliest date you'd like the system to run the task
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            // Ignore scheduling errors for now
        }
    }

    private func handleAppRefresh(task: BGAppRefreshTask) {
        // Schedule the next refresh
        scheduleAppRefresh()

        // Work simulation or hook your refresh work here
        let operation = BlockOperation {
            // TODO: perform your refresh work
        }

        task.expirationHandler = {
            operation.cancel()
        }

        operation.completionBlock = {
            task.setTaskCompleted(success: !operation.isCancelled)
        }

        OperationQueue().addOperation(operation)
    }
}

