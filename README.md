Helper proxy to download projects dependencies
===

Why
---

Create a lightweight local dependency repository.

The purpose is not to do things like Nexus, or Artifactory, sometimes a lightweight repository is enough.

Target usage: Workshops without a good network, so we create a local Wifi host with all required dependencies.  


Configuration
---

Edit the `src/main/resouces/application.conf`.

E.g. 
```hocon
server {
  // port, use 0 to random one
  port = 7000

  // Loglevel for server: EXTENSIVE, STANDARD, MINIMAL, OFF
  log = "MINIMAL"
}

dependencies {
  // Destination
  downloadPath = "/tmp/dep-hosting/"

  // Proxies
  proxies {

    npm {
      npmProxy = true
      hosts = ["https://registry.npmjs.org/"]
      cache = 1 day
    }

    maven {
      npmProxy = false
      hosts = [
        // Google maven repo
        "https://dl.google.com/dl/android/maven2/",
        // JCenter maven repo
        "https://jcenter.bintray.com/",
        // Maven Central
        "https://repo.maven.apache.org/maven2/",
        // Spring repo
        "https://repo.spring.io/milestone/",
        //    "https://repo.spring.io/snapshot/",
        // mvnrepository
        "https://mvnrepository.com/artifact"]
    }

    other {
      npmProxy = false
      hosts = [
        // Gradle wrapper
        "https://downloads.gradle.org/"
      ]
    }
  }
}
```

To use this configuration, you should set:

* `http://localhost:7000/npm` as npm/yarn registry with `npm config set registry http://localhost:7000/npm`
* `http://localhost:7000/maven` as maven/gradle repository
* `http://localhost:7000/other` to download the gradlew wrapper 

Usage
---

### Grab all dependencies

1. Configure and run the dep-hosting server.
2. Configure your project to use the dep-hosting server as dependency repository
3. Clean your project
4. Clean all your local dependencies data (maven/ivy repo, npm/yarn cache, ...)
5. Run the build of your project

You might have to fix repeat these steps few times to fix all configuration issues. 

### Running the repository

Now you can replace the dep-hosting server by a static web server.

You just need to serve files from the hosting path (see configuration).

=> run your Apache, NGinx, ... 

#### Serve with NodeJS and [http-server](https://github.com/indexzero/http-server)

1. install [http-server](https://github.com/indexzero/http-server) with `npm i -g http-server`
2. run `http-server <hosting-path>  -p <hosting-port> -d`

 