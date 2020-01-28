[![DevOps By Rultor.com](http://www.rultor.com/b/artipie/docker-adapter)](http://www.rultor.com/p/artipie/docker-adapter)
[![Managed by Zerocracy](https://www.0crat.com/badge/CT2E6TK9B.svg)](https://www.0crat.com/p/CT2E6TK9B)

![Github actions](https://github.com/artipie/docker-adapter/workflows/Maven%20Build/badge.svg)
[![PDD status](http://www.0pdd.com/svg?name=artipie/docker-adapter)](http://www.0pdd.com/p?name=artipie/docker-adapter)
[![License](https://img.shields.io/github/license/artipie/docker-adapter.svg?style=flat-square)](https://github.com/artipie/docker-adapter/blob/master/LICENSE)

Docker registry front and back end as Java dependency: front end includes all HTTP API functions
for Docker clients, back end provides classes to work with default registry file structure.
Back end depends on https://github.com/artipie/asto storage, so it can be served on file system, S3 or
other supported storages.

## Specification

Front end support non-blocking requests processing, this is why the back-end uses `Flow` API from JDK9.
ASTO storage system also support non-blocking data processing (in case of TCP/HTTP APIs, FS operations are
always blocking).

Registry documentation:
 - https://docs.docker.com/registry/introduction/
 - https://docs.docker.com/registry/spec/api/

The path layout in the storage backend is roughly as follows:

```
<root>/v2
    -> repositories/
        -> <name>/
            -> _manifests/
                revisions
                -> <manifest digest path>
                    -> link
                tags/<tag>
                -> current/link
                    -> index
                    -> <algorithm>/<hex digest>/link
                -> _layers/
                      <layer links to blob store>
                -> _uploads/<id>
                      data
                      startedat
                      hashstates/<algorithm>/<offset>
    -> blob/<algorithm>
        <split directory content addressable storage>
```

More detailed explanation of registry storage system see at SPEC.md file.
