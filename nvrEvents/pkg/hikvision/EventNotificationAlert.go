package hikvision

import (
	"encoding/xml"
	"fmt"
	"github.com/bobziuchkovski/digest"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"nvrEvents/pkg/nvr"
	"strings"
	"time"
)

const userName = "admin"
const password = "Camera2021"
const notificationPath = "/ISAPI/Event/notification/alertStream"
const scheme = "http"

type HikCamera struct {
	Hostname string // the camera Hostname
	Id       int    // the camera id, used for notifying signals
}
type EventNotificationAlert struct {
	XMLName          xml.Name `xml:"EventNotificationAlert"`
	Text             string   `xml:",chardata"`
	Version          string   `xml:"version,attr"`
	Xmlns            string   `xml:"xmlns,attr"`
	IpAddress        string   `xml:"ipAddress"`
	PortNo           int      `xml:"portNo"`
	Protocol         string   `xml:"protocol"`
	MacAddress       string   `xml:"macAddress"`
	ChannelID        string   `xml:"channelID"`
	DateTime         string   `xml:"dateTime"`
	ActivePostCount  int      `xml:"activePostCount"`
	EventType        string   `xml:"eventType"`
	EventState       string   `xml:"eventState"`
	EventDescription string   `xml:"eventDescription"`
	ChannelName      string   `xml:"channelName"`
}

func (e EventNotificationAlert) String() string {
	return fmt.Sprintf(
		"Event(type=%s, state=%s, time=%s, count=%d on channel %s)",
		e.EventType,
		e.EventState,
		e.DateTime,
		e.ActivePostCount,
		e.ChannelName,
	)
}

// listen to a Hikvision camera's event stream, report events
func HikListen(camera HikCamera) {

	// try digest authentication first, if it fails fall back to basic.
	req, _ := http.NewRequest("GET", scheme+"://"+camera.Hostname+notificationPath, nil)
	transport := digest.NewTransport("admin", "Camera2021")
	resp, err := transport.RoundTrip(req)
	if err != nil {
		req.SetBasicAuth("admin", "Camera2021")
		resp, err = http.DefaultClient.Do(req)
	}
	if err != nil {
		log.Fatalln(err)
		return
	}
    hdrs := resp.Header
    // loop over keys and values in the map.
    for k, v := range hdrs {
        fmt.Println(k, "value is", v)
    }
	hdr := resp.Header.Get("Content-Type")
    println("Content-type=", hdr)
	boundary := strings.Split(hdr, "boundary=")[1]
	reader := multipart.NewReader(resp.Body, boundary)
	for {
		part, err := reader.NextPart()
		if err != nil {
			println("Error: ", err)
			break
		}
		data, err := io.ReadAll(part)
		if err != nil {
			println("Error: ", err)
			break
		}
		var packet EventNotificationAlert
		err = xml.Unmarshal(data, &packet)
		if err != nil {
			println("Xml err: ", err)
			continue
		}
		if packet.EventState == "active" && packet.EventType == "VMD" {
			timeStamp, err := time.Parse("2006-01-02T15:04:05-07:00", packet.DateTime)
			if err == nil {
				nvr.SendSignal(camera.Id, 1, timeStamp, 10*time.Second)
			}
		}
		//println(packet.String())
	}
}
