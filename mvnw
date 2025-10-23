#!/bin/sh
MVNW_PATH="$(dirname "$0")"
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
else
  echo "Maven (mvn) が見つかりません。ローカルに Maven をインストールするか、mvn コマンドを用意してください。" >&2
  exit 1
fi
