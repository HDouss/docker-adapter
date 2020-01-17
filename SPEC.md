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

The storage backend layout is broken up into a content-addressable blob
store and repositories. The content-addressable blob store holds most data
throughout the backend, keyed by algorithm and digests of the underlying
content. Access to the blob store is controlled through links from the
repository to blobstore.

A repository is made up of layers, manifests and tags. The layers component
is just a directory of layers which are "linked" into a repository. A layer
can only be accessed through a qualified repository name if it is linked in
the repository. Uploads of layers are managed in the uploads directory,
which is key by upload id. When all data for an upload is received, the
data is moved into the blob store and the upload directory is deleted.
Abandoned uploads can be garbage collected by reading the startedat file
and removing uploads that have been active for longer than a certain time.

The third component of the repository directory is the manifests store,
which is made up of a revision store and tag store. Manifests are stored in
the blob store and linked into the revision store.
While the registry can save all revisions of a manifest, no relationship is
implied as to the ordering of changes to a manifest. The tag store provides
support for name, tag lookups of manifests, using "current/link" under a
named tag directory. An index is maintained to support deletions of all
revisions of a given manifest tag.

We cover the path formats implemented by this path mapper below.

Manifests:

	manifestRevisionsPathSpec:      <root>/v2/repositories/<name>/_manifests/revisions/
	manifestRevisionPathSpec:      <root>/v2/repositories/<name>/_manifests/revisions/<algorithm>/<hex digest>/
	manifestRevisionLinkPathSpec:  <root>/v2/repositories/<name>/_manifests/revisions/<algorithm>/<hex digest>/link

Tags:

	manifestTagsPathSpec:                  <root>/v2/repositories/<name>/_manifests/tags/
	manifestTagPathSpec:                   <root>/v2/repositories/<name>/_manifests/tags/<tag>/
	manifestTagCurrentPathSpec:            <root>/v2/repositories/<name>/_manifests/tags/<tag>/current/link
	manifestTagIndexPathSpec:              <root>/v2/repositories/<name>/_manifests/tags/<tag>/index/
	manifestTagIndexEntryPathSpec:         <root>/v2/repositories/<name>/_manifests/tags/<tag>/index/<algorithm>/<hex digest>/
	manifestTagIndexEntryLinkPathSpec:     <root>/v2/repositories/<name>/_manifests/tags/<tag>/index/<algorithm>/<hex digest>/link

Blobs:

	layerLinkPathSpec:            <root>/v2/repositories/<name>/_layers/<algorithm>/<hex digest>/link
	layersPathSpec:               <root>/v2/repositories/<name>/_layers

Uploads:

	uploadDataPathSpec:             <root>/v2/repositories/<name>/_uploads/<id>/data
	uploadStartedAtPathSpec:        <root>/v2/repositories/<name>/_uploads/<id>/startedat
	uploadHashStatePathSpec:        <root>/v2/repositories/<name>/_uploads/<id>/hashstates/<algorithm>/<offset>

Blob Store:

        blobsPathSpec:                  <root>/v2/blobs/
	blobPathSpec:                   <root>/v2/blobs/<algorithm>/<first two hex bytes of digest>/<hex digest>
	blobDataPathSpec:               <root>/v2/blobs/<algorithm>/<first two hex bytes of digest>/<hex digest>/data
	blobMediaTypePathSpec:          <root>/v2/blobs/<algorithm>/<first two hex bytes of digest>/<hex digest>/data

For more information on the semantic meaning of each path and their
contents, please see the path spec documentation.
