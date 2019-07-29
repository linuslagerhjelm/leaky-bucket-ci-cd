FROM ubuntu:16.04

SHELL ["/bin/bash", "-c"]

RUN apt-get update
RUN apt-get -qq -y install \
    curl \
    unzip \
    zip

RUN curl -s "https://get.sdkman.io" | bash

RUN chmod a+x "$HOME/.sdkman/bin/sdkman-init.sh"
RUN chmod a+x "$HOME/.sdkman/src/sdkman-main.sh"
RUN source "$HOME/.sdkman/bin/sdkman-init.sh"

RUN $HOME/.sdkman/src/sdkman-main.sh install java 12.0.2-zulu
RUN $HOME/.sdkman/src/sdkman-main.sh install gradle 5.4.1


