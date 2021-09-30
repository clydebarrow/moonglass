package main

import (
	"nvrEvents/pkg/hikvision"
	"sync"
)

var hikCameras = []hikvision.HikCamera{
	{"192.168.1.25", 1},
	{"192.168.254.11", 4},
	{"192.168.254.10", 2},
	{"192.168.254.2", 3},
}

func main() {

	wg := sync.WaitGroup{}
	wg.Add(len(hikCameras))
	println("Starting")
	for _, camera := range hikCameras {
		println("starting: ", camera.Hostname)
		go func(camera hikvision.HikCamera) {
			hikvision.HikListen(camera)
			wg.Done()
		}(camera)
	}
	wg.Wait()
}
