# Derived from https://github.com/apache/brooklyn/blob/7fdccd260164293e0ebfe51a4d428aca1c717e2d/Dockerfile
# and cloudsoft/amp/Dockerfile, used for testing dockerfile that builds docs as well as other releases

# maven/debian distro seems reliable (NB: alpine has libgcc issues with Go and PhantomJS)
FROM maven:3.8.2-jdk-8


# Install necessary binaries to build Cloudsoft AMP
# We install everything we need for each subproject
# Do it in batches so docker build can resume on error

# deb.debian.org sometimes redirects to a broken fastly cdn, so manually set our mirror
RUN sed -i 's/deb.debian.org/ftp.uk.debian.org/' /etc/apt/sources.list

RUN apt-get update

RUN apt-get install -y \
    `# common tools` git-core rsync sudo vim less curl gpg tar zip procps \
    `# parsing utils xml json etc` libxml2 libxml2-utils jq \
    `# usual build tools for eg nodejs modules` make automake autoconf libtool nasm gcc
RUN apt-get install -y \
    `# builders for linux installers` rpm dpkg pkg-config \
    `# libraries for nodejs image processing` libpng-dev libjpeg-progs pngquant \
    `# node; maven installs this usually, but handy to have for testing` nodejs
RUN apt-get install -y \
    `# couchdb qa framework uses this` python python3-pip
RUN apt-get install -y \
    `# pushing to s3 buckets for docs etc` awscli


# don't need this at the moment - no pdf - and is big
#RUN apt-get install -y\
#    `# pdf generation for docs (big, xfonts etc)` wkhtmltopdf

# install Go (golang)
RUN cd /tmp \
&& curl -O https://dl.google.com/go/go1.15.8.linux-amd64.tar.gz \
&& CKSUM=$(sha256sum go1.15.8.linux-amd64.tar.gz | awk '{print $1}') \
&& [ ${CKSUM} = "d3379c32a90fdf9382166f8f48034c459a8cc433730bc9476d39d9082c94583b" ] \
&& tar xf go1.15.8.linux-amd64.tar.gz \
&& rm go1.15.8.linux-amd64.tar.gz \
&& chown -R root:root ./go \
&& mv go /usr/local

ENV PATH="${PATH}:/usr/local/go/bin"


# Make sure the /.config && /.npm (for UI module builds) is writable for all users
RUN mkdir -p /.config && chmod -R 777 /.config
RUN mkdir -p /.npm && chmod -R 777 /.npm


# set this pref to suppress warnings
RUN git config --global pull.rebase false


# Make sure the /var/tmp (for RPM build) is writable for all users
RUN mkdir -p /var/tmp/ && chmod -R 777 /var/tmp/

# Make sure the /var/maven is writable for all users
RUN mkdir -p /var/maven/.m2/ && chmod -R 777 /var/maven/
ENV MAVEN_CONFIG=/var/maven/.m2


# Delete openssl conf so that brooklyn ui modules phantom js can build a
# eg https://github.com/bazelbuild/rules_closure/issues/351
# possibly could update phantom
RUN rm /etc/ssl/openssl.cnf

# set up a non-root user with sudo
RUN useradd docker

RUN usermod -u 1000 docker
RUN mkdir /home/docker
RUN chown -R docker:docker /home/docker
RUN usermod -aG sudo docker && sed -i 's/ALL$/NOPASSWD: ALL/' /etc/sudoers


# switch to this user
USER docker
WORKDIR /home/docker

SHELL ["/bin/bash", "-lc"]

RUN cd /home/docker && \
    touch .sudo_as_admin_successful

# install cfn
RUN pip3 install cloudformation-cli cloudformation-cli-java-plugin

ENV PATH="${PATH}:/home/docker/.local/bin"

# and finally override the entry point, restoring bash
CMD /bin/bash
