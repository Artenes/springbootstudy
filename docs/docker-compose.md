# Docker Compose

Basically docker-compose will orchestrate the lifecycle of a set of docker images. So instead of having to start multiple
containers manually, you can run a single command to start and configure all of them at once.

````shell
docker-compose up
````

Running this on the root of the project will make use of the ``docker-compose.yml`` file to start running some containers.

## Volumes

Volumes are like pen-drives. Docker generate them and mount them to the containers when they are up. You can share them among
multiple containers, make back up of them or just delete them. Is a good way to share data between containers at runtime.

## Network

You can specify "networks" in the compose file and containers will be able to see or not each other if they are on the same
network or not. If you set nothing, everyone is put on the same network and can see each other. If you create networks, then
you can hide containers from each other.

Fun fact: within a container, you reference another container by its name in the network. Ex: ``postgres://db:5432``, where
db is the name of a service in the docker-compose.yml file:

````yml
services:
  web:
    build: .
    ports:
      - "8000:8000"
  db:
    image: postgres
    ports:
      - "8001:5432"
````

## Links

- [Networking in Compose](https://docs.docker.com/compose/networking/)
- [Volumes](https://docs.docker.com/storage/volumes/)
- [Use Compose in production](https://docs.docker.com/compose/production/)
- [Docker Compose Tutorial: advanced Docker made simple](https://www.educative.io/blog/docker-compose-tutorial)