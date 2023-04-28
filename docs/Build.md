### Build
The provided `build.xml` (under `source/`) can be used both for compiling the source code and generating the deployable jar files.

```sh
$ cd source/
$ ant dist
```

The above command generates three different jar binary files in the jars directory:

- `monitoring-bin-controller.jar` containing all the classes and dependencies related to the controller.
- `monitoring-bin-core.jar` containing a subset of classes and dependencies that can be used for instantiating Data Sources and Data Consumers.
- `monitoring-bin-core-all.jar` adds additional libraries and dependencies to the above bin-core jar.

and also a jar containing the source code
- `monitoring-src.jar`
