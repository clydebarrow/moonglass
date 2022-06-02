package main

import (
	"nvrEvents/pkg/hikvision"
	"sync"
)

var hikCameras = []hikvision.HikCamera{
	{"192.168.254.5", 3},
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
