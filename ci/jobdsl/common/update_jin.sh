#!/bin/bash
JIN_BRANCH=${JIN_BRANCH:-master}
set -x
set -e
(test -e jin && git -C jin checkout master) || git clone git@github.com:gzvulon/mvp_jin_cli.git
git -C jin fetch
git -C jin checkout ${JIN_BRANCH}
git -C jin pull || echo no pull: deattached head
./jin/tools/jin --install

