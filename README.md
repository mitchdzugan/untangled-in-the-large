# Untangled in the Large

These are the files used in the YouTube video about Untangled in the Large.

Work in progres...there is a lot of extra stuff here for now

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

# Server Setup for HTML5 Routing

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
