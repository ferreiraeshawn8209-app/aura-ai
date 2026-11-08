#!/bin/sh
set -eu
repo_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
wrapper_path="$repo_root/gradle/wrapper/gradle-wrapper.jar"
wrapper_hash=cb0da6751c2b753a16ac168bb354870ebb1e162e9083f116729cec9c781156b8
if [ ! -f "$wrapper_path" ]; then
    curl -fsSL --retry 2 https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar -o "$wrapper_path"
fi
printf '%s  %s\n' "$wrapper_hash" "$wrapper_path" | sha256sum -c -
