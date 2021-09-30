# Motion detection gateway

This folder contains a small Go program that will listen to event streams from a given set of camera urls (currently
only Hikvision cameras are supported) and post those events into the Moonfire database as signals.

This code is intended as proof-of-concept and it is hoped that the functionality will be baked into the Moonfire
server code at some point in the future.

The set of cameras to monitor is contained in a list in `cmd/Main.go`. This list contains pairs of IP addresses
and an ID, which is expected to map to an entry in Moonfire's `signal` table. The only event type listened for
is motion detection (indicated by an EventType of "VMD") and it is mapped unconditionally to a signal type of "1", which
is assumed to be the index value of a motion event type in Moonfire's `signal_type_enum` table.

A username and password to be used for all cameras is hard-coded in `pkg/hikvision/EventNotificationAlert.go`.

Similarly the url for the Moonfire API and an authentication token are hard-coded in `pkg/nvr/Signals.go`

To build an executable to run on a 64 bit Arm architecture (e.g. Ubuntu 64 bit running on a Raspberry Pi 4) use this
command:

````shell
env GOOS=linux GOARCH=arm64 go build -o nvrEvents_arm64 -ldflags="-s -w" cmd/Main.go
````

This will leave the executable `nvrEvents_arm64` in this directory. It can be copied to the Pi and run.

