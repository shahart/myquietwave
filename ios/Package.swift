// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "VolumeCycler",
    platforms: [
        .iOS(.v15)
    ],
    products: [
        .library(
            name: "VolumeCycler",
            targets: ["VolumeCycler"]
        ),
    ],
    dependencies: [],
    targets: [
        .target(
            name: "VolumeCycler",
            dependencies: [],
            path: "Sources"
        ),
    ]
)




