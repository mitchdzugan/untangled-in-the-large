# Untangled in the Large

These are the files used in the [YouTube videos](https://youtu.be/j-_itpXEo6w) about Untangled in the Large.

This project was started from an [Untangled Template](https://github.com/untangled-web/untangled-template). It may be useful 
for you to review the details included in that README.

The files of interest are:
```
├── dev
│   ├── client
│   │   └── cljs
│   │       └── user.cljs
├── src
│   ├── client
│   │   └── large_example
│   │       ├── core.cljs
│   │       ├── macros.clj
│   │       └── ui
│   │           ├── routers.cljc
│   │           ├── routing.cljs
```

# Running

## Server

- Set the JVM_OPTS option `-Ddev` to work via figwheel and `index-dev.html`.
This ensures the proper page is served when you hit HTML5 routes as initial loads.
In IntelliJ, you can do this in the Run Configuration as a JVM parameter. At
the command line, just set the env variable JVM_OPTS to `-Ddev`.

Run a REPL, then call the function `(go)`. Refreshing the server
code and restarting it is `(reset)`. See `user.clj` or untangled-template for more detailed
details.

## Client

Start a normal REPL (e.g. via IntelliJ clojure.main) with the program argument
`script/figwheel.clj`.

You will not want to use figwheel alone, since it will not know to server
HTML5 routes. You'll want to use the server (which defaults to port 3000).

# HTML5 Routing

## Client Notes 

- The `pushy` startup code is in `core.cljs`. See the comments there.

## Server Setup

- See `system.clj` for how we're hacking the server to server index.html for any
URL. This is not needed if the user starts from `index.html`, but if they
try to load `http://host/x/y` from a bookmark, normally the server will
respond with NOT FOUND. Instead, we want it to just send the `index.html` and
let HTML5 routing figure out where they want to go in the app.
- Make sure your asset paths in your cljs compile configs start with `/`, otherwise the 
the paths generated from the generated code for dynamic loading will be relative,
and when you're jumping (via browser bookmark) to some route (e.g. `/a/b`) your
Ring response will be the content of `index.html`, but the browser will try
relative loads for `/a/b/x.js`, which will fail (actually, they'll probably send
the content of `index.html`, since that is set up as your "default" resource when
nothing is found).
