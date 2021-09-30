package nvr

import (
	"bytes"
	"encoding/json"
	"log"
	"net/http"
	"time"
)

const token string = "G5Xed6gfonA88cB//gAZIq15QTC6LqOsXRSHO3nyvAlG0/ek0I5qbcHUdRojgfPF"
const postUrl = "http://localhost:8080/api/signals"

/**
Convert a time to a value in 90kHz units
*/
func TimeTo90k(timeVal time.Time) int64 {
	return timeVal.UnixMilli() * int64(90)
}

func DurationTo90k(timeVal time.Duration) int64 {
	return timeVal.Milliseconds() * int64(90)
}

type Time90k struct {
	Base   string `json:"base"`
	Rel90k int64  `json:"rel90k"`
}
type Signal struct {
	SignalIds []int   `json:"signalIds"`
	States    []int   `json:"states"`
	Start     Time90k `json:"start"`
	End       Time90k `json:"end"`
}

/**
Send a signal for the given camera
*/
func SendSignal(cameraId int, eventId int, startTime time.Time, duration time.Duration) {
	body := &Signal{
		SignalIds: []int{cameraId},
		States:    []int{eventId},
		Start:     Time90k{Base: "epoch", Rel90k: TimeTo90k(startTime)},
		End:       Time90k{Base: "epoch", Rel90k: TimeTo90k(startTime) + DurationTo90k(duration)},
	}
	payloadBuf := new(bytes.Buffer)
	err := json.NewEncoder(payloadBuf).Encode(body)
	if err != nil {
		println(err)
		return
	}
	req, err := http.NewRequest("POST", postUrl, payloadBuf)
	req.Header.Set("Content-type", "application/json")
	if err != nil {
		log.Fatalln("Post failed")
	}
	req.AddCookie(&http.Cookie{
		Name:  "s",
		Value: token,
	})
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		log.Fatalln("Post failed")
	}
	resp.Body.Close()
}
