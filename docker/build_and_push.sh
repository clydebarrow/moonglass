#! /bin/sh
image="clydeps/moonglass"
rm -rf html
mkdir -p html/static
version=`git describe --tags | sed 's/^v//'`
cp -r ../webapp/build/distributions/* html/
find html -type f -exec gzipit  \{} \;
time docker buildx build \
    --tag="${image}:${version}" \
    --push \
    --platform=linux/amd64,linux/arm64/v8,linux/arm/v7 \
         .
