# Local Tests

The following tests are simple integration pyunit tests which run the services under test in a process using Gradle via python. These "Local Tests" are also a part of the CircleCI pipeline.

Note: [virtualenvwrapper](https://virtualenvwrapper.readthedocs.io/en/latest/) is your friend

* test_server.py

To run, simply invoke via python. Any java changes will be recompiled before the tests are run.
```bash
python test_server.py
```


# Containerized Tests

The following tests are integration tests meant to be run in Docker container(s). 

* proxy_load_tests.py 

To run locally invoke via docker-compose, connect to the client container containing the tests and run the tests.

#### Build the server artifacts and docker images 
```bash
./gradlew :int-test-proxy-server:installDist
./gradlew :int-test-backend-server:installDist
cd int-tests
docker-compose build
```

#### Run the tests with docker-compose
```bash
docker-compose up -d
docker exec -it inttests_client_1 /bin/bash
python3 /tests/proxy_load_tests.py
exit
docker-compose down
```
