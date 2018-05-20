#!/bin/bash

mkdir ./resources
cd resources
if ! [ -a ./en/enwiktionary-20180120-pages-articles.xml.bz2 ]
then
  mkdir en
  cd en
  wget https://dumps.wikimedia.org/enwiktionary/20180120/enwiktionary-20180120-pages-articles.xml.bz2
  cd ..
fi
if ! [ -a ./fr/frwiktionary-20180201-pages-articles.xml.bz2 ]
then
  mkdir fr
  cd fr
  wget https://dumps.wikimedia.org/frwiktionary/20180201/frwiktionary-20180201-pages-articles.xml.bz2
  cd ..
fi
cd ..
