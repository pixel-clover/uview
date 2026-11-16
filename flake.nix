{
  description = "UView: Java development environment for the Unity package viewer";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { nixpkgs, ... }:
    let
      supportedSystems = [
        "x86_64-linux"
        "aarch64-linux"
        "aarch64-darwin"
      ];
      forAllSystems = nixpkgs.lib.genAttrs supportedSystems;
    in
    {
      devShells = forAllSystems (system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
          jdk = pkgs.jdk21;
        in
        {
          default = pkgs.mkShell {
            packages = with pkgs; [
              jdk
              (maven.override { jdk_headless = jdk; })
              gnumake
              git
              pre-commit
              nixpkgs-fmt
              python315
            ];

            JAVA_HOME = "${jdk}";

            # Maven's JavaFX native libraries load these at runtime on Linux.
            shellHook = pkgs.lib.optionalString pkgs.stdenv.hostPlatform.isLinux ''
              export LD_LIBRARY_PATH="${pkgs.lib.makeLibraryPath [ pkgs.gtk3 pkgs.alsa-lib pkgs.libGL pkgs.libXtst pkgs.libXxf86vm ]}''${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
            '' + ''
              echo "UView dev shell (Java 21 with Maven)"
              echo ""
              echo "Common commands:"
              echo "  make package      Build target/uview.jar"
              echo "  make run          Build and launch UView"
              echo "  make test         Run tests and build checks"
              echo "  make lint         Check Java style"
              echo "  make format       Format Java sources"
            '';
          };
        });

      formatter = forAllSystems (system: nixpkgs.legacyPackages.${system}.nixpkgs-fmt);
    };
}
