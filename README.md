> # *If I had asked people what they wanted, they would have said faster horses.* #
> ***Henry Ford***

Intro: https://vimeo.com/165758772

More videos: https://vimeo.com/channels/halik

# Install

## Prepare environment

    $ mkdir /var/halik
    $ brew install python

## Backend

    $ cd server
    $ npm install

## Web UI

    $ cd web/
    $ npm install
    $ bower install
    $ gulp

## Java agent

    $ cd javaagent
    $ ./install-idea
    $ mvn install

If you need faster feedback cycle you might want to skip tests and proguard:

    $ mvn -DskipTests -Dskip.proguard=true package

# Run

## Web UI

    $ cd server/
    $ npm start

Go to: http://localhost:33284

## Quicksort example

Run from IntelliJ with VM options: "-javaagent:javaagent/target/uber-halik-javaagent-1.0.0-SNAPSHOT.jar"

It's likely you'll need to pass parameters at the end "=...". Look at AgentMain class for details.

# Competition

In enterprise:

* http://chrononsystems.com/
* https://comealive.io

In educational space:

* http://www.fullstack.io/choc/

# Similar products

* http://findtheflow.io/
* http://takipi.com
