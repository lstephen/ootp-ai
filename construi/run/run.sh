#!/bin/bash

set -e
set -x

if [[ -z "$SKIP_GIT_SYNC" ]];
then
  mkdir -p /root/.ssh

  printf "Host github.com\n\tStrictHostKeyChecking no\n" >> ~/.ssh/config

  cp /ssh/id_rsa /root/.ssh/id_rsa
  chmod 600 /root/.ssh/id_rsa

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
    git clone --depth 1 git@github.com:lstephen/ootp-ai-data.git
  fi
fi

echo "Running..."
mvn -B exec:java -Dgpg.skip=true

echo "Updating data..."
cd ootp-ai-data

[[ -n "$GIT_AUTHOR_NAME" ]] && git config user.name $GIT_AUTHOR_NAME
[[ -n "$GIT_AUTHOR_EMAIL" ]] && git config user.email $GIT_AUTHOR_EMAIL

git add --all
git commit -m "$(date)"
git pull --rebase
git push origin master

echo "Done."

