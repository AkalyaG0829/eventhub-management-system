import urllib.request
import json

try:
    req = urllib.request.Request("http://localhost:8080/api/events")
    with urllib.request.urlopen(req, timeout=5) as response:
        data = json.loads(response.read().decode())
        print(json.dumps(data, indent=2))
except Exception as e:
    print("Error:", e)
