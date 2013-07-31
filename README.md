# Objectify for Scala

#### Objectify is a light-weight web framework that allows you to structure your appication by providing policy management and dependency injection.

Here's a quick presentation to give you an idea of what it's all about: [Scala Meetup - Objectify](http://www.slideshare.net/artgon/scala-meetup-objectify-15072182)

Note: Currently the only included adapter is for [Scalatra](http://www.scalatra.org/), so feel free to add more! :)

## Why would I use this?

This framework was inspired by [James Gollick’s](http://jamesgolick.com/) similarly named [Ruby framework](https://github.com/bitlove/objectify).

With Scalatra services were defined all over the place, in various files and shared functionality
and codification was done via inheritance. The goal of Objectify is to codify how services are
structured, and how they should function. There is central definition of all paths, policies, responders and
 they are each structured in a very simple way, enforcing the single responsibility for principle for classes
 and creating a separation of concerns.

The reason we wanted to add our own dependecy injection is avoid using singleton when generally all we need
are request-scoped objects.

## How does it work?

## Getting started

## Advanced Examples



