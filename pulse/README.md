# Pulse

This app exists solely to be run in AWS Lambda, and call the Hub /heartbeat endpoint once per minute.

Requires Lambda environment variables defined:

```
platform_heartbeat_key
platform_heartbeat_url
```
