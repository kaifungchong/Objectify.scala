# Objectify for Scala

#### Objectify is a light-weight web framework that helps you to structure your application by providing policy management and dependency injection.

Here's a quick introduction I gave at a Scala meetup for an idea of what it's all about: [Scala Meetup - Objectify](http://www.slideshare.net/artgon/scala-meetup-objectify-15072182)

Note: Currently the only included adapter is for [Scalatra](http://www.scalatra.org/), so feel free to add more! :)

## Why would I use this?

This framework was inspired by [James Gollick’s](http://jamesgolick.com/) similarly named [Ruby framework](https://github.com/bitlove/objectify).

When we used Scalatra by itself, services were defined all over the place, in various files and shared functionality
and codification was done via inheritance. This isn't necessarily Scalatra's fault, it's just something that happens 
when you use simple frameworks in large applications. It required more structure.

The goal of Objectify is to codify how services are structured, and how they should function. There is a
central definition of all paths, policies, responders and they are each structured in a very simple way, 
enforcing the single responsibility for principle for classes, and creating a separation of concerns.

The reason we implemented our own dependecy injection is to avoid using singletons when generally all we need
are request-scoped objects.

## How does it work?

Each request and response is broken down into *Resolvers*, *Policies*, *Services* and *Reponders*. The lifecycle of a
typical request would first go through your web container and then to Objectify:

1. Verify policies
2. If they are not satisfied, execute a policy responder
3. If they are satisfied, execute a service
4. Pass the service result to a service responder
5. Return response to web container

It's quite simple but creates powerful constructs that can be used to succinctly break down your application into
its various parts.

The resolvers are essentially only there for dependency injection and are populated into policies, resolvers and
responders via the constructor.

#### Policies

Policies are mapped to policy responders in the configurations. They examine the request and simply return a true or
false depending what their purpose is. If they return false, then the policy responder is executed.

#### Services

Services do the heavy lifting in your application. They use resolvers to inject whatever necessary request information
they need and complete some unit of work, returning a result. This result is later passed on to the mapped responder.

#### Responders

Responders are only responsible for taking a result from a service or policy and then serializing it into some sort of
appropriate result. If your application is just an API that returns JSON, then this responder would be responsible for
converting a Scala object result to its JSON respresentation.

#### Resolvers

Resolvers help Objectify figure out how to inject appropriate values into the contructors of Policies, Services and
Responders. For example if you just want to parse a "userId" field from a body of JSON, you would make an appropriate
resolver that does this extraction and your service would simple take a variable that's populated at construction time.
This is great for code re-use and single-responsibility.

## Installation

The first step to getting started is installing the library. After cloning, you can install it via:

`mvn install`

Then add it to your pom file with the following:

```xml
<dependency>
    <groupId>org.objectify</groupId>
    <artifactId>objectify-scala</artifactId>
    <version>0.0.14</version>
</dependency>
```

## Getting started

Let's get started building a basic Objectify application.

#### Create a policy

A simple policy that always returns true.

```scala
class TruePolicy extends Policy {
    def isAllowed = true
}
```

And a responder that should never be called.

```scala
class TruePolicyResponder extends PolicyResponder[String] {
    def apply() = "Epic Fail!"
}
```

#### Create a service

Then we can create a simple service that just returns a list of numbers.

```scala
class NumbersIndexService extends Service[List[Int]] {
    def apply() = {
        List(1, 2, 3, 4, 5)
    }
}
```

#### Create a responder

We also create a responder that corresponds to the above service, taking a list of numbers and serializing them to a string.

```scala
class NumbersIndexResponder extends ServiceResponder[String, List[Int]] {
    def apply(serviceResult: List[Int]) = {
        serviceResult.toString
    }
}
```

#### Define some routes

The filter is going to be where you define all of your routes.

```scala
class GettingStarted extends ObjectifyScalatraAdapter with ScalatraFilter {
    actions resource ("number") onlyRoute ("index" -> "numbers") policy ~:[TruePolicy] -> ~:[TruePolicyResponder]
}
```

Once, you've defined this filter you will also have to add it to your Scalatra configuration and you're all set!

#### Suggested File Structure

Objectify takes the convention over configuration approach and we suggest the
following file structure for the above examples:

```
├── io
├── service
│   ├── policies
│   │   └── TruePolicy.scala
│   └── services
│       └── numbers
│           └── NumbersIndexService.scala
└── ui
    └── responders
        ├── TruePolicyResponder.scala
        └── numbers
            └── NumbersIndexResponder.scala
```

## Introducing Resolvers

_todo_

## Advanced Examples

_todo_



