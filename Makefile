IMAGE    := mall-chain
VERSION  := 0.0.1-SNAPSHOT
TAG      := $(IMAGE):$(VERSION)
LATEST   := $(IMAGE):latest
PORT     := 10010
PROFILE  := prod

.PHONY: build jar clean image run up down logs ps restart push pull init-db

jar:
	mvn clean package -DskipTests

clean:
	mvn clean

image:
	docker build -t $(TAG) -t $(LATEST) .

run: image
	docker run --rm -p $(PORT):$(PORT) \
		-e SPRING_PROFILE=$(PROFILE) \
		-e JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -Dfile.encoding=UTF-8" \
		-v mall-chain-logs:/data/app/logs/mall_chain \
		$(TAG)

up: image
	docker compose up -d

down:
	docker compose down

logs:
	docker compose logs -f mall-chain

ps:
	docker compose ps

restart:
	docker compose restart mall-chain

push:
	docker push $(TAG) && docker push $(LATEST)

pull:
	docker pull $(LATEST)

init-db:
	mysql -u root -p < mall-chain.sql
