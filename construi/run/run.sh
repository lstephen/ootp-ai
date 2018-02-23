#!/bin/bash

set -e
set -x

if [[ -z "$SKIP_GIT_SYNC" ]]
then
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
    git clone --depth 1 "https://${GITHUB_TOKEN}@github.com/lstephen/ootp-ai-data.git"
  fi
fi

echo "Running..."
MAVEN_OPTS="-Xmx1G" mvn -B exec:java -Dgpg.skip=true

if [[ -z "$SKIP_GIT_SYNC" ]]
then
  echo "Updating data..."
  cd ootp-ai-data

  [[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
  [[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

  git add --all
  git commit -m "$(date)"
  git pull --rebase
  git push origin master
fi

echo "Done."

