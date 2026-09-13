import urllib.request
import json

try:
    print("Testing /api/events/1...")
    req = urllib.request.Request("http://localhost:8080/api/events/1")
    with urllib.request.urlopen(req, timeout=5) as response:
        print(response.status)
except Exception as e:
    print("Error:", e)
