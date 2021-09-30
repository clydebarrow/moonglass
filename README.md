## Moonglass

### A front end for the Moonfire NVR

This is a work in progress, but is basically functional.

#### To run the web app as a development server:

* clone this repository - `git clone https://github.com/clydebarrow/moonglass.git`
* cd into `moonglass/webapp`
* Create the file "local.properties" and add a line like this:
````
nvrHost=http://192.168.1.50:8080
````

where the url points to your installation of Moonfire NVR.

* Run the command:

````
gradlew browserDevelopmentRun --continuous
````
A browser window should open, presenting the UI.

#### To install onto the NVR server

You will need to set up a proxy server - nginx is recommended.
The Moonglass production files need to be copied to the NVR server - you can automate this by
defining an `scp` destination in `local.properties`, e.g.:

````
deployTarget=ubuntu@nvr.example.com:moonglass
````

With this defined, you can simply run `gradlew deploy` and the production files will be copied to the given destination. To manually build run this command:

````shell
gradlew browserProductionWebpack
````

Then recursively copy all files in `build/distributions` to the server.

There is a [sample nginx config file](/Resources/nvr-nginx.conf) provided - you will need to
change the hostname as required, and the file location as chosen above.

Place the config file in `/etc/nginx/conf.d/` on the server, then restart nginx. Now browse to the server and the ui should load. The proxy server will deliver the Moonglass files while proxying the API requests to the Moonfire NVR running on the same host.



