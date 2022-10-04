# setup image name
FROM ubuntu:18.04

# don't forget who's daddy here
MAINTAINER mvchernyshenko@yandex.ru

# clear list of packages
# sync packages list with remote
# install all packages
RUN apt-get clean && \
    apt-get update && \
    apt-get install -y locales && \
    apt-get install -y locales-all && \
    apt-get install -y nano && \
    apt-get install -y mlocate && \
    apt-get install -y telnet && \
    apt-get install -y mailutils && \
    apt-get install -y sharutils && \
    apt-get install -y postfix && \
    apt-get install -y postfix-pgsql && \
    apt-get install -y dovecot-imapd && \
    apt-get install -y dovecot-pop3d && \
    apt-get install -y dovecot-pgsql && \
    apt-get install -y sasl2-bin && \
    apt-get install -y libsasl2-modules && \
    apt-get install -y libpam-pgsql

# clear list of packages, no need to store them
RUN rm -rf /var/lib/apt/lists/*

# setup the locales
# so that UTF would not
# look like shit and hieroglyphs
ENV LC_ALL   ru_RU.UTF-8
ENV LANG     ru_RU.UTF-8
ENV LANGUAGE ru_RU.UTF-8

RUN groupadd  -g 1000 vmail && \
    useradd -d /var/vmail/ -g 1000 -u 1000 vmail || echo "user \"vmail\" already exists" && \
    mkdir -p /var/vmail && \
    chown vmail:vmail /var/vmail

# remove libsasl2-modules automatically installed
# plugins that we do not need anymore
RUN rm -f /usr/lib/sasl2/libcrammd5.* && \
    rm -f /usr/lib/sasl2/libdigestmd5.* && \
    rm -f /usr/lib/sasl2/libsasldb.* && \
    rm -f /usr/lib/sasl2/libotp.* && \
    rm -f /usr/lib/sasl2/libntlm.* && \
    rm -f /usr/lib/sasl2/libanonymous.*

RUN touch /var/vmail/dummy.txt



CMD [ "tail", "-f", "/var/vmail/dummy.txt" ]