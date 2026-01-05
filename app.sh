#!/usr/bin/env sh
set -eu

cmd="${1:-}"

case "$cmd" in
  start)
    ./gradlew bootRun > /tmp/genai-boot.log 2>&1 &
    echo "Started app (bootRun) in background. Logs: /tmp/genai-boot.log"
    ;;
  stop)
    pids=$(lsof -ti:8081 || true)
    if [ -z "$pids" ]; then
      echo "No process listening on port 8081."
      exit 0
    fi
    echo "Stopping PIDs: $pids"
    kill $pids
    sleep 1
    if lsof -ti:8081 >/dev/null 2>&1; then
      echo "Port 8081 is still in use."
      exit 1
    fi
    echo "Port 8081 is clear."
    ;;
  status)
    pids=$(lsof -ti:8081 || true)
    if [ -z "$pids" ]; then
      echo "stopped"
      exit 1
    fi
    echo "running: $pids"
    ;;
  *)
    echo "Usage: $0 {start|stop|status}"
    exit 1
    ;;
esac
