# Objectify for Scala

#### Objectify is a light-weight web framework that helps you structure your application by providing policy management and dependency injection.

Here's a quick introduction I gave at a Scala meetup for an idea of what it's all about: [Scala Meetup - Objectify](http://www.slideshare.net/artgon/scala-meetup-objectify-15072182)

Note: Currently the only included adapter is for [Scalatra](http://www.scalatra.org/), so feel free to add more! :)

## Why would I use this?

This framework was inspired by [James Golick’s](http://jamesgolick.com/) similarly named [Ruby framework](https://github.com/bitlove/objectify).

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

#### Suggested file structure

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
    ├── GettingStarted.scala
    └── responders
        ├── TruePolicyResponder.scala
        └── numbers
            └── NumbersIndexResponder.scala
```

## Introducing Resolvers

#### Using the built-in resolvers

In this example we're going to add some more actions to the above example. We'll add a show service, that expects
a path in the format `/numbers/:id`, and a corresponding responder. It's using the built-in resolvers
`IdResolver` and `HttpServletResolver`.

```scala
class NumbersShowService(@Named("IdResolver") id: Int, req: HttpServletRequest) extends Service[Int] {
    def apply() = {
        if (req.getServerName.equals("localhost")) {
            100
        }
        else {
            id
        }
    }
}

class NumberShowResponder extends ServiceResponder[String, Int] {
    def apply(serviceResult: Int) = {
        serviceResult.toString
    }
}
```

This example demonstrates the two ways in which to specify Objectify resolvers. The easiest way is by type: if the resolver
has a fairly unique type -- e.g. `HttpServletRequest` -- the value will be injected by type. However, if it's a fairly
common type like `Int`, you'll need to specify the resolver by name, using the `javax.inject.Named` annotation.

#### Using the custom resolvers

Eventually you'll need to create your own resolver. To build on the above, let's parse our the server name in its own
resolver class:

```scala
class HostNameResolver extends Resolver[String, ObjectifyRequestAdapter] {
    def apply(param: ObjectifyRequestAdapter) = {
        param.getRequest.getServerName
    }
}
```

As you can see the resolver takes an `ObjectifyRequestAdapter` and gleans some sort of information from it. This is the
typical way in which resolvers should be used.

Here's what our updated service will look like:

```scala
class NumbersShowService(@Named("IdResolver") id: Int, @Named("HostNameResolver") hostname: String) extends Service[Int] {
    def apply() = {
        if (hostname.equals("localhost")) {
            100
        }
        else {
            id
        }
    }
}
```

Again, note that if we had a more specific type, we wouldn't need that `@Named` annotation. Here's what that might look
like:

```scala
case class HostNameString(hostname: String)

class HostNameResolver extends Resolver[HostNameString, ObjectifyRequestAdapter] {
    def apply(param: ObjectifyRequestAdapter) = {
        HostNameString(param.getRequest.getServerName)
    }
}

class NumbersShowService(@Named("IdResolver") id: Int, hostString: HostNameString) extends Service[Int] {
    def apply() = {
        if (hostString.hostname.equals("localhost")) {
            100
        }
        else {
            id
        }
    }
}
```

Note the updated route definition should like this:

```scala
class GettingStarted extends ObjectifyScalatraAdapter with ScalatraFilter {
    actions resource ("number") onlyRoute ("index" -> "numbers", "show" -> "numbers/:id") policy ~:[TruePolicy] -> ~:[TruePolicyResponder]
}
```

And file structure

```
├── io
├── service
│   ├── policies
│   │   └── TruePolicy.scala
│   └── services
│       └── numbers
│           ├── NumbersIndexService.scala
│           └── NumbersShowService.scala
└── ui
    ├── GettingStarted.scala
    ├── resolvers
    │   └── HostNameResolver.scala
    └── responders
        ├── TruePolicyResponder.scala
        └── numbers
            ├── NumbersIndexResponder.scala
            └── NumbersShowResponder.scala
```

## Advanced Examples

_todo_



