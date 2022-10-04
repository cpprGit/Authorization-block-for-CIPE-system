# setup image name
FROM ubuntu:18.04
MAINTAINER mvchernyshenko@yandex.ru

# clear list of packages
# sync packages list with remote
# install jre, locales, nano
RUN apt-get clean && \
    apt-get update && \
    apt-get install -y openjdk-8-jre-headless && \
    apt-get install -y locales && \
    apt-get install -y locales-all && \
    apt-get install -y nano

# clear list of packages, no need to store them
RUN rm -rf /var/lib/apt/lists/*

# setup the locales
# so that UTF would not
# look like shit and hieroglyphs
ENV LC_ALL   ru_RU.UTF-8
ENV LANG     ru_RU.UTF-8
ENV LANGUAGE ru_RU.UTF-8

