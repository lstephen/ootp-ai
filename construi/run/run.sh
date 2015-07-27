#!/bin/bash

set -e

printf "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

if [ -d "ootp-ai-data/.git" ]; then
  echo "Pulling latest data..."
  cd ootp-ai-data
  git reset --hard HEAD
  git clean -fd || true
  git pull --rebase
  cd ..
else
  echo "Cloning latest data..."
  rm -rf ootp-ai-data
  git clone git@github.com:lstephen/ootp-ai-data.git
fi

echo "Running..."
mvn -B clean install exec:java -Dgpg.skip=true

echo "Updating data..."
cd ootp-ai-data

[[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
[[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

git add --all
git commit -m "$(date)"
git pull --rebase
git push origin master

echo "Done."

