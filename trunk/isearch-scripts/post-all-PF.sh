#!/bin/bash

cd ../../isearch-v1.0/documents/PF
FILES=*
URL=http://localhost:8983/solr/update

for f in $FILES; do 
  id=isearch-`echo $f | sed -r 's/.pdf$//'` # extract the id from the filename
  #echo "Posting $id to $URL"
  curl "$URL/extract?literal.id=$id&literal.collection=isearch-PF" -F "text=@$f" 
  #curl "$URL/extract?literal.id=$id&extractOnly=true" -F "text=@$f" 
  echo
done

#send the commit command to make sure all the changes are flushed and visible
curl $URL --data-binary '<commit/>' -H 'Content-type:application/xml'
echo