#!/bin/bash

docker run \
  --name postgres \
   -p 5432:5432 \
   -v /media/nvm/postgres_facedetect:/var/lib/postgresql/data \
  --rm \
   -e POSTGRES_PASSWORD=finger \
      postgres:15.1
