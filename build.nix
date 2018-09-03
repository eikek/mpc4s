with import <nixpkgs> { };

buildFHSUserEnv {
  name = "enter-fhs";
  targetPkgs = pkgs: with pkgs; [
    netcat jdk8 wget which zsh dpkg sbt git elmPackages.elm ncurses fakeroot mc jekyll
    # haskells http client needs this (to download elm packages)
    iana-etc
  ];
  runScript = "$SHELL";
}
