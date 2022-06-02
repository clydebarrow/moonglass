FROM --platform=$TARGETPLATFORM scottlamb/moonfire-nvr:v0.7.3 AS deploy
LABEL maintainer="clyde@control-j.com"
ARG INVALIDATE_CACHE_BUILD_DEPLOY=
ENV LC_ALL=C.UTF-8
WORKDIR /usr/local/lib/moonfire-nvr
#RUN mv ui oldui ; mkdir ui
COPY html/ ui/

# The install instructions say to use --user in the docker run commandline.
# Specify a non-root user just in case someone forgets.
USER 10000:10000
WORKDIR /var/lib/moonfire-nvr
ENTRYPOINT [ "/usr/local/bin/moonfire-nvr" ]
