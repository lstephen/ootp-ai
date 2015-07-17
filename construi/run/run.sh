#!/bin/bash

set -e

printf "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

echo "Retrieving lastest data..."
rm -rf ootp-ai-data
git clone git@github.com:lstephen/ootp-ai-data.git

echo "Running..."
mvn -B install exec:java -Dgpg.skip=true

echo "Updating data..."
cd ootp-ai-data

[[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
[[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

git add --all
git commit -m "$(date)"
git push origin master

echo "Done."

