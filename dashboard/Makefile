# docker-grafana-graphite makefile

# Environment Varibles
CONTAINER = myop-graphite-dashboard

.PHONY: up

prep :
	mkdir -p \
		log/graphite \
		log/graphite/webapp \
		grafana/internals \
		graphite/internals

pull :
	docker-compose pull

up : prep pull
	docker-compose -f docker-compose.yml up -d

down :
	docker-compose -f docker-compose.yml down

shell :
	docker exec -ti $(CONTAINER) /bin/sh

tail :
	docker logs -f --tail=10 $(CONTAINER)
