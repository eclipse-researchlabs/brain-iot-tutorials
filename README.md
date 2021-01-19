# Brain IoT Tutorials

This repository holds the Brain IoT Tutorials. They are maintained as markdown and turned into HTML by [Jekyll][jekyll].



## Git Submodules

The project uses git submodules (so it can include content from other projects).

After you have cloned the repository, you need to initialise git submodules as follows:

```
$ git submodule init 
$ git submodule update
```

### Include up-to-date content

To show full source files in the tutorials, they could each be copied to `_includes`, but then risk becoming out-of-date if the source is updated.

This is why it's better to reference them directly in their git repository, by adding a git submodule. If you want to reference code from another repository then add a submodule like this:

```
git submodule add https://github.com/eclipseresearchlabs/<repo-name>.git _includes/<repo-name>
```

where `repo-name` is the name of the repository

The full content of the git repository will then be available in `_includes/<repo-name>`.

## Run and View the Tutorials Locally

To Install Jekyll, you need the ruby gem command:

```shell
$ sudo gem install bundler jekyll
```

To run Jekyll locally:

```shell
$ ./run.sh
    Server address: http://127.0.0.1:4000
  Server running... press ctrl-c to stop.
```
You can now browse to <http://127.0.0.1:4000> to see the site, which is automatically regenerated as you change the files.

## Deploy

[.travis.yml](.travis.yml) is configured to build the site for all branches and pull requests. However, Travis is not configured to publish the tutorial website, so the site currently has to be deployed manually.

The generated Jekyll site is in `_site` and can be deployed to any web server.

The site currently uses *absolute* links for some resources, which will break if not deployed to the web server root. This should be fixable by making the offending links *relative*.

A workaround is to use `wget` to make the site relative:

```
wget --recursive --no-clobber --page-requisites --html-extension --convert-links  --exclude-domains localhost http://127.0.0.1:4000
```


[jekyll]: http://jekyllrb.com/
