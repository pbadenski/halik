#!/bin/bash
set -e

pushd javaagent
# build java agent
mvn package
popd

pushd web
# "install web app dependencies"
npm install
# "fix for `libsass` bindings not found"
npm rebuild node-sass
# "build web app"
node_modules/gulp/bin/gulp.js build
# "install phantomjs"
npm install -g phantomjs-prebuilt
# "run mocha tests"
$(which phantomjs) node_modules/mocha-phantomjs-core/mocha-phantomjs-core.js test/index.html
# "build & publish production version of web app for public eye"
node_modules/gulp/bin/gulp.js --env production --target website build
cp -rf release/website ../server/dist
# "build production version of web app for plugin"
node_modules/gulp/bin/gulp.js --env production --target plugin build
popd

# SKIP FOR PERFORMANCE "install intellij core as maven dependency"
# pushd /tmp
# MVN_ARGS="-Dmaven.repo.local=${WERCKER_CACHE_DIR}" sh "$WERCKER_ROOT/intellij-plugin/install-idea"
# popd

pushd intellij-plugin
# "build intellij plugin"
mvn package
# "publish intellij plugin with server app"
mkdir ../server/dist/updates
cp target/v*/halik-intellij-plugin.zip ../server/dist/updates/
cp updates.xml ../server/dist/
popd

# "create deployment package"
mkdir deployment
cd server
zip -r ../deployment/$(git rev-parse HEAD).zip .

