# Brain IoT Tutorials

This repository holds the Brain IoT Tutorials. They are maintained as markdown and turned into HTML by [Jekyll][jekyll].

[.gitlab-ci.yml](.gitlab-ci.yml) is configured to build the site when the master branch is updated. However, the BRAIN-IoT GitLab is not configured to publish GitLab pages, so the site currently has to be deployed manually.

## Git Submodules

The project uses git submodules (so it can include content from other projects).

After you have cloned the repository, you need to initialise git submodules as follows:

```
$ git submodule init 
$ git submodule update
```

## Run Locally

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

[jekyll]: http://jekyllrb.com/
