#! /bin/sh
rm -rf html
mkdir -p html/static
tag=`git describe --tags | sed 's/^v//'`
cp -r ../webapp/build/distributions/* html/
find html -type f -exec gzipit  \{} \;
docker build . -t clydeps/moonglass:$tag
