set -e

mkdir -p ~/app
cd ~/app

JAR=$(ls -t *.jar | head -n1)

if command -v lsof >/dev/null 2>&1; then
  PID=$(lsof -t -i:8080 || true)
  if [ -n "$PID" ]; then
    kill -9 "$PID" || true
  fi
else
  fuser -k 8080/tcp || true
fi

if [ -f ../../.env ]; then
  export $(cat ../../.env | xargs)
fi

nohup java -jar "$JAR" > app.log 2>&1 &
echo "Started $JAR"
