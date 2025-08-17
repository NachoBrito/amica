# Mosquitto Message Broker

This docker-compose file starts a [Mosquitto](https://www.mosquitto.org/) message broker. Use the following command to run the service:

```bash
docker-compose up -d
```

Then, if you run `docker -ps` you should see something like this:

```
$ docker ps
CONTAINER ID   IMAGE                      COMMAND                  CREATED      STATUS      PORTS                                                                                      NAMES
65f8c55f605c   eclipse-mosquitto:latest   "/docker-entrypoint.…"   2 days ago   Up 2 days   0.0.0.0:1883->1883/tcp, [::]:1883->1883/tcp, 0.0.0.0:9001->9001/tcp, [::]:9001->9001/tcp   mosquitto
```

## Verify It Works

To verify this is working, you can subscribe to a topic, in this case `test.message`:

```bash
docker exec -it mosquitto mosquitto_sub -v -t "#"
```

And then publish a message **from a different terminal**:

```bash
docker exec -it mosquitto mosquitto_pub -t test.message -m 'It works!'
```

You should see the message received in the subscribed terminal window:

``` bash
$ docker exec -it mosquitto mosquitto_sub -v -t test.messagege
test.message It works!
```

