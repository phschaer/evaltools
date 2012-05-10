#! /bin/bash

rm ./results-all.txt
echo "P_SOLR" >> results-all.txt
./trec_eval -q qrels/qrels_ds_DE_2005.txt results-solr.txt >> results-all.txt
echo "P_BRADFORD" >> results-all.txt
./trec_eval -q qrels/qrels_ds_DE_2005.txt results-bradford.txt >> results-all.txt
echo "P_LOTKA" >> results-all.txt
./trec_eval -q qrels/qrels_ds_DE_2005.txt results-lotka.txt >> results-all.txt
echo "P_STR" >> results-all.txt
./trec_eval -q qrels/qrels_ds_DE_2005.txt results-str.txt >> results-all.txt
echo "P_RANDOM" >> results-all.txt
./trec_eval -q qrels/qrels_ds_DE_2005.txt results-random.txt >> results-all.txt
echo "P_AUTHCENT" >> results-all.txt
./trec_eval -q qrels/qrels_ds_DE_2005.txt results-authcent.txt >> results-all.txt

cat results-all.txt | grep P_
cat results-all.txt | grep num
