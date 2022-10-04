# setup image name
FROM ubuntu:18.04
FROM postgres:10

MAINTAINER mvchernyshenko@yandex.ru

# clear list of packages
# sync packages list with remote
# install locales, nano
RUN apt-get clean && \
    apt-get update && \
    apt-get install -y locales && \
    apt-get install -y locales-all && \
    apt-get install -y nano

# clear list of packages, no need to store them
RUN rm -rf /var/lib/apt/lists/*

# setup the locales
# so that UTF would not
# look like hieroglyphs
ENV LC_ALL   en_US.UTF-8
ENV LANG     en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# copy scripts to be run when
# database is being created
COPY ./database/ /docker-entrypoint-initdb.d
