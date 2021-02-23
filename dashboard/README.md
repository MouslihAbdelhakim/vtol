# Vtol's dashboard

This folder should contain everything you need to store and display metrics on a grafana dashboard. The content of this folder are cloned from https://github.com/myoperator/grafana-graphite-statsd

## Manage the dashboard

You should have `docker`, `docker-compose` and `make` installed on your machine. 

Start the dashboard with:
```bash
$ make up
```

Stop the dashboard with:
```bash
$ make down
```

To run container's shell
```bash
$ make shell
```

To view the container log
```bash
$ make tail
```

## Logging into the dashboard

Once all containers are running, all you need to do is:

- open your browser pointing to http://localhost (or another port if you changed it)
- login with the default username (admin) and password (admin)
- Go to datasources and check if graphite is there as default datasource

## Info about the containers
The container exposes the following ports:
- `80`: the Grafana web interface.
- `8080`: the Graphite web port
- `2003`: the Graphite data port
- `8125`: the StatsD UDP port.
- `9125`: the StatsD repeater's UDP port.
- `8126`: the StatsD administrative port.
- `9102`: statsD prometheus metrics
- `9090`: prometheus metrics
- `9093`: alertmanager