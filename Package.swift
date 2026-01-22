// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "liblc3",
    products: [
        .library(name: "liblc3", targets: ["liblc3"]),
    ],
    targets: [
        .target(
           name: "liblc3",
           path: ".",
           exclude: [
               "CONTRIBUTING.md",
               "LICENSE",
               "Makefile",
               "README.md",
               "conformance",
               "fuzz",
               "meson.build",
               "meson_options.txt",
               "pyproject.toml",
               "python",
               "tables",
               "test",
               "tools",
               "wasm",
               "zephyr",
               "src/meson.build",
               "src/makefile.mk",
           ],
           sources: [
               "src",
           ],
           publicHeadersPath: "include",
           cSettings: [
                .headerSearchPath("include"),
                .headerSearchPath("src"),
           ],
         ),
    ]
)

