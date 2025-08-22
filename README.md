
# A.M.I.C.A.

**A.M.I.C.A.** stands for "Autonomous Multi-agent Intelligent Companion Assistant", and "**amica**" is the latin word for *friend*

## How to run this project

1. Start the services:

```shell
docker compose up -d

```

2. Start the services (each one in a different terminal):

```shell
cd agents/LocalSystemAgent
mvn mn:run

```shell
cd agents/MinuteTakerAgent
mvn mn:run
```

3. Start the CLI (on a new terminal)

```bash
cd amica-cli
mvn mn:run
```

Once in the CLI application, you can send queries, like for example:

```shell
> How many users are currently logged into this system?


## References

- https://developer.nvidia.com/blog/introduction-to-llm-agents/
- https://developer.nvidia.com/blog/build-an-llm-powered-api-agent-for-task-execution/
- https://developer.nvidia.com/blog/building-your-first-llm-agent-application/