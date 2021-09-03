## Moonglass

### A front end for the Moonfire NVR

This is a work in progress, and has not been tested other than using the dev-server.

To run:

* clone this repository - `git clone https://github.com/clydebarrow/moonglasss.git`
* cd into `moonglass`
* Create the file "local.properties" and add a line like this:
````
nvrHost=http://192.168.1.50:8080
````

where the url points to your *unauthenticated* installation of Moonfire NVR (user authentication is coming...)

* Run the command:

````
gradlew browserDevelopmentRun --continuous
````
A browser window should open, presenting the UI.

