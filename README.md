# FusionBrain MCP Server

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

**FusionBrain MCP Server** is an MCP server application based on Java + Spring Boot and [FusionBrain SDK Starter](https://gitlab.com/fusionbrain-dev/fusionbrain-spring-boot-starter).  
The server provides tools for generating images by using FusionBrain API.

---

## Features

- Uses [FusionBrain SDK Starter](https://gitlab.com/fusionbrain-dev/fusionbrain-spring-boot-starter) configurable via `application.yml`, environment variables, or JVM options.
- Supports three main MCP tools:
    - `startGenerateImage` — starts image generation and returns task id
    - `checkStatus` — checks the status of started task by id
    - `generateImageSync` — starts image generation and blocks until image is ready

---

## Configuration

### SDK configuration
FusionBrain MCP Server uses **default SDK settings**, but they can be overridden.
Some properties like **api-key** and **secret-key** are obligatory and must be defined.

| Property                 | Description                    | Default                        |
|--------------------------|--------------------------------|-------------------------------|
| `fusionbrain.api-key`    | API key for FusionBrain access | — _(required)_                |
| `fusionbrain.api-secret` | Secret key for FusionBrain access  | — _(required)_                |
| `fusionbrain.base-url`   | FusionBrain server URL         | `https://api-key.fusionbrain.ai` |

The full list of sdk properties can be found [here](https://gitlab.com/fusionbrain-dev/fusionbrain-spring-boot-starter).

### MCP configuration
This server is based on Spring AI [MCP Server Boot Starter](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html).
Properties of Spring AI can be overridden if needed.

This MCP server can be run with `stdio` transport and `SSE` (default option).

`stdio` transport can be set by activating Spring profile `stdio`:

`java -Dspring.profiles.active=stdio ... -jar fusionbrain-mcp-server.jar`

Note that in `stdio` profile logging to console is disabled, and you have to activate file logs by defining:
```properties
logging.file.path=/app/logs                 # directory where rolling log files are collected
logging.file.name=/app/logs/mcp-server.log  # path to a log file
```

### Ways to set parameters:

1. **Via JVM options:**

   ```
   java -jar \
    -Dfusionbrain.api-key=<your-api-key> \
    -Dfusionbrain.secret-key=<your-secret-key> \
    fusionbrain-mcp-server.jar
   ```

2. **Via environment variables:**

   ```
   export FUSIONBRAIN_APIKEY=<your-api-key>
   export FUSIONBRAIN_SECRETKEY=<your-secret-key>
   java -jar fusionbrain-mcp-server.jar
   ```


---

## Build

### Build `.jar` from source

```
cd <path/to/fusionbrain-mcp-server>
make build
```

The built file will be located at:

```
target/fusionbrain-mcp-server-<version>.jar
```

### Build Docker image

```
cd <path/to/fusionbrain-mcp-server>
make docker
```

The built image will be named:

```
fusionbrain-mcp-server:latest
```

---

## Run

### Via Java (JAR)

```
java -jar \
  -Dfusionbrain.api-key=your-api-key \
  -Dfusionbrain.secret-key=your-secret-key \
  target/FusionBrain-mcp-server.jar
```

### Via Docker

```
docker run --rm \
  -e FUSIONBRAIN_APIKEY=your-api-key \
  -e FUSIONBRAIN_SECRETKEY=your-api-key \
  -e JAVA_OPTS=<your jvm options> \
  -p 8080:8080 \
  fusionbrain-mcp-server:latest
```

---

## Tools

### `startGenerateImage`

Start image generation. Accepts the following request format:
```json
{
  "request": {
    "style": "",
    "width": 1024,
    "height": 1024,
    "prompt": "your prompt"
  }
}
```
By default, width and height values are 1024.
This tool accepts these generation params and calls image generation API.
As generation takes some time to be finished, this tool does not wait for it and returns `taskId`, by which the status of the generation can be requested.

Response format:
```json
{
  "status": "STARTED",
  "taskId": "123e4567-e89b-12d3-a456-426614174000",
  "description": "nullable",
  "statusRequestDelay": 10
}
```
`status` can equal `STARTED` or `ERROR`. In the latter case `description` is provided.

`taskId` is a UUID of certain generation task.

`statusRequestDelay` is an estimation in seconds how much time it takes to complete generation.
To avoid vain status polling requests, it's recommended to call `checkStatus` tool first after the lapse of this delay.

---

### `checkStatus`

Check resource status by UUID. If finished then a result is returned.

As the only argument it takes `taskId` in UUID format that was returned after calling `startGenerateImage`.

Response format:
```json
{
  "status": "FINISHED",
  "result": "image in base64 format"
}
```
or
```json
{
  "status": "IN_PROCESS | INTERNAL_ERROR"
}
```
If returned `status` is `IN_PROCESS`, a later tool call is supposed.
If you don't want to implement polling logic and not so many client-server connections are supposed, you can call the tool below, that blocks until the image is ready.

---
### `generateImageSync`
Starts image generation and blocks until the image is ready. Warning: this tool holds http connection between this server and your client.

Request format is identical to `startGenerateImage`'s format whereas a response format is the same as `checkStatus` response.

Once the request to image generation API is performed this tool polls API checking the status of the task and returns either with `FINISHED | INTERNAL_ERROR` statuses or `POLLING_TIMEOUT` if the limit of retries is reached.

The interval between repeated calls and max number of retries can be set via properties `fusionbrain.poll-interval` and `fusionbrain.max-retries` respectively.

---

## Download

The ready `.jar` file will be available for download [releases](https://github.com/FusionBrain26/fusionbrain-mcp-server/releases).

---

## Requirements

- Java 17+
- Docker (optional, for container build)

---

## 📝 License

This project is licensed under the MIT License.
