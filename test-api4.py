import urllib.request
import json

try:
    print("Testing /api/events without proxy...")
    proxy_handler = urllib.request.ProxyHandler({})
    opener = urllib.request.build_opener(proxy_handler)
    urllib.request.install_opener(opener)
    
    req = urllib.request.Request("http://127.0.0.1:8080/api/events")
    with urllib.request.urlopen(req, timeout=5) as response:
        data = json.loads(response.read().decode())
        print(json.dumps(data, indent=2))
except Exception as e:
    print("Error:", e)
