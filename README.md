
# A.M.I.C.A.

**A.M.I.C.A.** stands for "Autonomous Multi-agent Intelligent Companion Assistant", and "**amica**" is the latin word for *friend*

## How to run this project

### 1. Start the services:

```shell
docker compose up -d

```

This will start two containers:

- A [mosquitto](https://mosquitto.org/) MQTT message broker that will be used as Message Bus.
- A [Valkey](https://valkey.io/) cache that will be used as a shared memory.

### 2. Compile the Java SDK project:

```shell
cd amika-sdk-java
mvn clean install
```

### 3. Start the services (each one in a different terminal):

```shell
cd agents/local-system-agent
mvn mn:run
```

```shell
cd agents/minute-taker-agent
mvn mn:run
```

### 4. Start the CLI (in a new terminal)

```bash
cd amica-cli
mvn mn:run
```

Once in the CLI application, you can send queries, like for example:

```shell
> How many users are currently logged into this system?
```

The `local-system-agent` will use the [UserCount](./agents/local-system-agent/src/main/java/es/nachobrito/amica/agent/localsystem/domain/model/agent/tool/UserCountTool.java) tool to get the number of users currently logged, and will respond with something like this:

```shell
There are currently 2 users logged into this system.
```

You will also see how the `minute-taker-agent` will detect there was a new conversation between the user and the agent, and will generate the minute:

```shell
10:13:55.198 [RxComputationThreadPool-1] INFO  e.n.a.a.m.i.l.agent.MinuteTakerAgent - Saving minute for conversation ebc5d308-8fba-4a61-af09-913dd0766eb8 to ...
10:13:55.198 [RxComputationThreadPool-1] INFO  e.n.a.a.m.i.l.agent.MinuteTakerAgent - 
{
  "actors": ["nacho", "local-system-agent"],
  "summary": "The user asked how many users are currently logged into this system.",
  "messages": [
    "The user asked How many users are currently logged into this system",
    "The agent response was There are currently 2 users logged into this system."
  ]
}

```

## References

- https://developer.nvidia.com/blog/introduction-to-llm-agents/
- https://developer.nvidia.com/blog/build-an-llm-powered-api-agent-for-task-execution/
- https://developer.nvidia.com/blog/building-your-first-llm-agent-application/