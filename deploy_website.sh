#!/usr/bin/env bash

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# It requires Python to run.
# Install the packages with the following command:
# pip install mkdocs mkdocs-material

set -ex

# Generate API docs
./gradlew dokkaPostProcess

# Copy the changelog into the site, omitting the unreleased section
cat CHANGELOG.md \
 | grep -v '^## Unreleased' \
 | sed '/^## /,$!d' \
 > docs/changelog.md

# Copy the README into the index, omitting the license, docs links, and fixing hrefs
cat README.md \
  | sed 's:docs/img:img:g' \
  | sed -e '/## Documentation/,/(runsample)\./d' \
  | sed '/## License/Q' \
  > docs/index.md

# Add some extra links to the index page
cat >> docs/index.md <<- EOM

# API Reference

* [Commands and Exceptions](api/clikt/com.github.ajalt.clikt.core/index.md)
* [Options](api/clikt/com.github.ajalt.clikt.parameters.options/index.md)
* [Arguments](api/clikt/com.github.ajalt.clikt.parameters.arguments/index.md)
* [Parameter Type Conversions](api/clikt/com.github.ajalt.clikt.parameters.types/index.md)
* [Output Formatting](api/clikt/com.github.ajalt.clikt.output/index.md)
EOM

# Build and deploy the new site to github pages
mkdocs gh-deploy

# Remove the file copies
rm docs/index.md docs/changelog.md
