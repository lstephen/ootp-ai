FROM maven:3-jdk-8

RUN apt-get update && apt-get install -y git python3 python3-pip

RUN ln -s /usr/bin/python3 /usr/local/bin/python \
 && ln -s /usr/bin/pip3 /usr/local/bin/pip

RUN pip install --upgrade pip

ENV LC_ALL=C.UTF-8 LANG=C.UTF-8
