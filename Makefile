APP_NAME := fusionbrain-mcp-server
TARGET_DIR := target
TAG := latest

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
    MVNW := ./mvnw
endif
ifeq ($(UNAME_S),Darwin)
    MVNW := ./mvnw
endif
ifeq ($(OS),Windows_NT)
    MVNW := mvnw.cmd
endif

.PHONY: build docker clean

build:
	$(MVNW) clean package -DskipTests

docker: build
	docker build -t $(APP_NAME):$(TAG) .

clean:
	$(MVNW) clean
